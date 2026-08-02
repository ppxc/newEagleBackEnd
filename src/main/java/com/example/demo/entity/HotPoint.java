package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 汛期驾驶舱热力点 — 用于在地图上以 marker 形式呈现的单个事故点。
 *
 * <p>区别于 {@link HeatData}(聚合后的簇中心 + count),
 * 本实体携带原始事故点位置与详情,供前端点击 marker 时弹详情面板。
 *
 * <p>color 字段是 Java 层根据同网格 count 算出的着色档位(0..4):
 * <ul>
 *   <li>0 — 蓝:该网格只有 1 条记录(孤立点)</li>
 *   <li>1 — 黄:2 条</li>
 *   <li>2 — 橙:3-5 条</li>
 *   <li>3 — 红:6-15 条</li>
 *   <li>4 — 深红:>15 条</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotPoint {
    private BigDecimal lat;
    private BigDecimal lng;
    private String reportDate;
    private String damageAddress;
    private String checkAddress;
    private Integer color;
}