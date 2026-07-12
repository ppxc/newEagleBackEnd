package com.example.demo.service.impl;

import com.example.demo.entity.HeatData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HeatDataCacheServiceImplCascadeTest {

    private HeatDataCacheServiceImpl svc;

    @BeforeEach
    void setUp() {
        svc = new HeatDataCacheServiceImpl();
        ReflectionTestUtils.setField(svc, "stepDeg", 0.005);
        ReflectionTestUtils.setField(svc, "mergeDistDeg", 0.003);
        ReflectionTestUtils.setField(svc, "cascadeP95Min", 5);
        ReflectionTestUtils.setField(svc, "cascadeDominantRatio", 0.4);
        ReflectionTestUtils.setField(svc, "cascadeMergeDistDeg", 0.003);
        ReflectionTestUtils.setField(svc, "cascadeKMax", 8);
        ReflectionTestUtils.setField(svc, "algorithmMode", "cascade");
    }

    private HeatData pt(double lng, double lat, int count) {
        return new HeatData(lng, lat, count, false, null);
    }

    private void runCascade(List<HeatData> pts) {
        try {
            Method m = HeatDataCacheServiceImpl.class
                .getDeclaredMethod("markRegionAbnormalCascade", List.class);
            m.setAccessible(true);
            m.invoke(svc, pts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Set<String> VALID_SEVS = new HashSet<>(Arrays.asList("stat", "regionMax", "topk", "normal"));

    @Test @DisplayName("T10: sparse data, no P95 candidates → Step 2/3 path, no crash")
    void sparseDataDoesNotCrash() {
        List<HeatData> pts = Arrays.asList(
            pt(0.100, 0.100, 2), pt(0.101, 0.100, 3),
            pt(0.200, 0.200, 3), pt(0.201, 0.200, 2),
            pt(0.300, 0.300, 1), pt(0.301, 0.300, 4),
            pt(0.400, 0.400, 1), pt(0.401, 0.400, 1),
            pt(0.500, 0.500, 2), pt(0.501, 0.500, 1)
        );
        runCascade(pts);
        for (HeatData p : pts) assertTrue(VALID_SEVS.contains(p.getSeverity()),
            "unexpected severity: " + p.getSeverity());
    }

    @Test @DisplayName("T11: dense cluster dominating 4-neighborhood → Step 1 marks stat")
    void denseClusterTriggersStep1() {
        List<HeatData> pts = new ArrayList<>();
        for (int i = 0; i < 20; i++) pts.add(pt(0.100 + i*0.0001, 0.100, 100));
        for (int i = 0; i < 5; i++) pts.add(pt(0.105 + i*0.0001, 0.100, 5));
        for (int i = 0; i < 5; i++) pts.add(pt(0.095 + i*0.0001, 0.100, 5));
        for (int i = 0; i < 5; i++) pts.add(pt(0.100 + i*0.0001, 0.105, 5));
        for (int i = 0; i < 5; i++) pts.add(pt(0.100 + i*0.0001, 0.095, 5));
        runCascade(pts);
        long statCount = pts.stream().filter(p -> "stat".equals(p.getSeverity())).count();
        assertTrue(statCount > 0, "Step 1 must fire and mark at least one point as stat");
    }

    @Test @DisplayName("T12: cascade fallback chain executes, doesn't crash on moderate data")
    void cascadeHandlesModerateData() {
        // 3x3 region grid where each cell has count=15. Step 1 may or may not fire
        // depending on cluster-dominance edge cases. The invariant: no crash,
        // no severity values outside the valid set, and EVERY point gets a severity.
        List<HeatData> pts = new ArrayList<>();
        for (int i = 0; i < 3; i++) pts.add(pt(0.100 + i*0.001, 0.100, 15)); // center cell
        for (int i = 0; i < 3; i++) pts.add(pt(0.105 + i*0.001, 0.100, 15)); // east cell
        for (int i = 0; i < 3; i++) pts.add(pt(0.100 + i*0.001, 0.105, 15)); // north cell
        for (int i = 0; i < 3; i++) pts.add(pt(0.100 + i*0.001, 0.095, 15)); // south cell
        for (int i = 0; i < 3; i++) pts.add(pt(0.095 + i*0.001, 0.100, 15)); // west cell
        runCascade(pts);
        // If Step 1 fires → expect some 'stat'. If not → expect 'regionMax' or 'topk'.
        // Either way: at least one abnormal and all severities must be valid.
        long abnormalCount = pts.stream().filter(HeatData::getAbnormal).count();
        assertTrue(abnormalCount > 0, "cascade must produce at least some anomaly on moderate data");
        for (HeatData p : pts) assertTrue(VALID_SEVS.contains(p.getSeverity()),
            "unexpected severity: " + p.getSeverity());
    }

    @Test @DisplayName("T13: nothing dominates → Step 3 top-K fallback")
    void step3FiresWhenNothingElseDoes() {
        // Suppress Step 1 by raising p95Min above any candidate's count.
        ReflectionTestUtils.setField(svc, "cascadeP95Min", 999);
        // 9 regions in a 3x3 interior grid: each cell has all 4 cardinal neighbors populated,
        // each cell count=3. dominantRatio per cluster = 3/15 = 0.2 → Step 2 fails entirely.
        // Step 3 fires and marks top-K=8 (out of 9 points).
        List<HeatData> pts = new ArrayList<>();
        for (int j = 1; j <= 3; j++) {
            for (int i = 1; i <= 3; i++) {
                pts.add(pt(0.100 + i*0.005, 0.100 + j*0.005, 3));
            }
        }
        runCascade(pts);
        long topkCount = pts.stream().filter(p -> "topk".equals(p.getSeverity())).count();
        long regionMaxCount = pts.stream().filter(p -> "regionMax".equals(p.getSeverity())).count();
        long statCount = pts.stream().filter(p -> "stat".equals(p.getSeverity())).count();
        assertEquals(0L, regionMaxCount, "Step 2 must NOT fire on 3x3 interior grid with ratio=0.2");
        assertEquals(0L, statCount, "Step 1 is suppressed (p95Min=999)");
        assertTrue(topkCount > 0 && topkCount <= 8,
            "Step 3 should mark at most top-K=8 points as topk, got " + topkCount);
    }

    @Test @DisplayName("T14: single point → no crash, normal severity")
    void singlePointNoCrash() {
        List<HeatData> pts = new ArrayList<>();
        pts.add(pt(0.100, 0.100, 1));
        runCascade(pts);
        assertEquals("normal", pts.get(0).getSeverity());
        assertFalse(pts.get(0).getAbnormal());
    }

    @Test @DisplayName("T15: cluster at lng edge → no 4-neighborhood crash")
    void boundaryLngNoCrash() {
        List<HeatData> pts = new ArrayList<>();
        for (int i = 0; i < 10; i++) pts.add(pt(0.001 + i*0.0001, 0.100, 100));
        for (int i = 0; i < 5; i++) pts.add(pt(0.006 + i*0.0001, 0.100, 5));
        for (int i = 0; i < 5; i++) pts.add(pt(0.001 + i*0.0001, 0.105, 5));
        for (int i = 0; i < 5; i++) pts.add(pt(0.001 + i*0.0001, 0.095, 5));
        runCascade(pts);
        for (HeatData p : pts) assertTrue(VALID_SEVS.contains(p.getSeverity()),
            "unexpected severity at lng edge: " + p.getSeverity());
    }
}
