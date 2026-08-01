package com.example.demo.controller;

import com.example.demo.entity.HeatData;
import com.example.demo.entity.HotPoint;
import com.example.demo.entity.Result;
import com.example.demo.service.FloodSeasonHeatmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/rain")
public class FloodSeasonHeatmapController {

    private static final Logger log = LoggerFactory.getLogger(FloodSeasonHeatmapController.class);

    @Autowired
    private FloodSeasonHeatmapService floodSeasonHeatmapService;

    @GetMapping("/hotmap")
    public Result<List<HeatData>> getHotmapData(
            @RequestParam(required = false) String date
    ) {
        try {
            List<HeatData> data;
            if (date == null || date.isEmpty()) {
                data = floodSeasonHeatmapService.getAllHeatData();
            } else {
                data = floodSeasonHeatmapService.getHeatData(date);
            }
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取热力图数据失败", e);
            return Result.error("获取热力图数据失败", e);
        }
    }

    /**
     * 新增:返回每个原始事故点 + 密度着色档位,供前端以 marker 形式渲染。
     * 与 /hotmap(聚合热力接口)并行存在,不互相影响。
     */
    @GetMapping("/hotPoints")
    public Result<List<HotPoint>> getHotPoints(
            @RequestParam(required = false) String date
    ) {
        try {
            List<HotPoint> data = floodSeasonHeatmapService.getHotPoints(date);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取热力点数据失败", e);
            return Result.error("获取热力点数据失败", e);
        }
    }
}
