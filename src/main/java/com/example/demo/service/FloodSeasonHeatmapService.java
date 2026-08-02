package com.example.demo.service;

import com.example.demo.entity.HeatData;
import com.example.demo.entity.HotPoint;

import java.util.List;

public interface FloodSeasonHeatmapService {
    List<HeatData> getHeatData(String date);
    List<HeatData> getAllHeatData();

    /**
     * 返回原始事故点(每点一个 marker),带密度着色档位。
     * date 为空时返回全量;非空时按 reportdate 过滤。
     * 不影响 /api/rain/hotmap 的聚合热力接口。
     */
    List<HotPoint> getHotPoints(String date);
}