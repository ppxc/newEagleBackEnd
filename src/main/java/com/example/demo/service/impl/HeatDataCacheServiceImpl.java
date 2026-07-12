package com.example.demo.service.impl;

import com.example.demo.entity.HeatData;
import com.example.demo.service.HeatDataCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 热力图数据缓存服务实现（重构版：单 Map + LRU + TTL）
 *
 * <h3>改进点</h3>
 * <ul>
 *   <li><b>单 Map</b>：4 张 Map (data/processing/progress/complete) → 1 张 Map&lt;LocalDate, CacheEntry&gt;，
 *       避免"忘记清一个 Map 导致状态不一致"的问题</li>
 *   <li><b>LRU</b>：超过 MAX_CACHE_SIZE 时按 lastAccessTime 淘汰最早的</li>
 *   <li><b>TTL</b>：超过 7 天未访问的条目会被定时清理</li>
 *   <li><b>O(1) count</b>：{@link #getCachedCount} 不复制列表，直接返回 size</li>
 * </ul>
 */
@Service
public class HeatDataCacheServiceImpl implements HeatDataCacheService {

    private static final Logger logger = LoggerFactory.getLogger(HeatDataCacheServiceImpl.class);

    /** 缓存最大日期数（按访问时间淘汰） */
    private static final int MAX_CACHE_SIZE = 100;
    /** TTL：7 天未访问则过期 */
    private static final long TTL_MILLIS = 7L * 24 * 60 * 60 * 1000;
    /** 清理任务执行间隔：每 10 分钟跑一次 */
    private static final long CLEANUP_INTERVAL_MS = 10L * 60 * 1000;

    /** 单区域占总案件 > 95% 强制标 stat */
    private static final double DOMINANT_REGION_RATIO = 0.95;

    @Value("${heatmap.region.step-deg:1.0}")
    private double stepDeg;
    @Value("${heatmap.region.merge-dist-deg:0.003}")
    private double mergeDistDeg;
    @Value("${heatmap.region.p95-min:5}")
    private int p95Min;
    @Value("${heatmap.region.dominant-ratio:0.20}")
    private double dominantRatio;
    @Value("${heatmap.region.k-max:8}")
    private int kMax;

    // ============= Algorithm mode switch =============
    @Value("${heatmap.algorithm.mode:p95}")
    private String algorithmMode;

    // ============= Cascade (v3) parameters =============
    @Value("${heatmap.cascade.p95-min:5}")
    private int cascadeP95Min;

    @Value("${heatmap.cascade.dominant-ratio:0.4}")
    private double cascadeDominantRatio;

    @Value("${heatmap.cascade.merge-dist-deg:0.003}")
    private double cascadeMergeDistDeg;

    @Value("${heatmap.cascade.k-max:8}")
    private int cascadeKMax;

    // @deprecated ignored by P95 algorithm, kept for property-binding backwards-compat
    @Value("${heatmap.region.sigma:1.3}")
    private double sigma;
    // @deprecated ignored by P95 algorithm, kept for property-binding backwards-compat
    @Value("${heatmap.region.watch-sigma:1.0}")
    private double watchSigma;
    // @deprecated ignored by P95 algorithm, kept for property-binding backwards-compat
    @Value("${heatmap.region.k-min:1}")
    private int kMin;
    // @deprecated ignored by P95 algorithm, kept for property-binding backwards-compat
    @Value("${heatmap.region.use-robust:true}")
    private boolean useRobust;
    // @deprecated ignored by P95 algorithm, kept for property-binding backwards-compat
    @Value("${heatmap.region.dispersion-eps:1e-6}")
    private double dispersionEps;

    /** 最小非空区域数，少于此值不进行异常判定（避免稀疏日输出假信号） */
    private static final int MIN_NON_EMPTY_CELLS = 3;

    /** 单 Map 替代原来 4 张 Map */
    private final Map<LocalDate, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public List<HeatData> getCachedHeatData(LocalDate date) {
        CacheEntry entry = cache.get(date);
        if (entry == null) {
            return new ArrayList<>();
        }
        entry.touch();
        // 防御性拷贝，避免外部修改内部状态
        List<HeatData> copy = new ArrayList<>(entry.data);
        // 算法分派：3-way switch on heatmap.algorithm.mode.
        // Default 'p95' preserves the original production behavior.
        switch (algorithmMode) {
            case "cascade":
                markRegionAbnormalCascade(copy);
                break;
            case "legacy":
                markRegionAbnormal(copy);
                break;
            case "p95":
            default:
                markRegionAbnormalP95(copy);
                break;
        }
        return copy;
    }

    /**
     * 基于"区域聚合 + P95 + 距离合并 + 主导比"标记异常点
     *
     * 流程：
     *   Step 1: 按 (regionLng, regionLat) 聚合 → regionTotal
     *   Step 2: 边界检查（非空区域 < 3 → 全 normal）
     *   Step 3: 计算 P95（线性插值），threshold = max(p95-min, P95)
     *   Step 4: 候选 = count >= threshold 的网格
     *   Step 5: 候选按距离 < merge-dist-deg 合并为集群
     *   Step 6: 主导比 → stat；其余按合并 count 降序排，前 k-max → topk，其余 watch
     *   Step 7: 点级传播 + abnormal 字段
     */
    void markRegionAbnormalP95(List<HeatData> dataList) {
        if (dataList == null || dataList.isEmpty()) return;
        if (dataList.size() < MIN_NON_EMPTY_CELLS) {
            markAllNormal(dataList);
            return;
        }

        // Step 1: 区域聚合
        Map<RegionKey, int[]> regionStats = new HashMap<>();
        for (HeatData p : dataList) {
            if (p.getLng() == null || p.getLat() == null) continue;
            RegionKey k = regionOf(p.getLng(), p.getLat());
            int[] stat = regionStats.computeIfAbsent(k, x -> new int[2]);
            stat[0] += (p.getCount() != null ? p.getCount() : 0);
            stat[1] += 1;
        }
        if (regionStats.isEmpty()) {
            markAllNormal(dataList);
            return;
        }

        // 收集非空网格
        List<CellView> nonEmpty = new ArrayList<>();
        long globalTotal = 0;
        for (Map.Entry<RegionKey, int[]> e : regionStats.entrySet()) {
            if (e.getValue()[0] > 0) {
                nonEmpty.add(new CellView(e.getKey(), e.getValue()[0]));
                globalTotal += e.getValue()[0];
            }
        }
        if (nonEmpty.size() < MIN_NON_EMPTY_CELLS) {
            markAllNormal(dataList);
            return;
        }

        // Step 2: 计算 P95（线性插值）
        nonEmpty.sort(Comparator.comparingInt(c -> c.count));
        int n = nonEmpty.size();
        double rank = 0.95 * (n - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        double frac = rank - lo;
        double p95 = nonEmpty.get(lo).count * (1 - frac) + nonEmpty.get(hi).count * frac;
        double threshold = Math.max(p95Min, p95);

        // Step 3: 候选 = count >= threshold
        List<CellView> candidates = new ArrayList<>();
        for (CellView cv : nonEmpty) {
            if (cv.count >= threshold) candidates.add(cv);
        }

        // Step 4: 按距离合并候选为集群
        List<Cluster> clusters = new ArrayList<>();
        for (CellView cv : candidates) {
            Cluster target = null;
            for (Cluster cl : clusters) {
                if (cl.containsWithin(cv, mergeDistDeg)) {
                    target = cl;
                    break;
                }
            }
            if (target == null) {
                target = new Cluster();
                clusters.add(target);
            }
            target.add(cv);
        }

        // Step 5: 主导比 → stat
        Map<RegionKey, String> severity = new HashMap<>();
        List<Cluster> remaining = new ArrayList<>();
        for (Cluster cl : clusters) {
            if (globalTotal > 0 && (double) cl.totalCount / globalTotal > dominantRatio) {
                for (CellView cv : cl.cells) severity.put(cv.key, "stat");
            } else {
                remaining.add(cl);
            }
        }

        // Step 6: 按合并 count 降序，前 k-max → topk，其余 watch
        remaining.sort((a, b) -> Long.compare(b.totalCount, a.totalCount));
        for (int i = 0; i < remaining.size(); i++) {
            String sev = (i < kMax) ? "topk" : "watch";
            for (CellView cv : remaining.get(i).cells) severity.put(cv.key, sev);
        }

        // Step 7: 点级传播 + abnormal
        for (HeatData p : dataList) {
            if (p.getLng() == null || p.getLat() == null) continue;
            RegionKey k = regionOf(p.getLng(), p.getLat());
            String sev = severity.getOrDefault(k, "normal");
            p.setSeverity(sev);
            p.setAbnormal(!"normal".equals(sev));
        }

        long statN = severity.values().stream().filter("stat"::equals).count();
        long topkN = severity.values().stream().filter("topk"::equals).count();
        long watchN = severity.values().stream().filter("watch"::equals).count();
        long normalN = regionStats.size() - severity.size();
        logger.info("[markRegionAbnormalP95] cells={}, p95={}, threshold={}, clusters={}, severity分布: stat={}, topk={}, watch={}, normal={}",
            regionStats.size(), String.format("%.2f", p95), String.format("%.2f", threshold),
            clusters.size(), statN, topkN, watchN, normalN);
    }

    /**
     * 网格视图：用于 P95 计算和距离合并
     */
    private static class CellView {
        final RegionKey key;
        final int count;
        CellView(RegionKey k, int c) {
            this.key = k;
            this.count = c;
        }
    }

    /**
     * 集群：候选网格按距离合并的容器
     */
    private static class Cluster {
        final List<CellView> cells = new ArrayList<>();
        long totalCount = 0;

        void add(CellView c) {
            cells.add(c);
            totalCount += c.count;
        }

        boolean containsWithin(CellView c, double distDeg) {
            for (CellView m : cells) {
                double dLng = m.key.lng - c.key.lng;
                double dLat = m.key.lat - c.key.lat;
                if (Math.sqrt(dLng * dLng + dLat * dLat) < distDeg) return true;
            }
            return false;
        }
    }

    /**
     * @deprecated 已替换为 markRegionAbnormalP95；保留以备回滚，不应在生产路径调用。
     * 旧实现：基于 median + MAD modified z-score
     */
    @Deprecated
    void markRegionAbnormal(List<HeatData> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        // Step 1: 区域聚合
        Map<RegionKey, int[]> regionStats = new HashMap<>();  // [total, pointCount]
        for (HeatData p : dataList) {
            if (p.getLng() == null || p.getLat() == null) continue;
            RegionKey k = regionOf(p.getLng(), p.getLat());
            int[] stat = regionStats.computeIfAbsent(k, x -> new int[2]);
            stat[0] += (p.getCount() != null ? p.getCount() : 0);
            stat[1] += 1;
        }
        if (regionStats.isEmpty()) {
            markAllNormal(dataList);
            return;
        }

        // Step 2a: 非空区域 < 2 → 全部 normal
        List<Integer> nonEmptyTotals = new ArrayList<>();
        for (int[] s : regionStats.values()) {
            if (s[0] > 0) nonEmptyTotals.add(s[0]);
        }
        if (nonEmptyTotals.size() < 2) {
            markAllNormal(dataList);
            return;
        }

        // 全局总数 + 95% 主导检测
        long globalTotal = 0;
        for (int[] s : regionStats.values()) globalTotal += s[0];
        RegionKey dominant = null;
        for (Map.Entry<RegionKey, int[]> e : regionStats.entrySet()) {
            if (e.getValue()[0] > 0
                && (double) e.getValue()[0] / globalTotal > DOMINANT_REGION_RATIO) {
                dominant = e.getKey();
            }
        }

        // Step 2b: 计算 median + MAD（仅对非空区域）
        nonEmptyTotals.sort(Integer::compareTo);
        int median = nonEmptyTotals.get(nonEmptyTotals.size() / 2);
        List<Integer> deviations = new ArrayList<>();
        for (int t : nonEmptyTotals) deviations.add(Math.abs(t - median));
        deviations.sort(Integer::compareTo);
        double mad = deviations.get(deviations.size() / 2);

        Map<RegionKey, String> severity = new HashMap<>();
        Map<RegionKey, Double> zScore = new HashMap<>();

        if (mad > 0) {
            // Step 3+4: modified zscore + 初步判定
            for (Map.Entry<RegionKey, int[]> e : regionStats.entrySet()) {
                RegionKey k = e.getKey();
                int total = e.getValue()[0];
                if (total == 0) {
                    severity.put(k, "normal");
                    zScore.put(k, 0.0);
                    continue;
                }
                double z = 0.6745 * (total - median) / mad;
                zScore.put(k, z);
                double absZ = Math.abs(z);
                if (absZ > sigma) {
                    severity.put(k, "stat");
                } else if (absZ > watchSigma) {
                    severity.put(k, "watch");
                } else {
                    severity.put(k, "normal");
                }
            }
        } else {
            // MAD = 0：所有 region_total 相等 → 全部 normal（后续走 K_min 兜底）
            for (RegionKey k : regionStats.keySet()) {
                severity.put(k, "normal");
                zScore.put(k, 0.0);
            }
        }

        // 95% 主导区域强制 stat
        if (dominant != null) severity.put(dominant, "stat");

        // Step 5a: K_min 补足
        long statCount = severity.values().stream().filter("stat"::equals).count();
        if (statCount < kMin) {
            int needed = (int) (kMin - statCount);
            // 候选：normal + watch 区域，按 regionTotal 降序
            List<Map.Entry<RegionKey, int[]>> candidates = new ArrayList<>();
            for (Map.Entry<RegionKey, int[]> e : regionStats.entrySet()) {
                String sev = severity.get(e.getKey());
                if ("stat".equals(sev)) continue;  // 已 stat 跳过
                candidates.add(e);
            }
            candidates.sort((a, b) -> b.getValue()[0] - a.getValue()[0]);
            for (int i = 0; i < Math.min(needed, candidates.size()); i++) {
                // watch 区域保持 watch，不升级 topk（spec §4.5 设计）
                RegionKey k = candidates.get(i).getKey();
                if (!"watch".equals(severity.get(k))) {
                    severity.put(k, "topk");
                }
            }
        }

        // Step 5b: K_max 截断（优先丢 topk，按 |z| 升序）
        List<RegionKey> actionable = new ArrayList<>();
        for (Map.Entry<RegionKey, String> e : severity.entrySet()) {
            if ("stat".equals(e.getValue()) || "topk".equals(e.getValue())) {
                actionable.add(e.getKey());
            }
        }
        if (actionable.size() > kMax) {
            int excess = actionable.size() - kMax;
            List<RegionKey> topkList = new ArrayList<>();
            for (RegionKey k : actionable) {
                if ("topk".equals(severity.get(k))) topkList.add(k);
            }
            topkList.sort((a, b) -> Double.compare(
                Math.abs(zScore.get(a)), Math.abs(zScore.get(b))));
            for (int i = 0; i < Math.min(excess, topkList.size()); i++) {
                severity.put(topkList.get(i), "normal");
            }
        }

        // Step 6: 点级传播 + 向后兼容 abnormal
        for (HeatData p : dataList) {
            if (p.getLng() == null || p.getLat() == null) continue;
            RegionKey k = regionOf(p.getLng(), p.getLat());
            String sev = severity.getOrDefault(k, "normal");
            p.setSeverity(sev);
            p.setAbnormal(!"normal".equals(sev));
        }

        long statN = severity.values().stream().filter("stat"::equals).count();
        long topkN = severity.values().stream().filter("topk"::equals).count();
        long watchN = severity.values().stream().filter("watch"::equals).count();
        long normalN = severity.values().stream().filter("normal"::equals).count();
        logger.info("[markRegionAbnormal] 区域数={}, median={}, MAD={}, severity分布: stat={}, topk={}, watch={}, normal={}",
            regionStats.size(), median, mad, statN, topkN, watchN, normalN);
    }

    /**
     * 区域 key：(floor(lng/step)*step, floor(lat/step)*step)
     * 用 static class + 手写 equals/hashCode（项目 Java 8，不支持 record）
     */
    /**
     * 级联方案 (cascade / v3) — three-step gating anomaly detector.
     *
     * <p>Step 1 (RED): per-region aggregation → P95 (linear interpolation) →
     *   candidates = regions with count >= max(p95Min, P95) → cluster candidates by
     *   Euclidean distance within cascadeMergeDistDeg → for each cluster, compute
     *   dominantRatio against the cluster's own count plus its 4 cardinal grid
     *   neighbors. Clusters with dominantRatio > cascadeDominantRatio are marked
     *   "stat" (red).
     *
     * <p>Step 2 (BLUE): only runs if Step 1 produced zero "stat". For each region,
     *   pick the highest-count point. Cluster those max-points by the same distance
     *   rule, compute dominantRatio the same way. Dominated maxima are marked
     *   "regionMax" (blue).
     *
     * <p>Step 3 (YELLOW): only runs if Step 1 AND Step 2 produced zero "stat".
     *   Sort all HeatData points by count descending, take top cascadeKMax, mark
     *   "topk" (yellow).
     *
     * <p>All other points → "normal".
     */
    void markRegionAbnormalCascade(List<HeatData> points) {
        if (points == null || points.isEmpty()) return;
        if (points.size() < MIN_NON_EMPTY_CELLS) {
            markAllNormal(points);
            return;
        }

        Map<RegionKey, int[]> regionStats = new HashMap<>();
        for (HeatData p : points) {
            if (p.getLng() == null || p.getLat() == null) continue;
            RegionKey k = regionOf(p.getLng(), p.getLat());
            int[] stat = regionStats.computeIfAbsent(k, x -> new int[2]);
            stat[0] += (p.getCount() != null ? p.getCount() : 0);
            stat[1] += 1;
        }
        if (regionStats.size() < MIN_NON_EMPTY_CELLS) { markAllNormal(points); return; }

        List<Integer> nonEmptyTotals = new ArrayList<>();
        for (int[] s : regionStats.values()) if (s[0] > 0) nonEmptyTotals.add(s[0]);
        if (nonEmptyTotals.size() < MIN_NON_EMPTY_CELLS) { markAllNormal(points); return; }
        nonEmptyTotals.sort(Integer::compareTo);

        double rank = 0.95 * (nonEmptyTotals.size() - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        double frac = rank - lo;
        double p95 = nonEmptyTotals.get(lo) * (1 - frac) + nonEmptyTotals.get(hi) * frac;
        double step1Threshold = Math.max(cascadeP95Min, p95);

        List<CellView> step1Candidates = new ArrayList<>();
        for (Map.Entry<RegionKey, int[]> e : regionStats.entrySet()) {
            if (e.getValue()[0] >= step1Threshold) {
                step1Candidates.add(new CellView(e.getKey(), e.getValue()[0]));
            }
        }

        List<Cluster> clusters = new ArrayList<>();
        for (CellView cv : step1Candidates) {
            Cluster target = null;
            for (Cluster cl : clusters) {
                if (cl.containsWithin(cv, cascadeMergeDistDeg)) { target = cl; break; }
            }
            if (target == null) { target = new Cluster(); clusters.add(target); }
            target.add(cv);
        }

        Set<RegionKey> step1Red = new HashSet<>();
        for (Cluster cl : clusters) {
            if (dominantRatio(cl, regionStats) > cascadeDominantRatio) {
                for (CellView cv : cl.cells) step1Red.add(cv.key);
            }
        }

        if (!step1Red.isEmpty()) {
            applySeveritiesByRegion(points, step1Red, "stat");
            logCascadeStep(1, regionStats.size(), step1Red.size());
            return;
        }

        Map<RegionKey, HeatData> regionMaxPoint = new HashMap<>();
        for (HeatData p : points) {
            if (p.getLng() == null || p.getLat() == null) continue;
            int c = p.getCount() != null ? p.getCount() : 0;
            if (c <= 0) continue;
            RegionKey k = regionOf(p.getLng(), p.getLat());
            HeatData cur = regionMaxPoint.get(k);
            if (cur == null || c > cur.getCount()) regionMaxPoint.put(k, p);
        }

        List<CellView> maxCells = new ArrayList<>();
        for (Map.Entry<RegionKey, HeatData> e : regionMaxPoint.entrySet()) {
            maxCells.add(new CellView(e.getKey(),
                e.getValue().getCount() != null ? e.getValue().getCount() : 0));
        }

        List<Cluster> maxClusters = new ArrayList<>();
        for (CellView cv : maxCells) {
            Cluster target = null;
            for (Cluster cl : maxClusters) {
                if (cl.containsWithin(cv, cascadeMergeDistDeg)) { target = cl; break; }
            }
            if (target == null) { target = new Cluster(); maxClusters.add(target); }
            target.add(cv);
        }

        Set<RegionKey> step2Blue = new HashSet<>();
        for (Cluster cl : maxClusters) {
            if (dominantRatio(cl, regionStats) > cascadeDominantRatio) {
                for (CellView cv : cl.cells) step2Blue.add(cv.key);
            }
        }

        if (!step2Blue.isEmpty()) {
            applySeveritiesByRegion(points, step2Blue, "regionMax");
            logCascadeStep(2, regionStats.size(), step2Blue.size());
            return;
        }

        List<HeatData> sorted = new ArrayList<>(points);
        sorted.sort((a, b) -> {
            int ca = a.getCount() != null ? a.getCount() : 0;
            int cb = b.getCount() != null ? b.getCount() : 0;
            if (cb != ca) return Integer.compare(cb, ca);
            return Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
        });
        Set<HeatData> topK = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(cascadeKMax, sorted.size()); i++) topK.add(sorted.get(i));

        for (HeatData p : points) {
            if (topK.contains(p)) { p.setSeverity("topk"); p.setAbnormal(true); }
            else                  { p.setSeverity("normal"); p.setAbnormal(false); }
        }
        logCascadeStep(3, regionStats.size(), topK.size());
    }

    /**
     * Apply {@code severity} to all points whose grid region is in {@code markedRegions}.
     * All other points become "normal".
     */
    private void applySeveritiesByRegion(List<HeatData> points,
                                         Set<RegionKey> markedRegions,
                                         String severity) {
        for (HeatData p : points) {
            if (p.getLng() == null || p.getLat() == null) {
                p.setSeverity("normal"); p.setAbnormal(false); continue;
            }
            RegionKey k = regionOf(p.getLng(), p.getLat());
            if (markedRegions.contains(k)) { p.setSeverity(severity); p.setAbnormal(true); }
            else                           { p.setSeverity("normal"); p.setAbnormal(false); }
        }
    }

    private void logCascadeStep(int step, int regionCount, int markedCount) {
        logger.info("[markRegionAbnormalCascade] step={} fired, regions={}, marked={}",
            step, regionCount, markedCount);
    }

    private RegionKey regionOf(Double lng, Double lat) {
        double rLng = Math.floor(lng / stepDeg) * stepDeg;
        double rLat = Math.floor(lat / stepDeg) * stepDeg;
        return new RegionKey(rLng, rLat);
    }

    /**
     * Compute the dominant ratio for a candidate cluster against its 4-neighborhood grid blocks.
     *
     * <p>"Nearby" semantics (v3 spec): a region is "near" the cluster if its
     * stepDeg-bucketed grid key lies in the cluster itself OR in one of the four
     * cardinal grid neighbors of any cluster cell. All non-zero region totals in
     * that footprint are summed into the denominator; the cluster's own total is
     * included by construction.
     *
     * <p>Grid-boundary regions that don't exist (e.g. west neighbor missing at the
     * data-domain edge) are simply omitted from the denominator.
     *
     * @return ratio in (0, 1]; returns 0.0 if the cluster is empty or the
     *         neighborhood total is 0
     */
    double dominantRatio(Cluster cluster, Map<RegionKey, int[]> regionStats) {
        if (cluster == null || cluster.cells.isEmpty()) return 0.0;

        long neighborTotal = 0;
        for (CellView cv : cluster.cells) {
            neighborTotal += cv.count;
            RegionKey[] neigh = new RegionKey[] {
                new RegionKey(cv.key.lng + stepDeg, cv.key.lat),
                new RegionKey(cv.key.lng - stepDeg, cv.key.lat),
                new RegionKey(cv.key.lng,         cv.key.lat + stepDeg),
                new RegionKey(cv.key.lng,         cv.key.lat - stepDeg)
            };
            for (RegionKey nk : neigh) {
                int[] s = regionStats.get(nk);
                if (s != null && s[0] > 0) neighborTotal += s[0];
            }
        }
        if (neighborTotal <= 0) return 0.0;
        return (double) cluster.totalCount / neighborTotal;
    }

    private void markAllNormal(List<HeatData> dataList) {
        for (HeatData p : dataList) {
            p.setSeverity("normal");
            p.setAbnormal(false);
        }
    }

    /**
     * 区域 key：半开区间 [a, a+step) 避免边界重复归类
     */
    private static class RegionKey {
        final double lng;
        final double lat;

        RegionKey(double lng, double lat) {
            this.lng = lng;
            this.lat = lat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RegionKey)) return false;
            RegionKey that = (RegionKey) o;
            return Double.compare(that.lng, lng) == 0
                && Double.compare(that.lat, lat) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lng, lat);
        }

        @Override
        public String toString() {
            return "(" + lng + "," + lat + ")";
        }
    }

    /**
     * 旧实现：基于 Poisson / Negative Binomial Pearson 残差
     * 已弃用（spec §十一回滚方案保留），保留以便回滚时切换
     */
    @Deprecated
    private void markAbnormalOnList(List<HeatData> dataList) {
        if (dataList == null || dataList.isEmpty()) return;
        int n = dataList.size();
        List<Integer> counts = new ArrayList<>();
        for (HeatData d : dataList) {
            if (d.getCount() != null) counts.add(d.getCount());
        }
        if (counts.isEmpty()) return;

        // Step 1: 计算全局均值 λ̂
        double lambda = counts.stream().mapToInt(Integer::intValue).average().orElse(0);
        if (lambda < 1e-6) return;

        // Step 2: 计算方差和离散度，判断是否过离散
        double variance = counts.stream()
                .mapToDouble(v -> Math.pow(v - lambda, 2)).sum() / n;
        double dispersion = variance / lambda; // Var/Mean 比值

        // Step 3: 估算负二项过离散参数 θ
        // θ = λ / (dispersion - 1)，dispersion <= 1 时无需过离散（用泊松）
        double theta = Double.MAX_VALUE;
        boolean useNegBin = false;
        if (dispersion > 1.0 && lambda > 0) {
            theta = lambda / (dispersion - 1.0);
            useNegBin = theta > 0.1; // θ 过小说明严重过离散，但仍可计算
        }

        // Step 4: 计算 Pearson 残差并标记
        long abnormalCount = 0;
        for (HeatData d : dataList) {
            double y = d.getCount() != null ? d.getCount() : 0;
            double residual;
            if (useNegBin) {
                // Negative Binomial Pearson 残差
                double nbVariance = lambda + (lambda * lambda) / theta;
                residual = (y - lambda) / Math.sqrt(nbVariance);
            } else {
                // Poisson Pearson 残差
                residual = (y - lambda) / Math.sqrt(lambda);
            }
            d.setAbnormal(Math.abs(residual) > 2.0);
            if (Math.abs(residual) > 2.0) abnormalCount++;
        }

        logger.info("[markAbnormalOnList] 数据量={}, 均值={}, 方差={}, 离散度={}, 模型={}, 异常点数={}",
                n, String.format("%.3f", lambda), String.format("%.3f", variance),
                String.format("%.3f", dispersion), useNegBin ? "NegativeBinomial" : "Poisson", abnormalCount);
    }

    @Override
    public int getCachedCount(LocalDate date) {
        CacheEntry entry = cache.get(date);
        if (entry == null) {
            return 0;
        }
        entry.touch();
        return entry.data.size();
    }

    @Override
    public void cacheHeatData(LocalDate date, List<HeatData> data) {
        CacheEntry entry = cache.computeIfAbsent(date, k -> new CacheEntry());
        entry.data = new ArrayList<>(data);
        entry.touch();
    }

    @Override
    public void clearCache(LocalDate date) {
        cache.remove(date);
    }

    @Override
    public boolean isProcessing(LocalDate date) {
        CacheEntry entry = cache.get(date);
        return entry != null && entry.processing;
    }

    @Override
    public void setProcessing(LocalDate date, boolean processing) {
        CacheEntry entry = cache.computeIfAbsent(date, k -> new CacheEntry());
        entry.processing = processing;
        if (!processing) {
            entry.progress = 100;
        }
        entry.touch();
    }

    @Override
    public int getProgress(LocalDate date) {
        CacheEntry entry = cache.get(date);
        return entry == null ? 0 : entry.progress;
    }

    @Override
    public void setProgress(LocalDate date, int progress) {
        CacheEntry entry = cache.computeIfAbsent(date, k -> new CacheEntry());
        entry.progress = Math.min(100, Math.max(0, progress));
        entry.touch();
    }

    @Override
    public boolean isCacheComplete(LocalDate date) {
        CacheEntry entry = cache.get(date);
        return entry != null && entry.complete;
    }

    @Override
    public void setCacheComplete(LocalDate date) {
        CacheEntry entry = cache.computeIfAbsent(date, k -> new CacheEntry());
        entry.complete = true;
        entry.processing = false;
        entry.progress = 100;
        entry.touch();
    }

    @Override
    public void mergeHeatData(LocalDate date, List<HeatData> newData) {
        CacheEntry entry = cache.computeIfAbsent(date, k -> new CacheEntry());
        if (newData == null || newData.isEmpty()) {
            return;
        }

        // 用临时 Map 去重（每次 merge 都重建，n 一般较小可接受）
        Map<String, HeatData> existingMap = new HashMap<>();
        for (HeatData hd : entry.data) {
            String key = String.format("%.3f,%.3f", hd.getLng(), hd.getLat());
            existingMap.put(key, hd);
        }

        for (HeatData newHd : newData) {
            String key = String.format("%.3f,%.3f", newHd.getLng(), newHd.getLat());
            HeatData existing = existingMap.get(key);
            if (existing != null) {
                existing.setCount(existing.getCount() + newHd.getCount());
            } else {
                entry.data.add(newHd);
                existingMap.put(key, newHd);
            }
        }
        entry.touch();
    }

    /**
     * 定期清理过期 / 超量条目
     * <p>每 10 分钟一次；过期（&gt; 7 天未访问）+ 超量（&gt; 100 条）→ 淘汰最旧</p>
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredAndOversized() {
        long now = System.currentTimeMillis();
        int sizeBefore = cache.size();

        // Step 1: 按 TTL 淘汰
        cache.entrySet().removeIf(e -> now - e.getValue().lastAccessTime > TTL_MILLIS);

        // Step 2: 按 LRU 淘汰（仅在超量时）
        if (cache.size() > MAX_CACHE_SIZE) {
            int evictCount = cache.size() - MAX_CACHE_SIZE;
            cache.entrySet().stream()
                    .sorted(Comparator.comparingLong(e -> e.getValue().lastAccessTime))
                    .limit(evictCount)
                    .map(Map.Entry::getKey)
                    .forEach(cache::remove);
        }

        int sizeAfter = cache.size();
        if (sizeBefore != sizeAfter) {
            logger.debug("热力图缓存清理: {} → {} (MAX_SIZE={}, TTL={}d)",
                    sizeBefore, sizeAfter, MAX_CACHE_SIZE, TTL_MILLIS / (24 * 60 * 60 * 1000));
        }
    }

    public int size() {
        return cache.size();
    }

    /**
     * 缓存条目：打包 data / 状态标志 / 进度 / 上次访问时间
     * <p>lastAccessTime 用 volatile，保证多线程下及时可见（写入远多于读，O(1) 缓存行更新）</p>
     */
    private static class CacheEntry {
        volatile List<HeatData> data = new ArrayList<>();
        volatile boolean processing;
        volatile int progress;
        volatile boolean complete;
        volatile long lastAccessTime = System.currentTimeMillis();

        void touch() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}
