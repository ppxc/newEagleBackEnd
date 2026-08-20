package com.example.demo.mapper;

import com.example.demo.entity.CurGzlTableRy;
import com.example.demo.entity.CurGzlTableBm;
import com.example.demo.entity.CurGzlTableGroup;
import com.example.demo.entity.CurGzlTableRs;
import com.example.demo.entity.AcdZhouqiQs;
import com.example.demo.entity.AcdZhouqiBm;
import com.example.demo.entity.AcdZhpflKhq;
import com.example.demo.entity.AcdPacllBm;
import com.example.demo.entity.AcdPacllXz;
import com.example.demo.entity.AcdPacllRy;
import com.example.demo.entity.AcdPflsgnZgs;
import com.example.demo.entity.AcdPflsgnKhq;
import com.example.demo.entity.AcdPflsgnXny;
import com.example.demo.entity.AcdAnjunCxZgs;
import com.example.demo.entity.AcdAnjunCxKhq;
import com.example.demo.entity.AcdAnjunCxXny;
import com.example.demo.entity.AcdChakanYear;
import com.example.demo.entity.AcdDingsunTjlYear;
import com.example.demo.entity.AcdDingsunWclYear;
import com.example.demo.entity.AcdCkDswcYear;
import com.example.demo.entity.AcdDingsunZflYear;
import com.example.demo.entity.AcdLisuanYear;
import com.example.demo.entity.AcdRsGzlYear;
import com.example.demo.entity.AcdChakanMonth;
import com.example.demo.entity.AcdCkDswcMonth;
import com.example.demo.entity.AcdDingsunTjlMonth;
import com.example.demo.entity.AcdDingsunWclMonth;
import com.example.demo.entity.AcdDingsunZflMonth;
import com.example.demo.entity.AcdRsGzlMonth;
import com.example.demo.entity.AcdLisuanMonth;
import com.example.demo.entity.AcdBaLaJaWjPk;
import com.example.demo.entity.AcdZhouqiRy;
import com.example.demo.entity.AcdJieanlBm;
import com.example.demo.entity.AcdJieanlRy;
import com.example.demo.entity.AcdPacllCxZgs;
import com.example.demo.entity.AcdLingjieRy;
import com.example.demo.entity.AcdLingjieBm;
import com.example.demo.entity.AcdLingjieGroups;
import com.example.demo.entity.AcdLingjieZxzt;
import com.example.demo.entity.AcdPflsgnSyxz;
import com.example.demo.entity.AcdPflsgnKhqZgs;
import com.example.demo.entity.AcdPflsgnSyxzZgs;
import com.example.demo.entity.AcdPflsgnPpZgs;
import com.example.demo.entity.AcdPflbdnZgs;
import com.example.demo.entity.AcdPflbdnKhq;
import com.example.demo.entity.AcdPflbdnSyxz;
import com.example.demo.entity.AcdPflbdnXny;
import com.example.demo.entity.AcdPflbdnSyxzZgs;
import com.example.demo.entity.AcdPflbdnKhqZgs;
import com.example.demo.entity.AcdPflbdnXnyZgs;
import com.example.demo.entity.AcdPflbdnPpZgs;
import com.example.demo.entity.AcdWjxs;
import com.example.demo.entity.AcdZhpflXz;
import com.example.demo.entity.AcdZgsCbb;
import com.example.demo.entity.AcdWxdwGjzb;
import com.example.demo.entity.AcdChejunRy;
import com.example.demo.entity.AcdChejunClbm;
import com.example.demo.entity.AcdChejunBm;
import com.example.demo.entity.AcdChejunSgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;


@Mapper
public interface ReportTableMapper{

    String getMaxTjDateByTable(@Param("tableName") String tableName);

    /**
     * 按表名 + 业务标志（jaflag 值）取最大统计日期。
     * 用于同一张表被多个报表共用、按 jaflag 字段区分的场合（如 acd_rs_gzl_year）。
     * XML 中硬编码列名 jaflag，传参只需要表名 + jaflag 值。
     */
    String getMaxTjDateByTableAndFlag(@Param("tableName") String tableName,
                                      @Param("jaflag") String jaflag);

    List<CurGzlTableRy> getCurGzlData(@Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("comName") String comName,
                                      @Param("groups") String groups,
                                      @Param("userName") String userName);

    List<CurGzlTableBm> getCurGzlDataBm(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName
    );

    List<CurGzlTableGroup> getCurGzlDataGroup(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("groups") String groups
    );

    List<CurGzlTableRs> getCurGzlDataRs(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName
    );

    // ==================== 新增表 ====================

    /** 周期-市公司 */
    List<AcdZhouqiQs> getZhouqiQsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 周期-部门 */
    List<AcdZhouqiBm> getZhouqiBmData(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname
    );

    /** 综合赔付率-客户群 */
    List<AcdZhpflKhq> getZhpflKhqData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 车险结案率-部门 */
    List<AcdPacllBm> getPacllBmData(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname
    );

    /** 车险结案率-小组 */
    List<AcdPacllXz> getPacllXzData(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("groups") String groups
    );

    /** 车险结案率-人员 */
    List<AcdPacllRy> getPacllRyData(
            @Param("tjDate") String tjDate,
            @Param("bm") String bm,
            @Param("groups") String groups,
            @Param("username") String username
    );

    // ==================== 事故年赔付率 ====================

    /** 事故年赔付率-支公司 */
    List<AcdPflsgnZgs> getPflsgnZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 事故年赔付率-客户群 */
    List<AcdPflsgnKhq> getPflsgnKhqData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 事故年赔付率-新能源 */
    List<AcdPflsgnXny> getPflsgnXnyData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    // ==================== 案均赔款 ====================

    /** 案均赔款-支公司（车险） */
    List<AcdAnjunCxZgs> getAnjunCxZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 案均赔款-客户群（车险） */
    List<AcdAnjunCxKhq> getAnjunCxKhqData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 案均赔款-新能源（车险） */
    List<AcdAnjunCxXny> getAnjunCxXnyData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    // ==================== 年度每月系列 ====================

    /** 查勘量-年度每月 */
    List<AcdChakanYear> getChakanYearData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 定损提交量-年度每月 */
    List<AcdDingsunTjlYear> getDingsunTjlYearData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 定损完成量-年度每月 */
    List<AcdDingsunWclYear> getDingsunWclYearData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 查勘量+定损完成-年度每月 */
    List<AcdCkDswcYear> getCkDswcYearData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 定损支付量-年度每月 */
    List<AcdDingsunZflYear> getDingsunZflYearData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 理算量-年度每月 */
    List<AcdLisuanYear> getLisuanYearData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 人伤跟踪量-年度每月 */
    List<AcdRsGzlYear> getRsGzlYearData(
            @Param("tjDate") String tjDate
    );

    /** 人伤调解量-年度每月 */
    List<AcdRsGzlYear> getRsTjlYearData(
            @Param("tjDate") String tjDate
    );

    /** 查勘量-月度每日 */
    List<AcdChakanMonth> getChakanMonthData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 查勘量+定损完成-月度每日 */
    List<AcdCkDswcMonth> getCkDswcMonthData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 定损提交量-月度每日 */
    List<AcdDingsunTjlMonth> getDingsunTjlMonthData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 定损完成量-月度每日 */
    List<AcdDingsunWclMonth> getDingsunWclMonthData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 定损支付量-月度每日 */
    List<AcdDingsunZflMonth> getDingsunZflMonthData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    /** 人伤跟踪量-月度每日（jaflag=后续跟踪） */
    List<AcdRsGzlMonth> getRsGzlMonthData(@Param("tjDate") String tjDate);

    /** 人伤调解量-月度每日（jaflag=人伤调解） */
    List<AcdRsGzlMonth> getRsTjlMonthData(@Param("tjDate") String tjDate);

    /** 理算量-月度每日 */
    List<AcdLisuanMonth> getLisuanMonthData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs
    );

    // ==================== 分页查询（laoxiao 9 + chakan_month） ====================

    /** 查勘量-年度每月 - 分页 */
    List<AcdChakanYear> getChakanYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countChakanYear(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 定损提交量-年度每月 - 分页 */
    List<AcdDingsunTjlYear> getDingsunTjlYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countDingsunTjlYear(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 定损完成量-年度每月 - 分页 */
    List<AcdDingsunWclYear> getDingsunWclYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countDingsunWclYear(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 查勘量+定损完成-年度每月 - 分页 */
    List<AcdCkDswcYear> getCkDswcYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countCkDswcYear(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 定损支付量-年度每月 - 分页 */
    List<AcdDingsunZflYear> getDingsunZflYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countDingsunZflYear(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 理算量-年度每月 - 分页 */
    List<AcdLisuanYear> getLisuanYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countLisuanYear(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 人伤跟踪量-年度每月 - 分页 */
    List<AcdRsGzlYear> getRsGzlYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countRsGzlYear(@Param("tjDate") String tjDate);

    /** 人伤调解量-年度每月 - 分页 */
    List<AcdRsGzlYear> getRsTjlYearDataPage(
            @Param("tjDate") String tjDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countRsTjlYear(@Param("tjDate") String tjDate);

    /** 查勘量-月度每日 - 分页 */
    List<AcdChakanMonth> getChakanMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countChakanMonth(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 查勘量+定损完成-月度每日 - 分页 */
    List<AcdCkDswcMonth> getCkDswcMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countCkDswcMonth(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 定损提交量-月度每日 - 分页 */
    List<AcdDingsunTjlMonth> getDingsunTjlMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countDingsunTjlMonth(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 定损完成量-月度每日 - 分页 */
    List<AcdDingsunWclMonth> getDingsunWclMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countDingsunWclMonth(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 定损支付量-月度每日 - 分页 */
    List<AcdDingsunZflMonth> getDingsunZflMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countDingsunZflMonth(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 人伤跟踪量-月度每日 - 分页 */
    List<AcdRsGzlMonth> getRsGzlMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countRsGzlMonth(@Param("tjDate") String tjDate);

    /** 人伤调解量-月度每日 - 分页 */
    List<AcdRsGzlMonth> getRsTjlMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countRsTjlMonth(@Param("tjDate") String tjDate);

    /** 理算量-月度每日 - 分页 */
    List<AcdLisuanMonth> getLisuanMonthDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countLisuanMonth(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    // ==================== 分页查询（lpcenter/efficiency 16 端点） ====================

    /** 人员日工作量 - 分页 */
    List<CurGzlTableRy> getCurGzlDataPage(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("groups") String groups,
            @Param("userName") String userName,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countCurGzlData(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("groups") String groups,
            @Param("userName") String userName);

    /** 部门日工作量 - 分页 */
    List<CurGzlTableBm> getCurGzlDataBmPage(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countCurGzlDataBm(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName);

    /** 小组日工作量 - 分页 */
    List<CurGzlTableGroup> getCurGzlDataGroupPage(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("groups") String groups,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countCurGzlDataGroup(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("groups") String groups);

    /** 人伤日工作量 - 分页 */
    List<CurGzlTableRs> getCurGzlDataRsPage(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countCurGzlDataRs(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("comName") String comName);

    /** 周期-市公司 - 分页 */
    List<AcdZhouqiQs> getZhouqiQsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countZhouqiQs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 周期-部门 - 分页 */
    List<AcdZhouqiBm> getZhouqiBmDataPage(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countZhouqiBm(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname);

    /** 综合赔付率-客户群 - 分页 */
    List<AcdZhpflKhq> getZhpflKhqDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countZhpflKhq(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 车险结案率-部门 - 分页 */
    List<AcdPacllBm> getPacllBmDataPage(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPacllBm(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname);

    /** 车险结案率-小组 - 分页 */
    List<AcdPacllXz> getPacllXzDataPage(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("groups") String groups,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPacllXz(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("groups") String groups);

    /** 车险结案率-人员 - 分页 */
    List<AcdPacllRy> getPacllRyDataPage(
            @Param("tjDate") String tjDate,
            @Param("bm") String bm,
            @Param("groups") String groups,
            @Param("username") String username,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPacllRy(
            @Param("tjDate") String tjDate,
            @Param("bm") String bm,
            @Param("groups") String groups,
            @Param("username") String username);

    /** 事故年赔付率-支公司 - 分页 */
    List<AcdPflsgnZgs> getPflsgnZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 事故年赔付率-客户群 - 分页 */
    List<AcdPflsgnKhq> getPflsgnKhqDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnKhq(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 事故年赔付率-新能源 - 分页 */
    List<AcdPflsgnXny> getPflsgnXnyDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnXny(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 案均赔款-支公司（车险）- 分页 */
    List<AcdAnjunCxZgs> getAnjunCxZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countAnjunCxZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 案均赔款-客户群（车险）- 分页 */
    List<AcdAnjunCxKhq> getAnjunCxKhqDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countAnjunCxKhq(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 案均赔款-新能源（车险）- 分页 */
    List<AcdAnjunCxXny> getAnjunCxXnyDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countAnjunCxXny(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    // ==================== 2026-06 新增 7 张表 ====================

    /** 车险案件量-承保地 */
    List<AcdBaLaJaWjPk> getBaLaJaWjPkData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdBaLaJaWjPk> getBaLaJaWjPkDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countBaLaJaWjPk(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 周期-人员 */
    List<AcdZhouqiRy> getZhouqiRyData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdZhouqiRy> getZhouqiRyDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countZhouqiRy(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 每日结案量-部门实时（无 comname 筛选） */
    List<AcdJieanlBm> getJieanlBmData(
            @Param("tjDate") String tjDate);
    List<AcdJieanlBm> getJieanlBmDataPage(
            @Param("tjDate") String tjDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countJieanlBm(
            @Param("tjDate") String tjDate);

    /** 每日结案量-人员实时 */
    List<AcdJieanlRy> getJieanlRyData(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname);
    List<AcdJieanlRy> getJieanlRyDataPage(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countJieanlRy(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname);

    /** 车险结案率-支公司 */
    List<AcdPacllCxZgs> getPacllCxZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdPacllCxZgs> getPacllCxZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPacllCxZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 零结案-人员（tjdate + groups + username） */
    List<AcdLingjieRy> getLingjieRyData(
            @Param("tjDate") String tjDate,
            @Param("groups") String groups,
            @Param("username") String username);
    List<AcdLingjieRy> getLingjieRyDataPage(
            @Param("tjDate") String tjDate,
            @Param("groups") String groups,
            @Param("username") String username,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countLingjieRy(
            @Param("tjDate") String tjDate,
            @Param("groups") String groups,
            @Param("username") String username);

    /** 零结案-部门（tjdate only） */
    List<AcdLingjieBm> getLingjieBmData(@Param("tjDate") String tjDate);

    /** 零结案-小组（tjdate + comname + groups） */
    List<AcdLingjieGroups> getLingjieGroupsData(
            @Param("tjDate") String tjDate,
            @Param("comname") String comname,
            @Param("groups") String groups);

    /** 零结案-中心整体（tjdate only） */
    List<AcdLingjieZxzt> getLingjieZxztData(@Param("tjDate") String tjDate);

    // ==================== 成本管控新增 14 张表 (2026-06) ====================

    /** 事故年赔付率-使用性质 */
    List<AcdPflsgnSyxz> getPflsgnSyxzData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("usenaturename") String usenaturename);
    List<AcdPflsgnSyxz> getPflsgnSyxzDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("usenaturename") String usenaturename,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnSyxz(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("usenaturename") String usenaturename);

    /** 事故年赔付率-支公司-客户群 */
    List<AcdPflsgnKhqZgs> getPflsgnKhqZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("khq") String khq);
    List<AcdPflsgnKhqZgs> getPflsgnKhqZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("khq") String khq,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnKhqZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("khq") String khq);

    /** 事故年赔付率-支公司-使用性质 */
    List<AcdPflsgnSyxzZgs> getPflsgnSyxzZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("usenaturename") String usenaturename);
    List<AcdPflsgnSyxzZgs> getPflsgnSyxzZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("usenaturename") String usenaturename,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnSyxzZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("usenaturename") String usenaturename);

    /** 事故年赔付率-支公司-品牌 */
    List<AcdPflsgnPpZgs> getPflsgnPpZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("brandname") String brandname);
    List<AcdPflsgnPpZgs> getPflsgnPpZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("brandname") String brandname,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflsgnPpZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("brandname") String brandname);

    /** 保单年赔付率-支公司 */
    List<AcdPflbdnZgs> getPflbdnZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdPflbdnZgs> getPflbdnZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 保单年赔付率-客户群 */
    List<AcdPflbdnKhq> getPflbdnKhqData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("khq") String khq);
    List<AcdPflbdnKhq> getPflbdnKhqDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("khq") String khq,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnKhq(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("khq") String khq);

    /** 保单年赔付率-使用性质 */
    List<AcdPflbdnSyxz> getPflbdnSyxzData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("usenaturename") String usenaturename);
    List<AcdPflbdnSyxz> getPflbdnSyxzDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("usenaturename") String usenaturename,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnSyxz(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("usenaturename") String usenaturename);

    /** 保单年赔付率-新能源 */
    List<AcdPflbdnXny> getPflbdnXnyData(
            @Param("tjDate") String tjDate,
            @Param("xnyflag") String xnyflag);
    List<AcdPflbdnXny> getPflbdnXnyDataPage(
            @Param("tjDate") String tjDate,
            @Param("xnyflag") String xnyflag,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnXny(
            @Param("tjDate") String tjDate,
            @Param("xnyflag") String xnyflag);

    /** 保单年赔付率-支公司-使用性质 */
    List<AcdPflbdnSyxzZgs> getPflbdnSyxzZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("usenaturename") String usenaturename);
    List<AcdPflbdnSyxzZgs> getPflbdnSyxzZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("usenaturename") String usenaturename,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnSyxzZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("usenaturename") String usenaturename);

    /** 保单年赔付率-支公司-客户群 */
    List<AcdPflbdnKhqZgs> getPflbdnKhqZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("khq") String khq);
    List<AcdPflbdnKhqZgs> getPflbdnKhqZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("khq") String khq,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnKhqZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("khq") String khq);

    /** 保单年赔付率-支公司-新能源 */
    List<AcdPflbdnXnyZgs> getPflbdnXnyZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("xnyflag") String xnyflag);
    List<AcdPflbdnXnyZgs> getPflbdnXnyZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("xnyflag") String xnyflag,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnXnyZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("xnyflag") String xnyflag);

    /** 保单年赔付率-支公司-品牌 */
    List<AcdPflbdnPpZgs> getPflbdnPpZgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("brandname") String brandname);
    List<AcdPflbdnPpZgs> getPflbdnPpZgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("brandname") String brandname,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countPflbdnPpZgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("comname") String comname,
            @Param("brandname") String brandname);

    /** 综合赔付率-险种 */
    List<AcdZhpflXz> getZhpflXzData(
            @Param("tjDate") String tjDate,
            @Param("xl") String xl);
    List<AcdZhpflXz> getZhpflXzDataPage(
            @Param("tjDate") String tjDate,
            @Param("xl") String xl,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countZhpflXz(
            @Param("tjDate") String tjDate,
            @Param("xl") String xl);

    // ==================== 维修单位 (2026-06) ====================
    /** 各支公司产保比 */
    List<AcdZgsCbb> getZgsCbbData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdZgsCbb> getZgsCbbDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countZgsCbb(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 维修单位关键指标 */
    List<AcdWxdwGjzb> getWxdwGjzbData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("repairfactoryname") String repairfactoryname);
    List<AcdWxdwGjzb> getWxdwGjzbDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("repairfactoryname") String repairfactoryname,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countWxdwGjzb(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("repairfactoryname") String repairfactoryname);

    // ==================== 车均定损 (2026-06) ====================
    /** 车均定损-人员 */
    List<AcdChejunRy> getChejunRyData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdChejunRy> getChejunRyDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countChejunRy(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 车均定损-处理部门 */
    List<AcdChejunClbm> getChejunClbmData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdChejunClbm> getChejunClbmDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countChejunClbm(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 车均定损-定损区域 */
    List<AcdChejunBm> getChejunBmData(
            @Param("tjDate") String tjDate,
            @Param("dsqy") String dsqy);
    List<AcdChejunBm> getChejunBmDataPage(
            @Param("tjDate") String tjDate,
            @Param("dsqy") String dsqy,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countChejunBm(
            @Param("tjDate") String tjDate,
            @Param("dsqy") String dsqy);

    /** 车均定损-市公司 */
    List<AcdChejunSgs> getChejunSgsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdChejunSgs> getChejunSgsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countChejunSgs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    /** 未决存量-案件类型 */
    List<AcdWjxs> getWjxsData(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);
    List<AcdWjxs> getWjxsDataPage(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs,
            @Param("offset") int offset,
            @Param("limit") int limit);
    long countWjxs(
            @Param("tjDate") String tjDate,
            @Param("comnameSgs") String comnameSgs);

    // ==================== 下拉去重端点（轻量级） ====================

    /**
     * 取市公司（comname_sgs）去重名单，用于搜索下拉。
     *
     * 三态日期过滤（由 Service 层保证至少一个生效，未传则返回全表去重）：
     *   - 单日：tjDate 非空 → tjdate = #{tjDate}
     *   - 区间：startDate + endDate 非空 → tjdate 范围
     *   - 共享表分段：flagValue 非空 → jaflag = #{flagValue}
     *
     * {@code tableName} 为字符串拼接（SQL 标识符），调用方（Service 层）必须先过
     * {@code TableNames.requireValid} 白名单校验；列名 comname_sgs / jaflag 硬编码，不接受外部输入。
     */
    List<String> getDistinctComnameSgs(
            @Param("tableName") String tableName,
            @Param("tjDate") String tjDate,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("flagValue") String flagValue);

}