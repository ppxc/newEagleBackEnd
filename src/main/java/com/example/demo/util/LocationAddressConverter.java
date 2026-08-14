package com.example.demo.util;

import com.example.demo.entity.GeocodeResult;
import com.example.demo.entity.LocationCache;
import com.example.demo.entity.UserLocation;
import com.example.demo.mapper.LocationCacheMapper;
import com.example.demo.service.LocationProgressCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 经纬度 ↔ 中文地址 转换工具（异步 + 多级缓存）
 *
 * <h3>四级查询链路</h3>
 * <ol>
 *   <li><b>原表自带经纬度</b> — 调用方传入的 {@link UserLocation} 列表已含经纬度，无需查询</li>
 *   <li><b>内存缓存</b> — 本类内部的 ConcurrentHashMap，5 分钟 TTL（也提供独立的 {@link InMemoryLocationCache} 组件供其他代码复用）</li>
 *   <li><b>地址映射表（数据库）</b> — {@code location_cache} 表，由 {@code LocationCacheMapper} 查询</li>
 *   <li><b>腾讯地图 API</b> — {@code tencent.map.api-domain}/ws/geocoder/v1/，调用结果回写至 ② 和 ③</li>
 * </ol>
 *
 * <h3>关键策略</h3>
 * <ul>
 *   <li>批量查询：先去重再批量查 DB，避免 N+1</li>
 *   <li>异步写入：DB 写入走 {@code writeQueue} + 定时任务，每 10 秒批量 flush</li>
 *   <li>配额保护：{@link GeocodeScheduler} 令牌桶限流 (3 QPS)</li>
 *   <li>指数退避：API 失败按 2^n 秒重试，最多 3 次</li>
 * </ul>
 */
@Component
public class LocationAddressConverter {

    private static final Logger logger = LoggerFactory.getLogger(LocationAddressConverter.class);

    // 读取你配置的腾讯KEY
    @Value("${tencent.map.key}")
    private String tencentMapKey;
    @Value("${tencent.map.api-domain}")
    private String tencentMapDomain;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocationCacheMapper locationCacheMapper;

    // 异步写入线程池
    private final ExecutorService writeExecutorService = Executors.newSingleThreadExecutor();

    // 每秒最大请求数（保守设置为3，腾讯地图免费版通常是5，但建议留有余量）
    private static final int MAX_REQUESTS_PER_SECOND = 3;
    // 请求间隔（毫秒）- 确保每个请求之间有足够的间隔
    private static final long REQUEST_INTERVAL_MS = 350; // 350ms = 每秒约2.8个请求

    // 连接断开后的冷却时间（毫秒）
    private static final long CONNECTION_COOL_DOWN_MS = 5000;
    // 记录上次连接断开的时间
    private volatile long lastConnectionErrorTime = 0;
    
    // API配额超限后的冷却时间（毫秒）- 需要更长的等待时间
    private static final long RATE_LIMIT_COOL_DOWN_MS = 300;
    // 记录上次配额超限的时间
    private volatile long lastRateLimitErrorTime = 0;

    // ============ 新增：内存缓存层 ============
    // 本地内存缓存，减少DB查询
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();
    // 缓存有效期（5分钟）
    private static final long LOCAL_CACHE_EXPIRE_MS = 5 * 60 * 1000;
    // 缓存时间戳记录
    private final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    // ============ 新增：异步写入队列 ============
    // 待写入DB的缓存队列
    private final LinkedBlockingQueue<LocationCache> writeQueue = new LinkedBlockingQueue<>(1000);
    // 批量写入阈值
    private static final int BATCH_WRITE_THRESHOLD = 50;
    // 定时写入间隔（秒）
    private static final long SCHEDULED_WRITE_INTERVAL_SECONDS = 10;
    // 定时任务调度器
    private ScheduledExecutorService writeScheduler;
    // 批量写入锁
    private final Object batchWriteLock = new Object();
    // DB 写入连续失败计数（用于避免死循环重试）
    private volatile int consecutiveDbFailures = 0;
    // 最大连续失败次数，超过后丢弃数据避免队列无限增长
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    // 位置进度追踪服务
    private final LocationProgressCacheService locationProgressCacheService;

    private final GeocodeScheduler geocodeScheduler;

    @Autowired
    public LocationAddressConverter(LocationCacheMapper locationCacheMapper, RestTemplate restTemplate,
                                     LocationProgressCacheService locationProgressCacheService,
                                     GeocodeScheduler geocodeScheduler) {
        this.locationCacheMapper = locationCacheMapper;
        this.restTemplate = restTemplate;
        this.locationProgressCacheService = locationProgressCacheService;
        this.geocodeScheduler = geocodeScheduler;
    }

    /**
     * 初始化定时任务
     */
    @PostConstruct
    public void init() {
        writeScheduler = Executors.newSingleThreadScheduledExecutor();
        // 定时批量写入（每10秒）
        writeScheduler.scheduleAtFixedRate(this::flushWriteQueue,
            SCHEDULED_WRITE_INTERVAL_SECONDS,
            SCHEDULED_WRITE_INTERVAL_SECONDS,
            TimeUnit.SECONDS);
        // 定时清理过期本地缓存（每5分钟）
        writeScheduler.scheduleAtFixedRate(this::cleanExpiredLocalCache,
            5, 5, TimeUnit.MINUTES);
        logger.info("LocationAddressConverter 初始化完成");
    }

    /**
     * 销毁时关闭线程池
     */
    @PreDestroy
    public void destroy() {
        writeExecutorService.shutdown();
        if (writeScheduler != null) {
            writeScheduler.shutdown();
        }
        // 关闭前尝试刷新队列
        try {
            flushWriteQueue();
        } catch (Exception e) {
            logger.error("关闭前刷新队列失败: {}", e.getMessage());
        }
        logger.info("LocationAddressConverter 已销毁");
    }

    // 每批次处理的数量（默认每批处理20个坐标）
    private static final int BATCH_SIZE = 20;

    /**
     * 校验坐标是否有效（范围检查 + 非空检查）。
     * 无效坐标会自动设置错误地址，调用方无需额外处理。
     */
    private boolean isValidForConversion(UserLocation loc) {
        if (loc.getLongitude() == null || loc.getLatitude() == null ||
            Double.isNaN(loc.getLongitude()) || Double.isNaN(loc.getLatitude())) {
            loc.setAddress("坐标无效");
            return false;
        }
        if (loc.getLongitude() < 73 || loc.getLongitude() > 135 ||
            loc.getLatitude() < 3 || loc.getLatitude() > 53) {
            loc.setAddress("坐标范围无效");
            return false;
        }
        return true;
    }

    public List<UserLocation> convertBatch(List<UserLocation> locationList) {
        if (locationList == null || locationList.isEmpty()) return locationList;

        // 过滤出需要解析的坐标
        List<UserLocation> needConvertList = new ArrayList<>();

        // 收集所有需要查询缓存的坐标（用于批量查询）
        List<Map<String, Double>> coordList = new ArrayList<>();
        Set<String> addedCoords = new HashSet<>();

        for (UserLocation loc : locationList) {
            if (!isValidForConversion(loc)) continue;

            double lng = formatDouble(loc.getLongitude(), 5);
            double lat = formatDouble(loc.getLatitude(), 5);

            // 收集需要查询的坐标（O(1)去重）
            String dedupKey = lng + "," + lat;
            if (addedCoords.add(dedupKey)) {
                Map<String, Double> coordMap = new HashMap<>();
                coordMap.put("longitude", lng);
                coordMap.put("latitude", lat);
                coordList.add(coordMap);
            }
        }

        // 批量查询缓存（先查本地缓存，再查DB）
        Map<String, String> cacheResultMap = new HashMap<>();
        List<Map<String, Double>> needDbQueryList = new ArrayList<>();

        if (!coordList.isEmpty()) {
            logger.info("批量查询缓存，共 {} 个坐标", coordList.size());
            
            // 先查本地缓存
            for (Map<String, Double> coordMap : coordList) {
                double lng = coordMap.get("longitude");
                double lat = coordMap.get("latitude");
                String coordKey = lng + "," + lat;
                String cachedAddress = getFromLocalCache(lng, lat);
                if (cachedAddress != null) {
                    cacheResultMap.put(coordKey, cachedAddress);
                    logger.debug("本地缓存命中: lat={}, lng={}", lat, lng);
                } else {
                    needDbQueryList.add(coordMap);
                }
            }
            logger.info("本地缓存命中 {} 条，需要查询DB {} 条", cacheResultMap.size(), needDbQueryList.size());
            
            // 再批量查询DB缓存
            if (!needDbQueryList.isEmpty()) {
                String currentTime = formatDate(LocalDateTime.now());
                try {
                    List<LocationCache> cachedList = locationCacheMapper.findValidByCoordinatesBatch(needDbQueryList, currentTime);
                    for (LocationCache cache : cachedList) {
                        String key = cache.getLongitude() + "," + cache.getLatitude();
                        cacheResultMap.put(key, cache.getAddress());
                        // 同时更新到本地缓存
                        localCache.put(key, cache.getAddress());
                        cacheTimestamps.put(key, System.currentTimeMillis());
                    }
                    logger.info("DB缓存查询完成，命中 {} 条缓存", cachedList.size());
                } catch (Exception e) {
                    logger.error("批量缓存查询失败: {}", e.getMessage());
                }
            }
        }

        // 根据缓存结果设置地址（遍历完整列表，确保同坐标的所有人员都能获取到缓存地址）
        Set<String> needConvertCoords = new HashSet<>();
        for (UserLocation loc : locationList) {
            if (loc.getAddress() != null) continue;  // 已有地址（无效坐标等）
            if (!isValidForConversion(loc)) continue;
            String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
            String cachedAddress = cacheResultMap.get(coordKey);
            if (cachedAddress != null) {
                loc.setAddress(cachedAddress);
            } else if (needConvertCoords.add(coordKey)) {
                needConvertList.add(loc);  // 每个坐标仅添加一个代表用于后续API调用
            }
        }

        // 分批次处理需要转换的坐标
        if (!needConvertList.isEmpty()) {
            // 对需要处理的坐标去重（根据经纬度）
            List<UserLocation> uniqueList = new ArrayList<>();
            Set<String> processedCoords = new HashSet<>();
            Map<String, UserLocation> coordToLocation = new HashMap<>();

            for (UserLocation loc : needConvertList) {
                String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
                if (!processedCoords.contains(coordKey)) {
                    processedCoords.add(coordKey);
                    coordToLocation.put(coordKey, loc);
                }
            }
            uniqueList.addAll(coordToLocation.values());

            logger.info("开始分批处理 {} 个唯一坐标，每批处理 {} 个", uniqueList.size(), BATCH_SIZE);

            // 分批次处理
            int totalSize = uniqueList.size();
            int batchCount = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;
            
            int consecutiveErrors = 0; // 连续错误计数
            int maxConsecutiveErrors = 5; // 最大连续错误数，超过则暂停
            
            for (int batchIndex = 0; batchIndex < batchCount; batchIndex++) {
                // 检查是否需要因为连续错误而暂停
                if (consecutiveErrors >= maxConsecutiveErrors) {
                    logger.warn("连续错误达到 {} 次，暂停处理", maxConsecutiveErrors);
                    try {
                        Thread.sleep(CONNECTION_COOL_DOWN_MS * 2); // 暂停更长时间
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    consecutiveErrors = 0; // 重置计数器
                }
                
                int start = batchIndex * BATCH_SIZE;
                int end = Math.min(start + BATCH_SIZE, totalSize);
                List<UserLocation> batchList = uniqueList.subList(start, end);
                
                logger.info("处理第 {}/{} 批次，共 {} 个坐标", batchIndex + 1, batchCount, batchList.size());
                
                // 处理当前批次
                for (UserLocation loc : batchList) {
                    try {
                        // 检查是否需要等待连接冷却
                        waitForConnectionCoolDown();
                        
                        // 等待直到可以发送请求
                        waitForRateLimit();
                        
                        // 执行转换（转换成功后会自动保存到缓存）
                        convertSingleLocation(loc);

                        // 重置连续错误计数
                        consecutiveErrors = 0;
                        
                    } catch (ResourceAccessException e) {
                        // 连接断开异常
                        consecutiveErrors++;
                        lastConnectionErrorTime = System.currentTimeMillis();
                        logger.error("连接断开异常（第 {} 次连续错误）: {}", consecutiveErrors, e.getMessage());
                        loc.setAddress("连接异常");
                    } catch (Exception e) {
                        consecutiveErrors++;
                        logger.error("坐标解析异常（第 {} 次连续错误）: {}", consecutiveErrors, e.getMessage());
                        loc.setAddress("解析异常");
                    }
                }
                
                logger.info("第 {}/{} 批次处理完成", batchIndex + 1, batchCount);
            }

            // 构建坐标→解析地址映射
            Map<String, String> coordToResolvedAddress = new HashMap<>();
            for (UserLocation loc : uniqueList) {
                String addr = loc.getAddress();
                if (addr != null && !addr.isEmpty()
                    && !addr.equals("解析异常") && !addr.equals("连接异常")
                    && !addr.equals("API配额超限") && !addr.equals("请求被中断")) {
                    String key = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
                    coordToResolvedAddress.put(key, addr);
                }
            }
            // 将解析结果回填到所有同坐标的位置（遍历完整列表，而非仅去重后的列表）
            for (UserLocation loc : locationList) {
                if (loc.getAddress() != null && !loc.getAddress().isEmpty()) continue;
                String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
                String addr = coordToResolvedAddress.get(coordKey);
                if (addr != null) loc.setAddress(addr);
            }
            
            logger.info("坐标处理完成");
        }

        return locationList;
    }

    /**
     * 分批处理方法：先处理第一批并返回，剩余批次后台异步处理
     * @param locationList 位置列表
     * @param processFirstBatch 是否先处理第一批再返回（true: 先处理第一批；false: 立即返回，全部后台处理）
     * @return 立即返回已处理缓存的结果，未处理的标记为"解析中"
     */
    public List<UserLocation> convertBatchWithAsync(List<UserLocation> locationList, boolean processFirstBatch, String taskKey) {
        if (locationList == null || locationList.isEmpty()) return locationList;

        // 过滤出需要解析的坐标
        List<UserLocation> needConvertList = new ArrayList<>();

        // 收集所有需要查询缓存的坐标（用于批量查询）
        List<Map<String, Double>> coordList = new ArrayList<>();
        Set<String> addedCoords = new HashSet<>();

        for (UserLocation loc : locationList) {
            if (!isValidForConversion(loc)) continue;

            double lng = formatDouble(loc.getLongitude(), 5);
            double lat = formatDouble(loc.getLatitude(), 5);

            // 收集需要查询的坐标（O(1)去重）
            if (addedCoords.add(lng + "," + lat)) {
                Map<String, Double> coordMap = new HashMap<>();
                coordMap.put("longitude", lng);
                coordMap.put("latitude", lat);
                coordList.add(coordMap);
            }
        }

        // 批量查询缓存（先查本地缓存，再查DB）
        Map<String, String> cacheResultMap = new HashMap<>();
        List<Map<String, Double>> needDbQueryList = new ArrayList<>();

        if (!coordList.isEmpty()) {
            logger.info("异步模式 - 批量查询缓存，共 {} 个坐标", coordList.size());
            
            // 先查本地缓存
            for (Map<String, Double> coordMap : coordList) {
                double lng = coordMap.get("longitude");
                double lat = coordMap.get("latitude");
                String coordKey = lng + "," + lat;
                String cachedAddress = getFromLocalCache(lng, lat);
                if (cachedAddress != null) {
                    cacheResultMap.put(coordKey, cachedAddress);
                    logger.debug("异步模式 - 本地缓存命中: lat={}, lng={}", lat, lng);
                } else {
                    needDbQueryList.add(coordMap);
                }
            }
            logger.info("异步模式 - 本地缓存命中 {} 条，需要查询DB {} 条", cacheResultMap.size(), needDbQueryList.size());
            
            // 再批量查询DB缓存
            if (!needDbQueryList.isEmpty()) {
                String currentTime = formatDate(LocalDateTime.now());
                try {
                    List<LocationCache> cachedList = locationCacheMapper.findValidByCoordinatesBatch(needDbQueryList, currentTime);
                    for (LocationCache cache : cachedList) {
                        String key = cache.getLongitude() + "," + cache.getLatitude();
                        cacheResultMap.put(key, cache.getAddress());
                        // 同时更新到本地缓存
                        localCache.put(key, cache.getAddress());
                        cacheTimestamps.put(key, System.currentTimeMillis());
                    }
                    logger.info("异步模式 - DB缓存查询完成，命中 {} 条缓存", cachedList.size());
                } catch (Exception e) {
                    logger.error("异步模式 - 批量缓存查询失败: {}", e.getMessage());
                }
            }
        }

        // 根据缓存结果设置地址（遍历完整列表，确保同坐标的所有人员都能获取到缓存地址）
        Set<String> needConvertCoords = new HashSet<>();
        for (UserLocation loc : locationList) {
            if (loc.getAddress() != null) continue;  // 已有地址（无效坐标等）
            if (!isValidForConversion(loc)) continue;
            String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
            String cachedAddress = cacheResultMap.get(coordKey);
            if (cachedAddress != null) {
                loc.setAddress(cachedAddress);
            } else if (needConvertCoords.add(coordKey)) {
                needConvertList.add(loc);  // 每个坐标仅添加一个代表用于后续API调用
            }
        }

        // 如果没有需要转换的坐标，直接返回
        if (needConvertList.isEmpty()) {
            logger.info("所有坐标均已在缓存中，无需转换");
            return locationList;
        }

        // 对需要处理的坐标去重（根据经纬度）
        List<UserLocation> uniqueList = new ArrayList<>();
        Set<String> processedCoords = new HashSet<>();
        Map<String, UserLocation> coordToLocation = new HashMap<>();

        for (UserLocation loc : needConvertList) {
            String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
            if (!processedCoords.contains(coordKey)) {
                processedCoords.add(coordKey);
                coordToLocation.put(coordKey, loc);
            }
        }
        uniqueList.addAll(coordToLocation.values());

        logger.info("发现 {} 个需要转换的唯一坐标", uniqueList.size());

        // 如果只有一个批次，同步处理
        if (uniqueList.size() <= BATCH_SIZE) {
            logger.info("坐标数量少于批次大小，直接同步处理");
            return convertBatch(locationList);
        }

        // 标记所有待处理坐标为"解析中，请等待10秒后再次点击筛选按钮"
        for (UserLocation loc : needConvertList) {
            loc.setAddress("解析中，请稍后重试");
        }

        // 准备分批处理
        int totalSize = uniqueList.size();
        int batchCount = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;
        
        if (processFirstBatch) {
            // 同步处理第一批
            List<UserLocation> firstBatch = uniqueList.subList(0, Math.min(BATCH_SIZE, totalSize));
            logger.info("同步处理第一批，共 {} 个坐标", firstBatch.size());
            
            for (UserLocation loc : firstBatch) {
                try {
                    waitForRateLimit();
                    convertSingleLocation(loc);
                } catch (Exception e) {
                    logger.error("坐标解析异常: {}", e.getMessage());
                    loc.setAddress("解析异常");
                }
            }

            // 构建坐标→解析地址映射
            Map<String, String> coordToResolvedAddress = new HashMap<>();
            for (UserLocation loc : firstBatch) {
                String addr = loc.getAddress();
                if (addr != null && !addr.isEmpty()
                    && !addr.equals("解析异常") && !addr.equals("连接异常")
                    && !addr.equals("API配额超限") && !addr.equals("请求被中断")) {
                    String key = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
                    coordToResolvedAddress.put(key, addr);
                }
            }
            // 将解析结果回填到所有同坐标的位置（遍历完整列表，而非仅去重后的列表）
            for (UserLocation loc : locationList) {
                if (loc.getAddress() != null && !loc.getAddress().isEmpty()) continue;
                String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
                String addr = coordToResolvedAddress.get(coordKey);
                if (addr != null) loc.setAddress(addr);
            }
            
            logger.info("第一批处理完成，立即返回结果");
        }

        // 异步处理剩余批次
        if (totalSize > BATCH_SIZE) {
            List<UserLocation> remainingList = new ArrayList<>(uniqueList.subList(BATCH_SIZE, totalSize));
            Map<String, UserLocation> remainingCoordMap = new HashMap<>();
            
            for (UserLocation loc : needConvertList) {
                String coordKey = formatDouble(loc.getLongitude(), 5) + "," + formatDouble(loc.getLatitude(), 5);
                if (remainingList.stream().anyMatch(r -> 
                    (formatDouble(r.getLongitude(), 5) + "," + formatDouble(r.getLatitude(), 5)).equals(coordKey))) {
                    remainingCoordMap.put(coordKey, loc);
                }
            }
            
            // 初始化进度追踪
            if (!uniqueList.isEmpty()) {
                try {
                    UserLocation firstLoc = uniqueList.get(0);
                    LocalDate date = getDateFromLocation(firstLoc);
                    locationProgressCacheService.setProcessing(date, true);
                    int firstBatchSize = processFirstBatch ? Math.min(BATCH_SIZE, totalSize) : 0;
                    int progress = (int) ((firstBatchSize * 100.0) / totalSize);
                    locationProgressCacheService.setProgress(date, progress);
                    logger.info("位置地址解析进度初始化: date={}, progress={}%", date, progress);
                } catch (Exception e) {
                    logger.warn("初始化进度追踪失败: {}", e.getMessage());
                }
            }

            // 通过统一调度器异步处理剩余批次
            geocodeScheduler.submit(taskKey, () -> {
                processRemainingBatchesAsync(remainingList, remainingCoordMap, batchCount - 1);
            });

            logger.info("剩余 {} 个坐标已提交后台异步处理", remainingList.size());
        }

        return locationList;
    }

    /**
     * 异步处理剩余批次
     */
    private void processRemainingBatchesAsync(List<UserLocation> remainingList, 
                                              Map<String, UserLocation> coordMap, 
                                              int batchCount) {
        logger.info("开始异步处理剩余 {} 个坐标，共 {} 批", remainingList.size(), batchCount);
        
        int totalSize = remainingList.size();
        int processedCount = 0;
        int consecutiveErrors = 0; // 连续错误计数
        int maxConsecutiveErrors = 5; // 最大连续错误数
        
        for (int batchIndex = 0; batchIndex < batchCount; batchIndex++) {
            // 检查是否需要因为连续错误而暂停
            if (consecutiveErrors >= maxConsecutiveErrors) {
                logger.warn("异步处理连续错误达到 {} 次，暂停处理", maxConsecutiveErrors);
                try {
                    Thread.sleep(CONNECTION_COOL_DOWN_MS * 2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                consecutiveErrors = 0;
            }
            
            int start = batchIndex * BATCH_SIZE;
            int end = Math.min(start + BATCH_SIZE, totalSize);
            List<UserLocation> batchList = remainingList.subList(start, end);
            
            logger.info("异步处理第 {}/{} 批次，共 {} 个坐标", batchIndex + 1, batchCount, batchList.size());
            
            for (UserLocation loc : batchList) {
                // 检查任务是否被取消
                if (geocodeScheduler.isInterrupted()) {
                    logger.info("异步地址解析任务被取消，已处理 {} 个坐标", processedCount);
                    try {
                        if (!remainingList.isEmpty()) {
                            UserLocation firstLoc = remainingList.get(0);
                            LocalDate date = getDateFromLocation(firstLoc);
                            locationProgressCacheService.setProcessing(date, false);
                        }
                    } catch (Exception ex) {
                        logger.warn("更新取消状态失败: {}", ex.getMessage());
                    }
                    return;
                }
                try {
                    // 检查是否需要等待连接冷却
                    waitForConnectionCoolDown();

                    // 检查是否需要等待API配额冷却
                    waitForRateLimitCoolDown();

                    waitForRateLimit();
                    convertSingleLocation(loc);
                    processedCount++;
                    consecutiveErrors = 0; // 重置连续错误计数
                    logger.debug("异步处理已完成 {} 个坐标", processedCount);
                } catch (ResourceAccessException e) {
                    // 连接断开异常
                    consecutiveErrors++;
                    lastConnectionErrorTime = System.currentTimeMillis();
                    logger.error("异步连接断开异常（第 {} 次连续错误）: {}", consecutiveErrors, e.getMessage());
                    loc.setAddress("连接异常");
                } catch (Exception e) {
                    consecutiveErrors++;
                    // 检查是否是配额超限错误
                    if (e.getMessage() != null && e.getMessage().contains("配额") || e.getMessage().contains("上限")) {
                        lastRateLimitErrorTime = System.currentTimeMillis();
                        logger.error("异步API配额超限（第 {} 次连续错误），将等待 {} ms", consecutiveErrors, RATE_LIMIT_COOL_DOWN_MS);
                        loc.setAddress("API配额超限");
                    } else {
                        logger.error("异步坐标解析异常（第 {} 次连续错误）: {}", consecutiveErrors, e.getMessage());
                        loc.setAddress("解析异常");
                    }
                }
            }
            
            logger.info("异步第 {}/{} 批次处理完成", batchIndex + 1, batchCount);

            // 报告进度
            try {
                if (!remainingList.isEmpty()) {
                    UserLocation firstLoc = remainingList.get(0);
                    LocalDate date = getDateFromLocation(firstLoc);
                    int overallTotal = BATCH_SIZE + totalSize;
                    int overallProcessed = BATCH_SIZE + processedCount;
                    int progress = (int) ((overallProcessed * 100.0) / overallTotal);
                    locationProgressCacheService.setProgress(date, progress);
                }
            } catch (Exception e) {
                logger.warn("更新进度失败: {}", e.getMessage());
            }
        }

        logger.info("所有异步处理完成，共处理 {} 个坐标", processedCount);

        // 标记完成
        try {
            if (!remainingList.isEmpty()) {
                UserLocation firstLoc = remainingList.get(0);
                LocalDate date = getDateFromLocation(firstLoc);
                locationProgressCacheService.setComplete(date);
                logger.info("位置地址解析全部完成: date={}", date);
            }
        } catch (Exception e) {
            logger.warn("标记完成失败: {}", e.getMessage());
        }
    }

    /**
     * 等待连接冷却时间（连接断开后需要等待一段时间再重试）
     */
    private synchronized void waitForConnectionCoolDown() {
        long now = System.currentTimeMillis();
        long timeSinceLastError = now - lastConnectionErrorTime;
        
        if (lastConnectionErrorTime > 0 && timeSinceLastError < CONNECTION_COOL_DOWN_MS) {
            long waitTime = CONNECTION_COOL_DOWN_MS - timeSinceLastError;
            logger.info("连接冷却中，等待 {} ms", waitTime);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 重置连接错误时间，允许继续处理
            lastConnectionErrorTime = 0;
        }
    }

    /**
     * 等待API配额冷却时间（配额超限后需要等待更长时间）
     */
    private synchronized void waitForRateLimitCoolDown() {
        long now = System.currentTimeMillis();
        long timeSinceLastRateLimitError = now - lastRateLimitErrorTime;
        
        if (lastRateLimitErrorTime > 0 && timeSinceLastRateLimitError < RATE_LIMIT_COOL_DOWN_MS) {
            long waitTime = RATE_LIMIT_COOL_DOWN_MS - timeSinceLastRateLimitError;
            logger.info("API配额冷却中，等待 {} ms", waitTime);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 重置配额错误时间，允许继续处理
            lastRateLimitErrorTime = 0;
        }
    }

    /**
     * 等待 API 调用许可（委托给统一调度器的令牌桶，锁外等待不阻塞其他线程）。
     */
    private void waitForRateLimit() {
        geocodeScheduler.acquire();
    }

    private void convertSingleLocation(UserLocation loc) {
        int retryCount = 0;
        int maxRetries = 3;
        
        while (retryCount < maxRetries) {
            try {
                Double lng = formatDouble(loc.getLongitude(), 5);
                Double lat = formatDouble(loc.getLatitude(), 5);

                String url = tencentMapDomain + "/ws/geocoder/v1/"
                        + "?location=" + lat + "," + lng
                        + "&key=" + tencentMapKey;

                logger.debug("发送请求: lat={}, lng={}", lat, lng);
                String jsonResp = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(jsonResp);

                String address;
                int status = root.path("status").asInt();
                
                if (status == 0) {
                    address = root.path("result").path("address").asText();
                    // 存入或更新缓存
                    updateToCache(lng, lat, address);
                    loc.setAddress(address);
                    logger.debug("请求成功: {}", address);
                    return; // 成功，退出重试循环
                } else {
                    String message = root.path("message").asText();
                    // 检查是否是配额超限错误
                    if (message.contains("上限") || message.contains("配额") || status == 121) {
                        logger.error("API配额超限: {}", message);
                        // 抛出异常，让上层处理配额超限的情况
                        throw new RuntimeException("API配额超限: " + message);
                    }
                    address = message;
                    loc.setAddress(address);
                    logger.warn("API返回错误: status={}, message={}", status, message);
                    return; // 其他错误，不再重试
                }
                
            } catch (RuntimeException e) {
                // 重新抛出配额超限异常
                throw e;
            } catch (Exception e) {
                retryCount++;
                logger.error("请求异常（重试 {}/{}）: {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    loc.setAddress("解析异常");
                } else {
                    // 指数退避等待
                    long waitTime = (long) Math.pow(2, retryCount) * 1000;
                    logger.info("等待 {} ms 后重试", waitTime);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        loc.setAddress("请求被中断");
                        return;
                    }
                }
            }
        }
    }

    /**
     * 异步更新缓存（不阻塞主线程）
     */
    private void updateToCache(Double longitude, Double latitude, String address) {
        // 1. 先更新本地内存缓存（立即生效）
        String coordKey = longitude + "," + latitude;
        localCache.put(coordKey, address);
        cacheTimestamps.put(coordKey, System.currentTimeMillis());
        
        // 2. 异步写入DB
        LocationCache cache = new LocationCache();
        cache.setLongitude(longitude);
        cache.setLatitude(latitude);
        cache.setAddress(address);
        cache.setCreateTime(formatDate(LocalDateTime.now()));
        cache.setUpdateTime(formatDate(LocalDateTime.now()));
        cache.setExpireTime(formatDate(LocalDateTime.now().plusDays(180)));
        
        // 加入写入队列（使用带超时的 offer 避免队列满时永久阻塞）
        try {
            if (!writeQueue.offer(cache, 5, TimeUnit.SECONDS)) {
                logger.warn("写入队列已满，丢弃缓存: lat={}, lng={}", latitude, longitude);
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("写入被中断，丢弃缓存: lat={}, lng={}", latitude, longitude);
            return;
        }
        // 检查是否达到批量写入阈值
        if (writeQueue.size() >= BATCH_WRITE_THRESHOLD) {
            triggerBatchWrite();
        }
    }

    /**
     * 触发批量写入
     */
    private void triggerBatchWrite() {
        writeExecutorService.submit(this::flushWriteQueue);
    }

    /**
     * 刷新写入队列到数据库
     */
    private void flushWriteQueue() {
        synchronized (batchWriteLock) {
            int queueSize = writeQueue.size();
            if (queueSize == 0) {
                return;
            }

            logger.info("开始批量写入缓存，队列大小: {}", queueSize);

            List<LocationCache> batchList = new ArrayList<>();
            writeQueue.drainTo(batchList);

            try {
                // 按坐标去重
                Map<String, LocationCache> uniqueMap = new LinkedHashMap<>();
                for (LocationCache cache : batchList) {
                    String key = cache.getLongitude() + "," + cache.getLatitude();
                    uniqueMap.put(key, cache);
                }

                List<LocationCache> uniqueList = new ArrayList<>(uniqueMap.values());
                logger.info("去重后 {} 条记录待写入", uniqueList.size());

                // 构建坐标列表用于批量查询
                List<Map<String, Double>> coordList = new ArrayList<>();
                for (LocationCache cache : uniqueList) {
                    Map<String, Double> coord = new HashMap<>();
                    coord.put("longitude", cache.getLongitude());
                    coord.put("latitude", cache.getLatitude());
                    coordList.add(coord);
                }

                // 1. 批量查询已存在的记录
                Map<String, LocationCache> existingMap = new HashMap<>();
                try {
                    List<LocationCache> existingList = locationCacheMapper.findByCoordinatesBatch(coordList);
                    for (LocationCache existing : existingList) {
                        String key = existing.getLongitude() + "," + existing.getLatitude();
                        existingMap.put(key, existing);
                    }
                } catch (Exception e) {
                    logger.error("批量查询缓存失败: {}", e.getMessage());
                }

                // 2. 分组：新记录走批量INSERT，已有记录逐条UPDATE
                List<LocationCache> newList = new ArrayList<>();
                for (LocationCache cache : uniqueList) {
                    String key = cache.getLongitude() + "," + cache.getLatitude();
                    LocationCache existing = existingMap.get(key);
                    if (existing != null) {
                        // 已有记录：更新
                        try {
                            existing.setAddress(cache.getAddress());
                            existing.setUpdateTime(cache.getUpdateTime());
                            existing.setExpireTime(cache.getExpireTime());
                            locationCacheMapper.updateById(existing);
                        } catch (Exception e) {
                            logger.error("更新缓存失败: key={}, {}", key, e.getMessage());
                        }
                    } else {
                        // 新记录：收集到批量插入列表
                        newList.add(cache);
                    }
                }

                // 3. 批量插入新记录
                if (!newList.isEmpty()) {
                    try {
                        locationCacheMapper.insertBatch(newList);
                        logger.info("批量插入 {} 条新缓存记录", newList.size());
                    } catch (Exception e) {
                        logger.error("批量插入缓存失败: {}", e.getMessage());
                        // 插入失败时逐条重试
                        for (LocationCache cache : newList) {
                            try {
                                locationCacheMapper.insert(cache);
                            } catch (Exception ex) {
                                logger.error("逐条插入缓存失败: {}", ex.getMessage());
                            }
                        }
                    }
                }

                logger.info("批量写入完成，UPDATE={}, INSERT={}",
                        uniqueList.size() - newList.size(), newList.size());

                // 写入成功，重置失败计数器
                consecutiveDbFailures = 0;
            } catch (Exception e) {
                consecutiveDbFailures++;
                logger.error("批量写入缓存失败（连续失败次数: {}）: {}", consecutiveDbFailures, e.getMessage());

                // 超过最大连续失败次数，丢弃数据避免队列无限增长
                if (consecutiveDbFailures >= MAX_CONSECUTIVE_FAILURES) {
                    logger.warn("DB 连续写入失败 {} 次，丢弃 {} 条缓存数据以避免死循环",
                            consecutiveDbFailures, batchList.size());
                } else {
                    // 尝试将数据放回队列（使用 offer 避免阻塞）
                    int requeued = 0;
                    int dropped = 0;
                    for (LocationCache cache : batchList) {
                        if (!writeQueue.offer(cache)) {
                            dropped++;
                        } else {
                            requeued++;
                        }
                    }
                    logger.info("数据重试：重新入队 {} 条，丢弃 {} 条", requeued, dropped);
                }
            }
        }
    }

    /**
     * 从本地缓存获取地址
     */
    private String getFromLocalCache(double longitude, double latitude) {
        String coordKey = longitude + "," + latitude;
        Long timestamp = cacheTimestamps.get(coordKey);
        if (timestamp != null && System.currentTimeMillis() - timestamp < LOCAL_CACHE_EXPIRE_MS) {
            return localCache.get(coordKey);
        }
        // 缓存过期，移除
        localCache.remove(coordKey);
        cacheTimestamps.remove(coordKey);
        return null;
    }

    /**
     * 定期清理过期的本地缓存条目，防止内存泄漏。
     * 由 writeScheduler 每 5 分钟调用一次。
     */
    private void cleanExpiredLocalCache() {
        long now = System.currentTimeMillis();
        // 使用 Iterator 避免在迭代中修改 map 导致 ConcurrentModificationException
        Iterator<Map.Entry<String, Long>> it = cacheTimestamps.entrySet().iterator();
        int cleaned = 0;
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (now - entry.getValue() > LOCAL_CACHE_EXPIRE_MS) {
                localCache.remove(entry.getKey());
                it.remove();  // 使用 Iterator.remove() 而非 map.remove()
                cleaned++;
            }
        }
        if (cleaned > 0) {
            logger.debug("清理过期本地缓存: {} 条, 当前缓存数: {}", cleaned, localCache.size());
        }
    }

    private void saveToCache(Double longitude, Double latitude, String address) {
        try {
            LocationCache cache = new LocationCache();
            cache.setLongitude(longitude);
            cache.setLatitude(latitude);
            cache.setAddress(address);
            cache.setCreateTime(formatDate(LocalDateTime.now()));
            cache.setUpdateTime(formatDate(LocalDateTime.now()));
            cache.setExpireTime(formatDate(LocalDateTime.now().plusDays(180)));
            locationCacheMapper.insert(cache);
        } catch (Exception e) {
            logger.error("缓存插入失败: {}", e.getMessage());
        }
    }

    public String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateFormatter.format(dateTime);
    }

    private double formatDouble(double value, int decimalPlaces) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * 安全获取日期（createTime 为 null 时回退到当前日期，避免进度追踪静默失败）
     */
    private LocalDate getDateFromLocation(UserLocation loc) {
        if (loc != null && loc.getCreateTime() != null) {
            return loc.getCreateTime().toLocalDate();
        }
        logger.debug("createTime 为 null，使用当前日期作为回退");
        return LocalDate.now();
    }

    /**
     * 正向地理编码：将地址转换为经纬度坐标
     * @param address 地址（建议包含城市名称）
     * @return GeocodeResult 包含坐标信息的结果对象
     */
    public GeocodeResult geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            GeocodeResult result = new GeocodeResult();
            result.setStatus(-1);
            result.setMessage("地址不能为空");
            return result;
        }

        // ====================== 先查询缓存（精确匹配） ======================
        LocationCache cache = locationCacheMapper.findValidByAddress(address, formatDate(LocalDateTime.now()));
        if (cache != null) {
            logger.debug("从缓存中获取地址坐标(精确匹配): address={}, lat={}, lng={}", address, cache.getLatitude(), cache.getLongitude());
            return buildResultFromCache(address, cache);
        }
        
        // ====================== 尝试模糊查询（截取地址前20个字） ======================
        String fuzzyKey = extractFuzzyKey(address);
        if (fuzzyKey != null) {
            String addressPattern = "%" + fuzzyKey + "%";
            cache = locationCacheMapper.findValidByAddressLike(addressPattern, formatDate(LocalDateTime.now()));
            if (cache != null) {
                logger.debug("从缓存中获取地址坐标(模糊匹配): address={}, fuzzyKey={}, lat={}, lng={}", address, fuzzyKey, cache.getLatitude(), cache.getLongitude());
                return buildResultFromCache(address, cache);
            }
        }

        // 智能追加城市前缀：不包含"成都市"则追加"四川省成都市"
        String queryAddress = buildGeocodeQueryAddress(address);

        int retryCount = 0;
        int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                // 等待速率限制
                waitForRateLimit();

                // URL编码地址（使用带前缀的地址进行API查询）
                String encodedAddress = java.net.URLEncoder.encode(queryAddress, "UTF-8");

                // 构建请求URL
                String url = tencentMapDomain + "/ws/geocoder/v1/"
                        + "?address=" + encodedAddress
                        + "&key=" + tencentMapKey
                        + "&policy=1"    // 宽松模式，允许地址中缺失城市
                        + "&output=json";

                logger.debug("发送正向地理编码请求: original={}, query={}", address, queryAddress);
                String jsonResp = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(jsonResp);

                GeocodeResult result = new GeocodeResult();
                int status = root.path("status").asInt();
                result.setStatus(status);
                result.setMessage(root.path("message").asText());
                result.setRequestId(root.path("request_id").asText());

                if (status == 0) {
                    JsonNode resultNode = root.path("result");
                    
                    GeocodeResult.Result resultData = new GeocodeResult.Result();
                    resultData.setTitle(resultNode.path("title").asText());
                    resultData.setReliability(resultNode.path("reliability").asInt());
                    resultData.setLevel(resultNode.path("level").asInt());

                    // 解析坐标
                    JsonNode locationNode = resultNode.path("location");
                    GeocodeResult.Location location = new GeocodeResult.Location();
                    location.setLat(formatDouble(locationNode.path("lat").asDouble(), 5));
                    location.setLng(formatDouble(locationNode.path("lng").asDouble(), 5));
                    resultData.setLocation(location);

                    // 解析地址部件
                    JsonNode addressComponentsNode = resultNode.path("address_components");
                    GeocodeResult.AddressComponents addressComponents = new GeocodeResult.AddressComponents();
                    addressComponents.setProvince(addressComponentsNode.path("province").asText());
                    addressComponents.setCity(addressComponentsNode.path("city").asText());
                    addressComponents.setDistrict(addressComponentsNode.path("district").asText());
                    addressComponents.setStreet(addressComponentsNode.path("street").asText());
                    addressComponents.setStreetNumber(addressComponentsNode.path("street_number").asText());
                    resultData.setAddressComponents(addressComponents);

                    // 解析行政区划信息
                    JsonNode adInfoNode = resultNode.path("ad_info");
                    GeocodeResult.AdInfo adInfo = new GeocodeResult.AdInfo();
                    adInfo.setAdcode(adInfoNode.path("adcode").asText());
                    resultData.setAdInfo(adInfo);

                    result.setResult(resultData);
                    logger.debug("正向地理编码成功: lat={}, lng={}", location.getLat(), location.getLng());
                    
                    // ====================== 将结果存入缓存 ======================
                    saveAddressToCache(address, location.getLng(), location.getLat());
                    
                    return result;
                } else {
                    logger.warn("正向地理编码返回错误: status={}, message={}", status, result.getMessage());
                    return result;
                }
                
            } catch (Exception e) {
                retryCount++;
                logger.error("正向地理编码异常（重试 {}/{}）: {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    GeocodeResult result = new GeocodeResult();
                    result.setStatus(-2);
                    result.setMessage("解析异常: " + e.getMessage());
                    return result;
                } else {
                    // 指数退避等待
                    long waitTime = (long) Math.pow(2, retryCount) * 1000;
                    logger.info("等待 {} ms 后重试", waitTime);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        GeocodeResult result = new GeocodeResult();
                        result.setStatus(-3);
                        result.setMessage("请求被中断");
                        return result;
                    }
                }
            }
        }

        // 不应该到达这里
        GeocodeResult result = new GeocodeResult();
        result.setStatus(-99);
        result.setMessage("未知错误");
        return result;
    }

    /**
     * 将正向地理编码结果存入缓存
     * @param address 地址
     * @param longitude 经度
     * @param latitude 纬度
     */
    private void saveAddressToCache(String address, Double longitude, Double latitude) {
        try {
            // 先检查是否已存在
            LocationCache existingCache = locationCacheMapper.findByAddress(address);
            if (existingCache != null) {
                // 更新缓存
                existingCache.setLongitude(longitude);
                existingCache.setLatitude(latitude);
                existingCache.setUpdateTime(formatDate(LocalDateTime.now()));
                existingCache.setExpireTime(formatDate(LocalDateTime.now().plusDays(180)));
                locationCacheMapper.updateById(existingCache);
                logger.debug("更新缓存: address={}", address);
            } else {
                // 插入新缓存
                LocationCache cache = new LocationCache();
                cache.setAddress(address);
                cache.setLongitude(longitude);
                cache.setLatitude(latitude);
                cache.setCreateTime(formatDate(LocalDateTime.now()));
                cache.setUpdateTime(formatDate(LocalDateTime.now()));
                cache.setExpireTime(formatDate(LocalDateTime.now().plusDays(180)));
                locationCacheMapper.insert(cache);
                logger.debug("插入缓存: address={}", address);
            }
        } catch (Exception e) {
            logger.error("缓存写入失败: {}", e.getMessage());
        }
    }

    /**
     * 从缓存构建GeocodeResult结果
     */
    private GeocodeResult buildResultFromCache(String address, LocationCache cache) {
        GeocodeResult result = new GeocodeResult();
        result.setStatus(0);
        result.setMessage("ok");
        result.setRequestId("cache");
        
        GeocodeResult.Result resultData = new GeocodeResult.Result();
        resultData.setTitle(address);
        resultData.setReliability(10);
        resultData.setLevel(10);
        
        GeocodeResult.Location location = new GeocodeResult.Location();
        location.setLat(cache.getLatitude());
        location.setLng(cache.getLongitude());
        resultData.setLocation(location);
        
        result.setResult(resultData);
        return result;
    }

    private static final int MAX_FUZZY_ADDRESS_LENGTH = 20;

    /**
     * 智能追加城市前缀：如果地址不包含"成都市"，则追加"四川省成都市"前缀
     * @param address 原始地址
     * @return 带前缀的查询地址
     */
    private String buildGeocodeQueryAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return address;
        }
        if (address.contains("成都市")) {
            return address;
        }
        return "四川省成都市" + address;
    }

    /**
     * 提取模糊查询关键字：限制最大长度为20个字
     * @param address 原始地址
     * @return 截取后的地址（最多20个字），如果地址为空或过短则返回null
     */
    private String extractFuzzyKey(String address) {
        if (address == null || address.length() < 2) {
            return null;
        }
        if (address.length() <= MAX_FUZZY_ADDRESS_LENGTH) {
            return address;
        }
        return address.substring(0, MAX_FUZZY_ADDRESS_LENGTH);
    }

    /**
     * 批量正向地理编码
     * @param addresses 地址列表
     * @return GeocodeResult列表
     */
    public List<GeocodeResult> geocodeBatch(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return new ArrayList<>();
        }

        List<GeocodeResult> results = new ArrayList<>();

        // 去重
        Set<String> processedAddresses = new LinkedHashSet<>(addresses);
        List<String> uniqueAddresses = new ArrayList<>(processedAddresses);

        logger.info("开始批量正向地理编码，共 {} 个唯一地址", uniqueAddresses.size());

        for (String address : uniqueAddresses) {
            GeocodeResult result = geocode(address);
            results.add(result);
        }

        logger.info("批量正向地理编码完成");
        return results;
    }
}