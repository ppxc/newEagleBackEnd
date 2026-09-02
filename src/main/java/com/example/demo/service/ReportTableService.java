package com.example.demo.service;

import com.example.demo.entity.CurGzlTableRy;
import com.example.demo.entity.CurGzlTableBm;
import com.example.demo.entity.CurGzlTableRs;
import com.example.demo.entity.CurGzlTableGroup;
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
import com.example.demo.entity.AcdLingjieGroup;
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
import com.example.demo.entity.PageResult;

import java.util.List;

public interface ReportTableService {
    // 通用获取最大日期
    String getMaxTjDate(String tableName);

    /**
     * 按表名 + 业务标志取最大统计日期（用于共享表按字段分段）。
     */
    String getMaxTjDateByTableAndFlag(String tableName, String flagColumn, String flagValue);

    /**
     * 专用重载：列名固定 jaflag，按 jaflag 值过滤后取 MAX(tjdate)。
     */
    String getMaxTjDateByTableAndFlag(String tableName, String jaflag);

    /**
     * 取市公司（comname_sgs）去重名单，用于搜索下拉。
     *
     * 三态日期过滤（可组合；全空则全表去重，业务上应避免）：
     *   - 单日：tjDate 非空 → tjdate = #{tjDate}
     *   - 区间：startDate + endDate 非空 → tjdate 范围
     *   - 共享表分段：flagValue 非空 → jaflag = #{flagValue}
     *
     * 表名必须在 TableNames 白名单内，否则抛 IllegalArgumentException。
     */
    List<String> getDistinctComnameSgs(String tableName, String tjDate, String startDate, String endDate, String flagValue);

    List<CurGzlTableRy> getCurGzlData(String startDate, String endDate, String comName, String groups, String userName);

    // 新增：按部门统计
    List<CurGzlTableBm> getCurGzlDataBm(String startDate, String endDate, String comName);

    // 小组统计表
    List<CurGzlTableGroup> getCurGzlDataGroup(
            String startDate,
            String endDate,
            String comName,
            String groups
    );

    // 人员/住院门诊统计表（新表）
    List<CurGzlTableRs> getCurGzlDataRs(String startDate, String endDate, String comName);

    // ==================== 新增表接口 ====================

    /** 周期-市公司 */
    List<AcdZhouqiQs> getZhouqiQsData(String tjDate, String comnameSgs);

    /** 周期-部门 */
    List<AcdZhouqiBm> getZhouqiBmData(String tjDate, String comname);

    /** 综合赔付率-客户群 */
    List<AcdZhpflKhq> getZhpflKhqData(String tjDate, String comnameSgs);

    /** 车险结案率-部门 */
    List<AcdPacllBm> getPacllBmData(String tjDate, String comname);

    /** 车险结案率-小组 */
    List<AcdPacllXz> getPacllXzData(String tjDate, String comname, String groups);

    /** 车险结案率-人员 */
    List<AcdPacllRy> getPacllRyData(String tjDate, String bm, String groups, String username);

    // ==================== 事故年赔付率 ====================

    /** 事故年赔付率-支公司 */
    List<AcdPflsgnZgs> getPflsgnZgsData(String tjDate, String comnameSgs);

    /** 事故年赔付率-客户群 */
    List<AcdPflsgnKhq> getPflsgnKhqData(String tjDate, String comnameSgs);

    /** 事故年赔付率-新能源 */
    List<AcdPflsgnXny> getPflsgnXnyData(String tjDate, String comnameSgs);

    // ==================== 案均赔款 ====================

    /** 案均赔款-支公司（车险） */
    List<AcdAnjunCxZgs> getAnjunCxZgsData(String tjDate, String comnameSgs);

    /** 案均赔款-客户群（车险） */
    List<AcdAnjunCxKhq> getAnjunCxKhqData(String tjDate, String comnameSgs);

    /** 案均赔款-新能源（车险） */
    List<AcdAnjunCxXny> getAnjunCxXnyData(String tjDate, String comnameSgs);

    // ==================== 年度每月系列 ====================

    /** 查勘量-年度每月 */
    List<AcdChakanYear> getChakanYearData(String tjDate, String comnameSgs);

    /** 定损提交量-年度每月 */
    List<AcdDingsunTjlYear> getDingsunTjlYearData(String tjDate, String comnameSgs);

    /** 定损完成量-年度每月 */
    List<AcdDingsunWclYear> getDingsunWclYearData(String tjDate, String comnameSgs);

    /** 查勘量+定损完成-年度每月 */
    List<AcdCkDswcYear> getCkDswcYearData(String tjDate, String comnameSgs);

    /** 定损支付量-年度每月 */
    List<AcdDingsunZflYear> getDingsunZflYearData(String tjDate, String comnameSgs);

    /** 理算量-年度每月 */
    List<AcdLisuanYear> getLisuanYearData(String tjDate, String comnameSgs);

    /** 人伤跟踪量-年度每月 */
    List<AcdRsGzlYear> getRsGzlYearData(String tjDate);

    /** 人伤调解量-年度每月 */
    List<AcdRsGzlYear> getRsTjlYearData(String tjDate);

    /** 查勘量-月度每日 */
    List<AcdChakanMonth> getChakanMonthData(String tjDate, String comnameSgs);

    /** 查勘量+定损完成-月度每日 */
    List<AcdCkDswcMonth> getCkDswcMonthData(String tjDate, String comnameSgs);

    /** 定损提交量-月度每日 */
    List<AcdDingsunTjlMonth> getDingsunTjlMonthData(String tjDate, String comnameSgs);

    /** 定损完成量-月度每日 */
    List<AcdDingsunWclMonth> getDingsunWclMonthData(String tjDate, String comnameSgs);

    /** 定损支付量-月度每日 */
    List<AcdDingsunZflMonth> getDingsunZflMonthData(String tjDate, String comnameSgs);

    /** 人伤跟踪量-月度每日 */
    List<AcdRsGzlMonth> getRsGzlMonthData(String tjDate);

    /** 人伤调解量-月度每日 */
    List<AcdRsGzlMonth> getRsTjlMonthData(String tjDate);

    /** 理算量-月度每日 */
    List<AcdLisuanMonth> getLisuanMonthData(String tjDate, String comnameSgs);

    // ==================== 分页查询（laoxiao 9 + chakan_month） ====================

    /** 查勘量-年度每月 - 分页 */
    PageResult<AcdChakanYear> getChakanYearDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 定损提交量-年度每月 - 分页 */
    PageResult<AcdDingsunTjlYear> getDingsunTjlYearDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 定损完成量-年度每月 - 分页 */
    PageResult<AcdDingsunWclYear> getDingsunWclYearDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 查勘量+定损完成-年度每月 - 分页 */
    PageResult<AcdCkDswcYear> getCkDswcYearDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 定损支付量-年度每月 - 分页 */
    PageResult<AcdDingsunZflYear> getDingsunZflYearDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 理算量-年度每月 - 分页 */
    PageResult<AcdLisuanYear> getLisuanYearDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 人伤跟踪量-年度每月 - 分页（无 comnameSgs 筛选） */
    PageResult<AcdRsGzlYear> getRsGzlYearDataPage(String tjDate, int current, int size);

    /** 人伤调解量-年度每月 - 分页（无 comnameSgs 筛选） */
    PageResult<AcdRsGzlYear> getRsTjlYearDataPage(String tjDate, int current, int size);

    /** 查勘量-月度每日 - 分页 */
    PageResult<AcdChakanMonth> getChakanMonthDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 查勘量+定损完成-月度每日 - 分页 */
    PageResult<AcdCkDswcMonth> getCkDswcMonthDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 定损提交量-月度每日 - 分页 */
    PageResult<AcdDingsunTjlMonth> getDingsunTjlMonthDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 定损完成量-月度每日 - 分页 */
    PageResult<AcdDingsunWclMonth> getDingsunWclMonthDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 定损支付量-月度每日 - 分页 */
    PageResult<AcdDingsunZflMonth> getDingsunZflMonthDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 人伤跟踪量-月度每日 - 分页 */
    PageResult<AcdRsGzlMonth> getRsGzlMonthDataPage(String tjDate, int current, int size);

    /** 人伤调解量-月度每日 - 分页 */
    PageResult<AcdRsGzlMonth> getRsTjlMonthDataPage(String tjDate, int current, int size);

    /** 理算量-月度每日 - 分页 */
    PageResult<AcdLisuanMonth> getLisuanMonthDataPage(String tjDate, String comnameSgs, int current, int size);

    // ==================== 分页查询（lpcenter/efficiency 16 端点） ====================

    /** 人员日工作量 - 分页 */
    PageResult<CurGzlTableRy> getCurGzlDataPage(String startDate, String endDate, String comName, String groups, String userName, int current, int size);

    /** 部门日工作量 - 分页 */
    PageResult<CurGzlTableBm> getCurGzlDataBmPage(String startDate, String endDate, String comName, int current, int size);

    /** 小组日工作量 - 分页 */
    PageResult<CurGzlTableGroup> getCurGzlDataGroupPage(String startDate, String endDate, String comName, String groups, int current, int size);

    /** 人伤日工作量 - 分页 */
    PageResult<CurGzlTableRs> getCurGzlDataRsPage(String startDate, String endDate, String comName, int current, int size);

    /** 周期-市公司 - 分页 */
    PageResult<AcdZhouqiQs> getZhouqiQsDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 周期-部门 - 分页 */
    PageResult<AcdZhouqiBm> getZhouqiBmDataPage(String tjDate, String comname, int current, int size);

    /** 综合赔付率-客户群 - 分页 */
    PageResult<AcdZhpflKhq> getZhpflKhqDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 车险结案率-部门 - 分页 */
    PageResult<AcdPacllBm> getPacllBmDataPage(String tjDate, String comname, int current, int size);

    /** 车险结案率-小组 - 分页 */
    PageResult<AcdPacllXz> getPacllXzDataPage(String tjDate, String comname, String groups, int current, int size);

    /** 车险结案率-人员 - 分页 */
    PageResult<AcdPacllRy> getPacllRyDataPage(String tjDate, String bm, String groups, String username, int current, int size);

    /** 事故年赔付率-支公司 - 分页 */
    PageResult<AcdPflsgnZgs> getPflsgnZgsDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 事故年赔付率-客户群 - 分页 */
    PageResult<AcdPflsgnKhq> getPflsgnKhqDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 事故年赔付率-新能源 - 分页 */
    PageResult<AcdPflsgnXny> getPflsgnXnyDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 案均赔款-支公司（车险）- 分页 */
    PageResult<AcdAnjunCxZgs> getAnjunCxZgsDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 案均赔款-客户群（车险）- 分页 */
    PageResult<AcdAnjunCxKhq> getAnjunCxKhqDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 案均赔款-新能源（车险）- 分页 */
    PageResult<AcdAnjunCxXny> getAnjunCxXnyDataPage(String tjDate, String comnameSgs, int current, int size);

    // ==================== 2026-06 新增 7 张表 ====================

    /** 车险案件量-承保地 */
    List<AcdBaLaJaWjPk> getBaLaJaWjPkData(String tjDate, String comnameSgs);
    PageResult<AcdBaLaJaWjPk> getBaLaJaWjPkDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 周期-人员 */
    List<AcdZhouqiRy> getZhouqiRyData(String tjDate, String comnameSgs);
    PageResult<AcdZhouqiRy> getZhouqiRyDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 每日结案量-部门实时 */
    List<AcdJieanlBm> getJieanlBmData(String tjDate);
    PageResult<AcdJieanlBm> getJieanlBmDataPage(String tjDate, int current, int size);

    /** 每日结案量-人员实时 */
    List<AcdJieanlRy> getJieanlRyData(String tjDate, String comname);
    PageResult<AcdJieanlRy> getJieanlRyDataPage(String tjDate, String comname, int current, int size);

    /** 车险结案率-支公司 */
    List<AcdPacllCxZgs> getPacllCxZgsData(String tjDate, String comnameSgs);
    PageResult<AcdPacllCxZgs> getPacllCxZgsDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 零结案-人员 */
    List<AcdLingjieRy> getLingjieRyData(String tjDate, String groups, String username);
    PageResult<AcdLingjieRy> getLingjieRyDataPage(String tjDate, String groups, String username, int current, int size);

    /**
     * 零结案-小组（合成：bm + groups + zxzt 三表内存合并）
     * 顺序：bm → groups → zxzt
     * bm 行 groups 留空；zxzt 行 groups 留空
     * zxzt 字段映射: lingjie_num_1_3→ljl1_3 / lingjie_num_month→ljl4 / lingjie_num_week→ljlWeek
     */
    List<AcdLingjieGroup> getLingjieGroupData(String tjDate, String comname, String groups);

    // ==================== 成本管控新增 14 张表 (2026-06) ====================

    /** 事故年赔付率-使用性质 */
    List<AcdPflsgnSyxz> getPflsgnSyxzData(String tjDate, String comnameSgs, String usenaturename);
    PageResult<AcdPflsgnSyxz> getPflsgnSyxzDataPage(String tjDate, String comnameSgs, String usenaturename, int current, int size);

    /** 事故年赔付率-支公司-客户群 */
    List<AcdPflsgnKhqZgs> getPflsgnKhqZgsData(String tjDate, String comnameSgs, String comname, String khq);
    PageResult<AcdPflsgnKhqZgs> getPflsgnKhqZgsDataPage(String tjDate, String comnameSgs, String comname, String khq, int current, int size);

    /** 事故年赔付率-支公司-使用性质 */
    List<AcdPflsgnSyxzZgs> getPflsgnSyxzZgsData(String tjDate, String comnameSgs, String comname, String usenaturename);
    PageResult<AcdPflsgnSyxzZgs> getPflsgnSyxzZgsDataPage(String tjDate, String comnameSgs, String comname, String usenaturename, int current, int size);

    /** 事故年赔付率-支公司-品牌 */
    List<AcdPflsgnPpZgs> getPflsgnPpZgsData(String tjDate, String comnameSgs, String comname, String brandname);
    PageResult<AcdPflsgnPpZgs> getPflsgnPpZgsDataPage(String tjDate, String comnameSgs, String comname, String brandname, int current, int size);

    /** 保单年赔付率-支公司 */
    List<AcdPflbdnZgs> getPflbdnZgsData(String tjDate, String comnameSgs);
    PageResult<AcdPflbdnZgs> getPflbdnZgsDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 保单年赔付率-客户群 */
    List<AcdPflbdnKhq> getPflbdnKhqData(String tjDate, String comnameSgs, String khq);
    PageResult<AcdPflbdnKhq> getPflbdnKhqDataPage(String tjDate, String comnameSgs, String khq, int current, int size);

    /** 保单年赔付率-使用性质 */
    List<AcdPflbdnSyxz> getPflbdnSyxzData(String tjDate, String comnameSgs, String usenaturename);
    PageResult<AcdPflbdnSyxz> getPflbdnSyxzDataPage(String tjDate, String comnameSgs, String usenaturename, int current, int size);

    /** 保单年赔付率-新能源 */
    List<AcdPflbdnXny> getPflbdnXnyData(String tjDate, String xnyflag);
    PageResult<AcdPflbdnXny> getPflbdnXnyDataPage(String tjDate, String xnyflag, int current, int size);

    /** 保单年赔付率-支公司-使用性质 */
    List<AcdPflbdnSyxzZgs> getPflbdnSyxzZgsData(String tjDate, String comnameSgs, String comname, String usenaturename);
    PageResult<AcdPflbdnSyxzZgs> getPflbdnSyxzZgsDataPage(String tjDate, String comnameSgs, String comname, String usenaturename, int current, int size);

    /** 保单年赔付率-支公司-客户群 */
    List<AcdPflbdnKhqZgs> getPflbdnKhqZgsData(String tjDate, String comnameSgs, String comname, String khq);
    PageResult<AcdPflbdnKhqZgs> getPflbdnKhqZgsDataPage(String tjDate, String comnameSgs, String comname, String khq, int current, int size);

    /** 保单年赔付率-支公司-新能源 */
    List<AcdPflbdnXnyZgs> getPflbdnXnyZgsData(String tjDate, String comnameSgs, String comname, String xnyflag);
    PageResult<AcdPflbdnXnyZgs> getPflbdnXnyZgsDataPage(String tjDate, String comnameSgs, String comname, String xnyflag, int current, int size);

    /** 保单年赔付率-支公司-品牌 */
    List<AcdPflbdnPpZgs> getPflbdnPpZgsData(String tjDate, String comnameSgs, String comname, String brandname);
    PageResult<AcdPflbdnPpZgs> getPflbdnPpZgsDataPage(String tjDate, String comnameSgs, String comname, String brandname, int current, int size);

    /** 综合赔付率-险种 */
    List<AcdZhpflXz> getZhpflXzData(String tjDate, String xl);
    PageResult<AcdZhpflXz> getZhpflXzDataPage(String tjDate, String xl, int current, int size);

    // ==================== 维修单位 (2026-06) ====================
    /** 各支公司产保比 */
    List<AcdZgsCbb> getZgsCbbData(String tjDate, String comnameSgs);
    PageResult<AcdZgsCbb> getZgsCbbDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 维修单位关键指标 */
    List<AcdWxdwGjzb> getWxdwGjzbData(String tjDate, String comnameSgs, String repairfactoryname);
    PageResult<AcdWxdwGjzb> getWxdwGjzbDataPage(String tjDate, String comnameSgs, String repairfactoryname, int current, int size);

    // ==================== 车均定损 (2026-06) ====================
    /** 车均定损-人员 */
    List<AcdChejunRy> getChejunRyData(String tjDate, String comnameSgs);
    PageResult<AcdChejunRy> getChejunRyDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 车均定损-处理部门 */
    List<AcdChejunClbm> getChejunClbmData(String tjDate, String comnameSgs);
    PageResult<AcdChejunClbm> getChejunClbmDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 车均定损-定损区域 */
    List<AcdChejunBm> getChejunBmData(String tjDate, String dsqy);
    PageResult<AcdChejunBm> getChejunBmDataPage(String tjDate, String dsqy, int current, int size);

    /** 车均定损-市公司 */
    List<AcdChejunSgs> getChejunSgsData(String tjDate, String comnameSgs);
    PageResult<AcdChejunSgs> getChejunSgsDataPage(String tjDate, String comnameSgs, int current, int size);

    /** 未决存量-案件类型 */
    List<AcdWjxs> getWjxsData(String tjDate, String comnameSgs);
    PageResult<AcdWjxs> getWjxsDataPage(String tjDate, String comnameSgs, int current, int size);
}