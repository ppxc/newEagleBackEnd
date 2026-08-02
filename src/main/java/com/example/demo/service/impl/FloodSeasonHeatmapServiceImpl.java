package com.example.demo.service.impl;

import com.example.demo.entity.AcdOldCaseRainXq;
import com.example.demo.entity.HeatData;
import com.example.demo.entity.HotPoint;
import com.example.demo.mapper.AcdOldCaseRainXqMapper;
import com.example.demo.service.FloodSeasonHeatmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FloodSeasonHeatmapServiceImpl implements FloodSeasonHeatmapService {

    private static final Logger logger = LoggerFactory.getLogger(FloodSeasonHeatmapServiceImpl.class);

    private static final int MAX_HEAT_POINTS = 10000;

    private static final double CD_MIN_LNG = 102.5;
    private static final double CD_MAX_LNG = 104.9;
    private static final double CD_MIN_LAT = 30.0;
    private static final double CD_MAX_LAT = 31.5;

    @Autowired
    private AcdOldCaseRainXqMapper acdOldCaseRainXqMapper;

    @Override
    public List<HeatData> getHeatData(String date) {
        logger.info("获取汛期热力图数据: date={}", date);
        List<AcdOldCaseRainXq> records;
        if (date == null || date.isEmpty()) {
            records = acdOldCaseRainXqMapper.selectWithCoordinates();
        } else {
            records = acdOldCaseRainXqMapper.selectByDate(date);
        }
        return buildHeatDataList(records);
    }

    @Override
    public List<HeatData> getAllHeatData() {
        logger.info("获取全部汛期热力图数据");
        List<AcdOldCaseRainXq> records = acdOldCaseRainXqMapper.selectWithCoordinates();
        return buildHeatDataList(records);
    }

    private List<HeatData> buildHeatDataList(List<AcdOldCaseRainXq> records) {
        List<HeatData> result = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return result;
        }

        Map<String, HeatData> groupMap = new HashMap<>();
        for (AcdOldCaseRainXq record : records) {
            BigDecimal latBg = record.getLatitude();
            BigDecimal lngBg = record.getLongitude();
            if (latBg == null || lngBg == null) {
                continue;
            }
            double lat = latBg.setScale(5, RoundingMode.HALF_UP).doubleValue();
            double lng = lngBg.setScale(5, RoundingMode.HALF_UP).doubleValue();

            if (lng < CD_MIN_LNG || lng > CD_MAX_LNG || lat < CD_MIN_LAT || lat > CD_MAX_LAT) {
                continue;
            }

            String key = lat + "," + lng;
            HeatData hd = groupMap.get(key);
            if (hd == null) {
                hd = new HeatData();
                hd.setLat(lat);
                hd.setLng(lng);
                hd.setCount(1);
                groupMap.put(key, hd);
            } else {
                hd.setCount(hd.getCount() + 1);
            }
        }

        result.addAll(groupMap.values());
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));

        if (result.size() > MAX_HEAT_POINTS) {
            logger.warn("热力图数据量超过最大限制 {}，已截断", MAX_HEAT_POINTS);
            return result.subList(0, MAX_HEAT_POINTS);
        }

        logger.info("热力图数据构建完成，共 {} 个坐标点", result.size());
        return result;
    }

    /**
     * 返回每个原始事故点 + 密度着色档位,供前端以 marker 形式渲染。
     *
     * <p>算法:用与 buildHeatDataList 相同的 5 位小数网格聚合得到每个网格的 count,
     * 然后按 count 映射到 0..4 档(蓝/黄/橙/红/深红);每条原始点带它所在网格的 color。
     *
     * @param date 可选日期(YYYY-MM-DD);为空时返回全量,非空时按 reportdate 过滤。
     */
    @Override
    public List<HotPoint> getHotPoints(String date) {
        logger.info("获取汛期热力点(原始 marker 数据) date={}", date);
        List<AcdOldCaseRainXq> records = (date == null || date.trim().isEmpty())
                ? acdOldCaseRainXqMapper.selectWithCoordinates()
                : acdOldCaseRainXqMapper.selectWithCoordinatesByDate(date);
        return buildHotPointList(records);
    }

    private List<HotPoint> buildHotPointList(List<AcdOldCaseRainXq> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        // 第一遍:按网格聚合,统计每个网格的 count
        Map<String, Integer> gridCount = new HashMap<>();
        for (AcdOldCaseRainXq r : records) {
            BigDecimal latBg = r.getLatitude();
            BigDecimal lngBg = r.getLongitude();
            if (latBg == null || lngBg == null) continue;
            double lat = latBg.setScale(5, RoundingMode.HALF_UP).doubleValue();
            double lng = lngBg.setScale(5, RoundingMode.HALF_UP).doubleValue();
            if (lng < CD_MIN_LNG || lng > CD_MAX_LNG
                    || lat < CD_MIN_LAT || lat > CD_MAX_LAT) continue;
            String key = lat + "," + lng;
            gridCount.merge(key, 1, Integer::sum);
        }

        // 第二遍:对每条原始点,带 color 返回
        List<HotPoint> out = new ArrayList<>(records.size());
        for (AcdOldCaseRainXq r : records) {
            BigDecimal latBg = r.getLatitude();
            BigDecimal lngBg = r.getLongitude();
            if (latBg == null || lngBg == null) continue;
            double lat = latBg.setScale(5, RoundingMode.HALF_UP).doubleValue();
            double lng = lngBg.setScale(5, RoundingMode.HALF_UP).doubleValue();
            if (lng < CD_MIN_LNG || lng > CD_MAX_LNG
                    || lat < CD_MIN_LAT || lat > CD_MAX_LAT) continue;

            Integer count = gridCount.get(lat + "," + lng);
            int color = colorBucket(count == null ? 1 : count);

            HotPoint p = new HotPoint();
            p.setLat(latBg);
            p.setLng(lngBg);
            p.setReportDate(r.getReportdate());
            p.setDamageAddress(r.getDamageaddress());
            p.setCheckAddress(r.getCheckaddress());
            p.setColor(color);
            out.add(p);
        }
        logger.info("热力点构建完成，共 {} 个 marker(网格数 {})",
                out.size(), gridCount.size());
        return out;
    }

    /** count → 5 档颜色(0=蓝,1=黄,2=橙,3=红,4=深红) */
    private static int colorBucket(int count) {
        if (count >= 16) return 4;
        if (count >= 6)  return 3;
        if (count >= 3)  return 2;
        if (count >= 2)  return 1;
        return 0;
    }
}