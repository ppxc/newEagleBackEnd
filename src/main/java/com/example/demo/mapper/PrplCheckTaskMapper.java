package com.example.demo.mapper;

import com.example.demo.entity.PrplCheckTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PrplCheckTaskMapper {

    PrplCheckTask selectById(Long id);

    List<PrplCheckTask> getAllTasksByDate(String date);

    // 取指定日期所有坐标行（应用层做 round + 聚合）
    List<Map<String, Object>> getHeatDataByDate(String date);

    // 统计指定日期的数据总量
    int countTasksByDate(String date);

    // 仅统计"缺经纬度 / 落在成都区域外"且 checksite 非空、需异步地理编码的任务
    int countTasksNeedingGeocode(String date);
}