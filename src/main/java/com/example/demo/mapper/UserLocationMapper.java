package com.example.demo.mapper;

import com.example.demo.entity.UserLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserLocationMapper {

    UserLocation selectById(Long id);

    List<UserLocation> selectAll();

    List<UserLocation> getLatestLocationsByDate(
            @Param("dateStr") String dateStr,
            @Param("groupscode") String groupscode,
            @Param("keyword") String keyword
    );

    List<UserLocation> getUserLocationsByDate(@Param("usercode") String usercode, @Param("dateStr") String dateStr);
}