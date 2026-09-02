package com.example.demo.service.impl;

import com.example.demo.entity.CurGzlTableBm;
import com.example.demo.entity.CurGzlTableGroup;
import com.example.demo.entity.CurGzlTableRy;
import com.example.demo.entity.WorkloadDeptData;
import com.example.demo.entity.WorkloadGroupData;
import com.example.demo.entity.WorkloadEmpData;
import com.example.demo.mapper.WorkloadMapper;
import com.example.demo.service.WorkloadService;
import com.example.demo.util.TableNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkloadServiceImpl implements WorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadServiceImpl.class);

    @Resource
    private WorkloadMapper workloadMapper;

    @Override
    public String getMaxTjDate(String tableName) {
        // 白名单校验：阻断 SQL 注入（XML 中 FROM ${tableName} 是字符串拼接）
        TableNames.requireValid(tableName);
        return workloadMapper.getMaxTjDateByTable(tableName);
    }

    @Override
    public List<WorkloadDeptData> getDepartmentWorkload(
            String startDate, String endDate, String comName, String granularity) {

        // 查询日粒度原始数据
        List<CurGzlTableBm> rawData = workloadMapper.getWorkloadBmData(startDate, endDate, comName);

        // 按部门分组，再按时间粒度聚合
        Map<String, List<CurGzlTableBm>> byDept = rawData.stream()
                .collect(Collectors.groupingBy(CurGzlTableBm::getComName, LinkedHashMap::new, Collectors.toList()));

        List<WorkloadDeptData> result = new ArrayList<>();
        for (Map.Entry<String, List<CurGzlTableBm>> entry : byDept.entrySet()) {
            WorkloadDeptData dept = new WorkloadDeptData();
            dept.setComCode(entry.getValue().get(0).getComCode());
            dept.setComName(entry.getKey());
            dept.setData(aggregateBmData(entry.getValue(), granularity));
            result.add(dept);
        }
        return result;
    }

    @Override
    public List<WorkloadGroupData> getGroupWorkload(
            String comCode, String startDate, String endDate, String groups, String granularity) {

        List<CurGzlTableGroup> rawData = workloadMapper.getWorkloadGroupData(startDate, endDate, comCode, groups);

        // 过滤 groupsCode 为空的脏数据
        List<CurGzlTableGroup> validData = rawData.stream()
                .filter(g -> g.getGroupsCode() != null && !g.getGroupsCode().trim().isEmpty())
                .collect(Collectors.toList());

        // 按小组分组
        Map<String, List<CurGzlTableGroup>> byGroup = validData.stream()
                .collect(Collectors.groupingBy(CurGzlTableGroup::getGroups, LinkedHashMap::new, Collectors.toList()));

        List<WorkloadGroupData> result = new ArrayList<>();
        for (Map.Entry<String, List<CurGzlTableGroup>> entry : byGroup.entrySet()) {
            WorkloadGroupData group = new WorkloadGroupData();
            List<CurGzlTableGroup> groupData = entry.getValue();
            group.setGroupsCode(groupData.get(0).getGroupsCode());
            group.setGroupsName(entry.getKey());
            group.setComCode(groupData.get(0).getComCode());
            group.setData(aggregateGroupData(groupData, granularity));
            result.add(group);
        }
        return result;
    }

    @Override
    public List<WorkloadEmpData> getEmployeeWorkload(
            String groupsCode, String startDate, String endDate, String userName, String granularity) {

        List<CurGzlTableRy> rawData = workloadMapper.getWorkloadRyData(startDate, endDate, groupsCode, userName);

        // 按员工分组
        Map<String, List<CurGzlTableRy>> byEmp = rawData.stream()
                .collect(Collectors.groupingBy(CurGzlTableRy::getUserCode, LinkedHashMap::new, Collectors.toList()));

        // 计算同组平均工作量用于异常判定
        double groupAvg = rawData.stream()
                .mapToInt(e -> Optional.ofNullable(e.getZl()).orElse(0))
                .average()
                .orElse(0);
        double groupStd = calculateStd(rawData.stream()
                .map(e -> Optional.ofNullable(e.getZl()).orElse(0))
                .collect(Collectors.toList()), groupAvg);

        List<WorkloadEmpData> result = new ArrayList<>();
        for (Map.Entry<String, List<CurGzlTableRy>> entry : byEmp.entrySet()) {
            WorkloadEmpData emp = new WorkloadEmpData();
            List<CurGzlTableRy> empData = entry.getValue();
            emp.setUserCode(entry.getKey());
            emp.setUserName(empData.get(0).getUserName());
            emp.setGroupsCode(empData.get(0).getGroupsCode());
            emp.setGroupsName(empData.get(0).getGroups());
            emp.setComCode(empData.get(0).getComCode());

            // 计算员工总体是否异常（平均工作量低于同组1.5个标准差）
            double empAvg = empData.stream()
                    .mapToInt(e -> Optional.ofNullable(e.getZl()).orElse(0))
                    .average()
                    .orElse(0);
            emp.setIsAbnormal(groupStd > 0 ? (empAvg < groupAvg - 1.5 * groupStd) : false);

            emp.setData(aggregateRyData(empData, granularity));
            result.add(emp);
        }
        return result;
    }

    @Override
    public String getComCodeByName(String comName) {
        return workloadMapper.getComCodeByName(comName);
    }

    @Override
    public String getGroupsCodeByComAndName(String comCode, String groupsName) {
        return workloadMapper.getGroupsCodeByComAndName(comCode, groupsName);
    }

    // ==================== 私有聚合方法 ====================

    private List<WorkloadDeptData.MonthData> aggregateBmData(
            List<CurGzlTableBm> data, String granularity) {

        // 按时间粒度分组聚合
        Map<String, List<CurGzlTableBm>> grouped = data.stream()
                .collect(Collectors.groupingBy(
                        item -> getPeriodKey(item.getTjDate(), granularity),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 提取各周期 zl 值用于 EWMA 异常检测
        List<Integer> zlValues = grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getValue().stream().mapToInt(item -> Optional.ofNullable(item.getZl()).orElse(0)).sum())
                .collect(Collectors.toList());
        List<AbnormalInfo> abnormalInfos = detectEwmaAnomalyFull(zlValues);

        final List<AbnormalInfo> infoList = abnormalInfos;
        final int[] idx = {0};
        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    WorkloadDeptData.MonthData md = new WorkloadDeptData.MonthData();
                    md.setPeriod(entry.getKey());
                    List<CurGzlTableBm> items = entry.getValue();
                    md.setZl(items.stream().mapToInt(item -> Optional.ofNullable(item.getZl()).orElse(0)).sum());
                    md.setJa(items.stream().mapToInt(item -> Optional.ofNullable(item.getJa()).orElse(0)).sum());
                    md.setCkJsl(items.stream().mapToInt(item -> Optional.ofNullable(item.getCkJsl()).orElse(0)).sum());
                    md.setDsTjl(items.stream().mapToInt(item -> Optional.ofNullable(item.getDsTjl()).orElse(0)).sum());
                    AbnormalInfo info = infoList.get(idx[0]++);
                    md.setIsAbnormal(info.isAbnormal);
                    md.setAbnormalType(info.abnormalType);
                    return md;
                })
                .collect(Collectors.toList());
    }

    private List<WorkloadGroupData.MonthData> aggregateGroupData(
            List<CurGzlTableGroup> data, String granularity) {

        Map<String, List<CurGzlTableGroup>> grouped = data.stream()
                .collect(Collectors.groupingBy(
                        item -> getPeriodKey(item.getTjDate(), granularity),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 提取各周期 zl 值用于 EWMA 异常检测
        List<Integer> zlValues = grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getValue().stream().mapToInt(item -> Optional.ofNullable(item.getZl()).orElse(0)).sum())
                .collect(Collectors.toList());
        List<AbnormalInfo> abnormalInfos = detectEwmaAnomalyFull(zlValues);

        final List<AbnormalInfo> infoList = abnormalInfos;
        final int[] idx = {0};
        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    WorkloadGroupData.MonthData md = new WorkloadGroupData.MonthData();
                    md.setPeriod(entry.getKey());
                    List<CurGzlTableGroup> items = entry.getValue();
                    md.setZl(items.stream().mapToInt(item -> Optional.ofNullable(item.getZl()).orElse(0)).sum());
                    md.setJa(items.stream().mapToInt(item -> Optional.ofNullable(item.getJa()).orElse(0)).sum());
                    md.setCkJsl(items.stream().mapToInt(item -> Optional.ofNullable(item.getCkJsl()).orElse(0)).sum());
                    md.setDsTjl(items.stream().mapToInt(item -> Optional.ofNullable(item.getDsTjl()).orElse(0)).sum());
                    AbnormalInfo info = infoList.get(idx[0]++);
                    md.setIsAbnormal(info.isAbnormal);
                    md.setAbnormalType(info.abnormalType);
                    return md;
                })
                .collect(Collectors.toList());
    }

    private List<WorkloadEmpData.MonthData> aggregateRyData(
            List<CurGzlTableRy> data, String granularity) {

        Map<String, List<CurGzlTableRy>> grouped = data.stream()
                .collect(Collectors.groupingBy(
                        item -> getPeriodKey(item.getTjDate(), granularity),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // ========== EWMA 控制图异常检测（基于稳健统计：MAD）============
        // 参数：λ=0.2（平滑系数），k=3.0（控制限乘数）
        final double lambda = 0.2;
        final double k = 3.0;

        // 用前半部分数据建立基准（避开后期可能的异常值），后半部分用于检测
        int total = data.size();
        int baselineEnd = total / 2;  // 前半部分算基准
        List<CurGzlTableRy> baselineData = data.subList(0, baselineEnd);
        List<CurGzlTableRy> detectData = data.subList(baselineEnd, total);

        // 用基准期数据计算中位数和 MAD
        List<Integer> baselineVals = baselineData.stream().map(CurGzlTableRy::getZl).collect(Collectors.toList());
        double mu = baselineVals.stream().mapToInt(Integer::intValue).average().orElse(0);

        // 计算 MAD：对每个值取 |x - median|，取中位数
        List<Double> absDevs = new ArrayList<>();
        double median = calculateMedian(baselineVals);
        for (int v : baselineVals) absDevs.add(Math.abs(v - median));
        double mad = calculateMedianDouble(absDevs);
        // MAD 转换为标准差等价：sigma_mad = 1.483 * MAD（正态分布假设下）
        double sigmaMad = mad * 1.483;
        // 标准差（用于 MAD 过小时回退）
        double sigma = calculateStd(baselineVals.stream().map(Integer::intValue).collect(Collectors.toList()), mu);
        // MAD 过小时（基准期数据太均匀），回退到标准差
        final double sigmaEff = sigmaMad < 0.5 * sigma ? sigma : sigmaMad;

        // 计算 EWMA 控制限（使用 sigma_mad）
        double controlLimit = sigmaEff > 0 ? k * sigmaEff * Math.sqrt(lambda / (2 - lambda)) : 0;

        logger.info("[aggregateRyData] 员工={}, 总周期={}, 基准期={}, mu={}, MAD={}, sigma_eff={}, UCL={}",
                data.isEmpty() ? "N/A" : data.get(0).getUserName(),
                total, baselineEnd,
                String.format("%.2f", mu), String.format("%.2f", mad),
                String.format("%.2f", sigmaEff), String.format("%.2f", controlLimit));

        // 初始化 EWMA 值（从基准期均值开始），用数组包装以在 lambda 内修改
        final double[] ewmaHolder = { mu };

        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    WorkloadEmpData.MonthData md = new WorkloadEmpData.MonthData();
                    md.setPeriod(entry.getKey());
                    List<CurGzlTableRy> items = entry.getValue();
                    md.setZl(items.stream().mapToInt(item -> Optional.ofNullable(item.getZl()).orElse(0)).sum());
                    md.setJa(items.stream().mapToInt(item -> Optional.ofNullable(item.getJa()).orElse(0)).sum());
                    md.setCkJsl(items.stream().mapToInt(item -> Optional.ofNullable(item.getCkJsl()).orElse(0)).sum());
                    md.setDsTjl(items.stream().mapToInt(item -> Optional.ofNullable(item.getDsTjl()).orElse(0)).sum());

                    int currentZl = md.getZl();

                    // ========== EWMA 更新 ==========
                    ewmaHolder[0] = lambda * currentZl + (1 - lambda) * ewmaHolder[0];

                    // ========== 异常判定（基于稳健 MAD 标准差）============
                    // Shewhart：|z - μ| > 3 * sigma_eff（仅在检测期判定）
                    boolean inDetectPhase = detectData.stream()
                            .anyMatch(d -> getPeriodKey(d.getTjDate(), granularity).equals(entry.getKey()));
                    boolean shewhartAbnormal = inDetectPhase && sigmaEff > 0
                            && Math.abs(currentZl - mu) > 3 * sigmaEff;
                    // EWMA：|EWMA_t - μ| > controlLimit（仅在检测期判定）
                    boolean ewmaAbnormal = inDetectPhase && controlLimit > 0
                            && Math.abs(ewmaHolder[0] - mu) > controlLimit;

                    boolean isAbnormal = shewhartAbnormal || ewmaAbnormal;
                    md.setIsAbnormal(isAbnormal);

                    // 异常类型：Shewhart 超限 = 尖峰（突发），EWMA 超限 = 趋势漂移
                    if (isAbnormal) {
                        md.setAbnormalType(shewhartAbnormal ? "spike" : "trend");
                    }

                    if (inDetectPhase) {
                        logger.info("[EWMA] period={}, zl={}, ewma={}, shewhart={}, ewmaAbn={}, abnormal={}",
                                entry.getKey(), currentZl, String.format("%.2f", ewmaHolder[0]),
                                shewhartAbnormal, ewmaAbnormal, isAbnormal);
                    }

                    return md;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据粒度获取时间区间标识
     * month: "2026-01"
     * week: "2026-W01"（该周周一日期）
     * day: "2026-01-15"
     */
    private String getPeriodKey(String tjDate, String granularity) {
        // FB-06/FB-13：显式校验输入，非法日期 fail-fast，不再返回垃圾值污染分组键
        if (tjDate == null || tjDate.length() < 10) {
            throw new IllegalArgumentException("无效的统计日期: " + tjDate);
        }
        LocalDate date;
        try {
            date = LocalDate.parse(tjDate.substring(0, 10));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("无效的统计日期: " + tjDate, e);
        }
        DateTimeFormatter df;

        switch (granularity) {
            case "month":
                df = DateTimeFormatter.ofPattern("yyyy-MM");
                return date.format(df);
            case "week":
                // 计算该日期对应的周一日期
                LocalDate monday = date.with(DayOfWeek.MONDAY);
                return monday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "day":
            default:
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    private double calculateStd(List<Integer> values, double mean) {
        if (values.size() < 2) return 0;
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / values.size();
        return Math.sqrt(variance);
    }

    /** 计算 Integer 列表的中位数 */
    private double calculateMedian(List<Integer> values) {
        if (values == null || values.isEmpty()) return 0;
        List<Integer> sorted = values.stream().sorted().collect(Collectors.toList());
        int n = sorted.size();
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        } else {
            return sorted.get(n / 2);
        }
    }

    /** 计算 Double 列表的中位数 */
    private double calculateMedianDouble(List<Double> values) {
        if (values == null || values.isEmpty()) return 0;
        List<Double> sorted = values.stream().sorted().collect(Collectors.toList());
        int n = sorted.size();
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        } else {
            return sorted.get(n / 2);
        }
    }

    // ==================== EWMA 异常检测结果 ====================
    private static class AbnormalInfo {
        boolean isAbnormal;
        String abnormalType; // null / "spike" / "trend"
        AbnormalInfo(boolean isAbnormal, String abnormalType) {
            this.isAbnormal = isAbnormal;
            this.abnormalType = abnormalType;
        }
    }

    /**
     * 基于 MAD 的 EWMA 控制图异常检测（通用版）
     * 前半部分为基准期，后半部分为检测期
     */
    private List<AbnormalInfo> detectEwmaAnomalyFull(List<Integer> zlValues) {
        List<AbnormalInfo> results = new ArrayList<>();
        if (zlValues == null || zlValues.isEmpty()) return results;

        int total = zlValues.size();
        int baselineEnd = total / 2;
        double lambda = 0.2;
        double k = 3.0;

        // 用基准期数据计算 mu 和 sigma_eff
        List<Integer> baselineVals = zlValues.subList(0, baselineEnd);
        double mu = baselineVals.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<Double> absDevs = new ArrayList<>();
        double median = calculateMedian(baselineVals);
        for (int v : baselineVals) absDevs.add(Math.abs(v - median));
        double mad = calculateMedianDouble(absDevs);
        double sigmaMad = mad * 1.483;
        double sigma = calculateStd(baselineVals.stream().map(Integer::intValue).collect(Collectors.toList()), mu);
        final double sigmaEff = sigmaMad < 0.5 * sigma ? sigma : sigmaMad;
        double controlLimit = sigmaEff > 0 ? k * sigmaEff * Math.sqrt(lambda / (2 - lambda)) : 0;

        // 初始化 EWMA
        final double[] ewmaHolder = { mu };

        for (int i = 0; i < total; i++) {
            int zl = zlValues.get(i);
            ewmaHolder[0] = lambda * zl + (1 - lambda) * ewmaHolder[0];
            boolean inDetectPhase = i >= baselineEnd;
            boolean shewhartAbnormal = inDetectPhase && sigmaEff > 0
                    && Math.abs(zl - mu) > 3 * sigmaEff;
            boolean ewmaAbnormal = inDetectPhase && controlLimit > 0
                    && Math.abs(ewmaHolder[0] - mu) > controlLimit;
            boolean isAbnormal = shewhartAbnormal || ewmaAbnormal;
            results.add(new AbnormalInfo(isAbnormal, isAbnormal ? (shewhartAbnormal ? "spike" : "trend") : null));
        }
        return results;
    }
}