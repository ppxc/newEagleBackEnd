package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 四级地址查询链路 — 第二级：内存缓存
 *
 * 查询链路：
 * ① 原表自带经纬度  -->  ② 内存缓存（本类）  -->  ③ 地址映射表 (location_cache)  -->  ④ 腾讯地图 API
 *
 * 用 ConcurrentHashMap 实现的简易内存缓存，带 5 分钟 TTL，
 * 避免每次都查 DB / 调外部 API。应用重启后缓存清空。
 */
@Component
public class InMemoryLocationCache {

    /** 缓存有效期（5 分钟） */
    private static final long LOCAL_CACHE_EXPIRE_MS = 5 * 60 * 1000L;

    /** key = "lng,lat" 精度保留 3 位，value = 中文地址 */
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    /** key = "lng,lat"，value = 写入时间戳（毫秒） */
    private final ConcurrentHashMap<String, Long> timestamps = new ConcurrentHashMap<>();

    /**
     * 查询缓存。命中且未过期返回地址，否则返回 null。
     * @param longitude 经度
     * @param latitude  纬度
     */
    public String get(double longitude, double latitude) {
        String key = key(longitude, latitude);
        Long ts = timestamps.get(key);
        if (ts != null && System.currentTimeMillis() - ts < LOCAL_CACHE_EXPIRE_MS) {
            return cache.get(key);
        }
        // 过期清理
        cache.remove(key);
        timestamps.remove(key);
        return null;
    }

    /**
     * 写入 / 覆盖缓存。
     */
    public void put(double longitude, double latitude, String address) {
        String key = key(longitude, latitude);
        cache.put(key, address);
        timestamps.put(key, System.currentTimeMillis());
    }

    /**
     * 清空全部缓存（用于运维或测试）。
     */
    public void clear() {
        cache.clear();
        timestamps.clear();
    }

    /**
     * 清理过期条目。建议由定时任务调用。
     * @return 清理条数
     */
    public int cleanExpired() {
        long now = System.currentTimeMillis();
        // 使用 Iterator 避免在迭代中修改 map 导致 ConcurrentModificationException
        java.util.Iterator<java.util.Map.Entry<String, Long>> it = timestamps.entrySet().iterator();
        int cleaned = 0;
        while (it.hasNext()) {
            java.util.Map.Entry<String, Long> e = it.next();
            if (now - e.getValue() > LOCAL_CACHE_EXPIRE_MS) {
                cache.remove(e.getKey());
                it.remove();  // 使用 Iterator.remove() 而非 map.remove()
                cleaned++;
            }
        }
        return cleaned;
    }

    public int size() {
        return cache.size();
    }

    private static String key(double longitude, double latitude) {
        return longitude + "," + latitude;
    }
}
