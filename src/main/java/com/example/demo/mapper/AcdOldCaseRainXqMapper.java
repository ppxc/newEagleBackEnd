package com.example.demo.mapper;

import com.example.demo.entity.AcdOldCaseRainXq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AcdOldCaseRainXqMapper {
    List<AcdOldCaseRainXq> selectAll();
    List<AcdOldCaseRainXq> selectByDate(@Param("reportdate") String reportdate);
    List<AcdOldCaseRainXq> selectWithCoordinates();
    List<AcdOldCaseRainXq> selectWithCoordinatesByDate(@Param("reportdate") String reportdate);
}