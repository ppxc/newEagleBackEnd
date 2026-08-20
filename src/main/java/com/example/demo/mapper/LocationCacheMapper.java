package com.example.demo.mapper;

import com.example.demo.entity.LocationCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LocationCacheMapper {

    int insert(LocationCache locationCache);

    LocationCache selectById(Long id);

    int updateById(LocationCache locationCache);

    LocationCache findByCoordinates(@Param("longitude") Double longitude, @Param("latitude") Double latitude);
    
    LocationCache findValidByCoordinates(@Param("longitude") Double longitude, @Param("latitude") Double latitude, @Param("currentTime") String currentTime);
    
    LocationCache findByAddress(@Param("address") String address);
    
    LocationCache findValidByAddress(@Param("address") String address, @Param("currentTime") String currentTime);
    
    LocationCache findByAddressLike(@Param("addressPattern") String addressPattern);
    
    LocationCache findValidByAddressLike(@Param("addressPattern") String addressPattern, @Param("currentTime") String currentTime);
    
    /**
     * 批量查询有效缓存（根据经纬度列表）
     * @param coordList 经纬度列表，每个元素是一个Map，包含 longitude 和 latitude
     * @param currentTime 当前时间
     * @return 缓存列表
     */
    List<LocationCache> findValidByCoordinatesBatch(@Param("coordList") List<Map<String, Double>> coordList, @Param("currentTime") String currentTime);

    /**
     * 批量查询缓存（不校验过期时间，按经纬度精确匹配）
     */
    List<LocationCache> findByCoordinatesBatch(@Param("coordList") List<Map<String, Double>> coordList);

    /**
     * 批量插入缓存
     */
    int insertBatch(@Param("list") List<LocationCache> list);
}