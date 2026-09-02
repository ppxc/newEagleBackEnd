package com.example.demo.controller;

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
import com.example.demo.entity.Result;
import com.example.demo.service.ReportTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ReportTableController {

    private static final Logger log = LoggerFactory.getLogger(ReportTableController.class);

    @Autowired
    private ReportTableService reportTableService;

    @GetMapping("/cur_gzl/list")
    public Result<List<CurGzlTableRy>> getCurGzlList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false) String userName
    ) {
        try {
            // 日期都为空 → 取最大日期
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {

                // 直接调用service
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_ry");
                startDate = maxDate;
                endDate = maxDate;
            }

            // 只有开始日期
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<CurGzlTableRy> data = reportTableService.getCurGzlData(
                    startDate, endDate, comName, groups, userName);

            return Result.success(data);

        } catch (Exception e) {
            log.error("获取失败", e);
            return Result.error("获取失败", e);
        }
    }

    @GetMapping("/cur_gzl_bm/list")
    public Result<List<CurGzlTableBm>> getCurGzlListBm(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_bm");
                startDate = maxDate;
                endDate = maxDate;
            }

            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<CurGzlTableBm> data = reportTableService.getCurGzlDataBm(
                    startDate, endDate, comName);

            return Result.success(data);

        } catch (Exception e) {
            log.error("获取部门统计失败", e);
            return Result.error("获取部门统计失败", e);
        }
    }

    @GetMapping("/cur_gzl_group/list")
    public Result<List<CurGzlTableGroup>> getCurGzlListGroup(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false) String groups
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                // 直接用咱们的通用最大日期方法
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_group");
                startDate = maxDate;
                endDate = maxDate;
            }

            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<CurGzlTableGroup> data = reportTableService.getCurGzlDataGroup(
                    startDate, endDate, comName, groups);

            return Result.success(data);

        } catch (Exception e) {
            log.error("获取小组统计失败", e);
            return Result.error("获取小组统计失败", e);
        }
    }

    @GetMapping("/cur_gzl_rs/list")
    public Result<List<CurGzlTableRs>> getCurGzlListRs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                // 通用最大日期
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_rs");
                startDate = maxDate;
                endDate = maxDate;
            }

            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<CurGzlTableRs> data = reportTableService.getCurGzlDataRs(
                    startDate, endDate, comName);

            return Result.success(data);

        } catch (Exception e) {
            log.error("获取住院门诊统计失败", e);
            return Result.error("获取住院门诊统计失败", e);
        }
    }

    // ==================== 新增表接口 ====================

    /** 周期-市公司 */
    @GetMapping("/zhouqi_qs/list")
    public Result<List<AcdZhouqiQs>> getZhouqiQsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                String maxDate = reportTableService.getMaxTjDate("acd_zhouqi_qs");
                tjDate = maxDate;
            }
            List<AcdZhouqiQs> data = reportTableService.getZhouqiQsData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取周期-市公司失败", e);
            return Result.error("获取周期-市公司失败", e);
        }
    }

    /** 周期-部门 */
    @GetMapping("/zhouqi_bm/list")
    public Result<List<AcdZhouqiBm>> getZhouqiBmList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                String maxDate = reportTableService.getMaxTjDate("acd_zhouqi_bm");
                tjDate = maxDate;
            }
            List<AcdZhouqiBm> data = reportTableService.getZhouqiBmData(tjDate, comname);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取周期-部门失败", e);
            return Result.error("获取周期-部门失败", e);
        }
    }

    /** 综合赔付率-客户群 */
    @GetMapping("/zhpfl_khq/list")
    public Result<List<AcdZhpflKhq>> getZhpflKhqList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                String maxDate = reportTableService.getMaxTjDate("acd_zhpfl_khq");
                tjDate = maxDate;
            }
            List<AcdZhpflKhq> data = reportTableService.getZhpflKhqData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取综合赔付率-客户群失败", e);
            return Result.error("获取综合赔付率-客户群失败", e);
        }
    }

    /** 车险结案率-部门 */
    @GetMapping("/pacll_bm/list")
    public Result<List<AcdPacllBm>> getPacllBmList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                String maxDate = reportTableService.getMaxTjDate("acd_pacll_bm");
                tjDate = maxDate;
            }
            List<AcdPacllBm> data = reportTableService.getPacllBmData(tjDate, comname);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取车险结案率-部门失败", e);
            return Result.error("获取车险结案率-部门失败", e);
        }
    }

    /** 车险结案率-小组 */
    @GetMapping("/pacll_xz/list")
    public Result<List<AcdPacllXz>> getPacllXzList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String groups
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                String maxDate = reportTableService.getMaxTjDate("acd_pacll_xz");
                tjDate = maxDate;
            }
            List<AcdPacllXz> data = reportTableService.getPacllXzData(tjDate, comname, groups);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取车险结案率-小组失败", e);
            return Result.error("获取车险结案率-小组失败", e);
        }
    }

    /** 车险结案率-人员 */
    @GetMapping("/pacll_ry/list")
    public Result<List<AcdPacllRy>> getPacllRyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String bm,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false) String username
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                String maxDate = reportTableService.getMaxTjDate("acd_pacll_ry");
                tjDate = maxDate;
            }
            List<AcdPacllRy> data = reportTableService.getPacllRyData(tjDate, bm, groups, username);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取车险结案率-人员失败", e);
            return Result.error("获取车险结案率-人员失败", e);
        }
    }

    // ==================== 事故年赔付率 ====================

    /** 事故年赔付率-支公司 */
    @GetMapping("/pflsgn_zgs/list")
    public Result<List<AcdPflsgnZgs>> getPflsgnZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_zgs");
            }
            List<AcdPflsgnZgs> data = reportTableService.getPflsgnZgsData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取事故年赔付率-支公司失败", e);
            return Result.error("获取事故年赔付率-支公司失败", e);
        }
    }

    /** 事故年赔付率-客户群 */
    @GetMapping("/pflsgn_khq/list")
    public Result<List<AcdPflsgnKhq>> getPflsgnKhqList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_khq");
            }
            List<AcdPflsgnKhq> data = reportTableService.getPflsgnKhqData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取事故年赔付率-客户群失败", e);
            return Result.error("获取事故年赔付率-客户群失败", e);
        }
    }

    /** 事故年赔付率-新能源 */
    @GetMapping("/pflsgn_xny/list")
    public Result<List<AcdPflsgnXny>> getPflsgnXnyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_xny");
            }
            List<AcdPflsgnXny> data = reportTableService.getPflsgnXnyData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取事故年赔付率-新能源失败", e);
            return Result.error("获取事故年赔付率-新能源失败", e);
        }
    }

    // ==================== 案均赔款 ====================

    /** 案均赔款-支公司（车险） */
    @GetMapping("/anjun_cx_zgs/list")
    public Result<List<AcdAnjunCxZgs>> getAnjunCxZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_anjun_cx_zgs");
            }
            List<AcdAnjunCxZgs> data = reportTableService.getAnjunCxZgsData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取案均赔款-支公司（车险）失败", e);
            return Result.error("获取案均赔款-支公司（车险）失败", e);
        }
    }

    @GetMapping("/anjun_cx_khq/list")
    public Result<List<AcdAnjunCxKhq>> getAnjunCxKhqList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_anjun_cx_khq");
            }
            List<AcdAnjunCxKhq> data = reportTableService.getAnjunCxKhqData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取案均赔款-客户群（车险）失败", e);
            return Result.error("获取案均赔款-客户群（车险）失败", e);
        }
    }

    @GetMapping("/anjun_cx_xny/list")
    public Result<List<AcdAnjunCxXny>> getAnjunCxXnyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_anjun_cx_xny");
            }
            List<AcdAnjunCxXny> data = reportTableService.getAnjunCxXnyData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取案均赔款-新能源（车险）失败", e);
            return Result.error("获取案均赔款-新能源（车险）失败", e);
        }
    }

    // ==================== 年度每月系列 ====================

    /** 查勘量-年度每月 */
    @GetMapping("/chakan_year/list")
    public Result<List<AcdChakanYear>> getChakanYearList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chakan_year");
            }
            List<AcdChakanYear> data = reportTableService.getChakanYearData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取查勘量-年度每月失败", e);
            return Result.error("获取查勘量-年度每月失败", e);
        }
    }

    /** 定损提交量-年度每月 */
    @GetMapping("/dingsun_tjl_year/list")
    public Result<List<AcdDingsunTjlYear>> getDingsunTjlYearList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_tjl_year");
            }
            List<AcdDingsunTjlYear> data = reportTableService.getDingsunTjlYearData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取定损提交量-年度每月失败", e);
            return Result.error("获取定损提交量-年度每月失败", e);
        }
    }

    /** 定损完成量-年度每月 */
    @GetMapping("/dingsun_wcl_year/list")
    public Result<List<AcdDingsunWclYear>> getDingsunWclYearList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_wcl_year");
            }
            List<AcdDingsunWclYear> data = reportTableService.getDingsunWclYearData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取定损完成量-年度每月失败", e);
            return Result.error("获取定损完成量-年度每月失败", e);
        }
    }

    /** 查勘量+定损完成-年度每月 */
    @GetMapping("/ck_dswc_year/list")
    public Result<List<AcdCkDswcYear>> getCkDswcYearList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_ck_dswc_year");
            }
            List<AcdCkDswcYear> data = reportTableService.getCkDswcYearData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取查勘量+定损完成-年度每月失败", e);
            return Result.error("获取查勘量+定损完成-年度每月失败", e);
        }
    }

    /** 定损支付量-年度每月 */
    @GetMapping("/dingsun_zfl_year/list")
    public Result<List<AcdDingsunZflYear>> getDingsunZflYearList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_zfl_year");
            }
            List<AcdDingsunZflYear> data = reportTableService.getDingsunZflYearData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取定损支付量-年度每月失败", e);
            return Result.error("获取定损支付量-年度每月失败", e);
        }
    }

    /** 理算量-年度每月 */
    @GetMapping("/lisuan_year/list")
    public Result<List<AcdLisuanYear>> getLisuanYearList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lisuan_year");
            }
            List<AcdLisuanYear> data = reportTableService.getLisuanYearData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取理算量-年度每月失败", e);
            return Result.error("获取理算量-年度每月失败", e);
        }
    }

    /** 人伤跟踪量-年度每月 */
    @GetMapping("/rs_gzl_year/list")
    public Result<List<AcdRsGzlYear>> getRsGzlYearList(
            @RequestParam(required = false) String tjDate
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                // 共享表 acd_rs_gzl_year：按 jaflag 区分"后续跟踪"/"人伤调解"，各自取段内最大日期
                tjDate = reportTableService.getMaxTjDateByTableAndFlag(
                        "acd_rs_gzl_year", "jaflag", "后续跟踪");
            }
            List<AcdRsGzlYear> data = reportTableService.getRsGzlYearData(tjDate);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取人伤跟踪量-年度每月失败", e);
            return Result.error("获取人伤跟踪量-年度每月失败", e);
        }
    }

    /** 人伤调解量-年度每月 */
    @GetMapping("/rs_tjl_year/list")
    public Result<List<AcdRsGzlYear>> getRsTjlYearList(
            @RequestParam(required = false) String tjDate
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                // 共享表 acd_rs_gzl_year：按 jaflag 区分"后续跟踪"/"人伤调解"，各自取段内最大日期
                tjDate = reportTableService.getMaxTjDateByTableAndFlag(
                        "acd_rs_gzl_year", "jaflag", "人伤调解");
            }
            List<AcdRsGzlYear> data = reportTableService.getRsTjlYearData(tjDate);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取人伤调解量-年度每月失败", e);
            return Result.error("获取人伤调解量-年度每月失败", e);
        }
    }

    /** 查勘量-月度每日 */
    @GetMapping("/chakan_month/list")
    public Result<List<AcdChakanMonth>> getChakanMonthList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chakan_month");
            }
            List<AcdChakanMonth> data = reportTableService.getChakanMonthData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取查勘量-月度每日失败", e);
            return Result.error("获取查勘量-月度每日失败", e);
        }
    }

    /** 查勘量+定损完成-月度每日 */
    @GetMapping("/ck_dswc_month/list")
    public Result<List<AcdCkDswcMonth>> getCkDswcMonthList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_ck_dswc_month");
            }
            List<AcdCkDswcMonth> data = reportTableService.getCkDswcMonthData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取查勘量+定损完成-月度每日失败", e);
            return Result.error("获取查勘量+定损完成-月度每日失败", e);
        }
    }

    /** 人伤跟踪量-月度每日 */
    @GetMapping("/rs_gzl_month/list")
    public Result<List<AcdRsGzlMonth>> getRsGzlMonthList(
            @RequestParam(required = false) String tjDate
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_rs_gzl_month");
            }
            List<AcdRsGzlMonth> data = reportTableService.getRsGzlMonthData(tjDate);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取人伤跟踪量-月度每日失败", e);
            return Result.error("获取人伤跟踪量-月度每日失败", e);
        }
    }

    /** 人伤调解量-月度每日 */
    @GetMapping("/rs_tjl_month/list")
    public Result<List<AcdRsGzlMonth>> getRsTjlMonthList(
            @RequestParam(required = false) String tjDate
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_rs_gzl_month");
            }
            List<AcdRsGzlMonth> data = reportTableService.getRsTjlMonthData(tjDate);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取人伤调解量-月度每日失败", e);
            return Result.error("获取人伤调解量-月度每日失败", e);
        }
    }

    /** 理算量-月度每日 */
    @GetMapping("/lisuan_month/list")
    public Result<List<AcdLisuanMonth>> getLisuanMonthList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lisuan_month");
            }
            List<AcdLisuanMonth> data = reportTableService.getLisuanMonthData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取理算量-月度每日失败", e);
            return Result.error("获取理算量-月度每日失败", e);
        }
    }

    // ==================== 分页端点（laoxiao 9 + chakan_month） ====================
    // 旧 /list 端点保持不动；新 /page 端点带 current/size 参数并返回 PageResult<T>。

    /** 查勘量-年度每月 - 分页 */
    @GetMapping("/chakan_year/page")
    public Result<PageResult<AcdChakanYear>> getChakanYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chakan_year");
            }
            return Result.success(reportTableService.getChakanYearDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取查勘量-年度每月分页失败", e);
            return Result.error("获取查勘量-年度每月分页失败", e);
        }
    }

    /** 定损提交量-年度每月 - 分页 */
    @GetMapping("/dingsun_tjl_year/page")
    public Result<PageResult<AcdDingsunTjlYear>> getDingsunTjlYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_tjl_year");
            }
            return Result.success(reportTableService.getDingsunTjlYearDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取定损提交量-年度每月分页失败", e);
            return Result.error("获取定损提交量-年度每月分页失败", e);
        }
    }

    /** 定损完成量-年度每月 - 分页 */
    @GetMapping("/dingsun_wcl_year/page")
    public Result<PageResult<AcdDingsunWclYear>> getDingsunWclYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_wcl_year");
            }
            return Result.success(reportTableService.getDingsunWclYearDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取定损完成量-年度每月分页失败", e);
            return Result.error("获取定损完成量-年度每月分页失败", e);
        }
    }

    /** 查勘量+定损完成-年度每月 - 分页 */
    @GetMapping("/ck_dswc_year/page")
    public Result<PageResult<AcdCkDswcYear>> getCkDswcYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_ck_dswc_year");
            }
            return Result.success(reportTableService.getCkDswcYearDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取查勘量+定损完成-年度每月分页失败", e);
            return Result.error("获取查勘量+定损完成-年度每月分页失败", e);
        }
    }

    /** 定损支付量-年度每月 - 分页（已 1136 行重点） */
    @GetMapping("/dingsun_zfl_year/page")
    public Result<PageResult<AcdDingsunZflYear>> getDingsunZflYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_zfl_year");
            }
            return Result.success(reportTableService.getDingsunZflYearDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取定损支付量-年度每月分页失败", e);
            return Result.error("获取定损支付量-年度每月分页失败", e);
        }
    }

    /** 理算量-年度每月 - 分页 */
    @GetMapping("/lisuan_year/page")
    public Result<PageResult<AcdLisuanYear>> getLisuanYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lisuan_year");
            }
            return Result.success(reportTableService.getLisuanYearDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取理算量-年度每月分页失败", e);
            return Result.error("获取理算量-年度每月分页失败", e);
        }
    }

    /** 人伤跟踪量-年度每月 - 分页（无 comnameSgs 筛选） */
    @GetMapping("/rs_gzl_year/page")
    public Result<PageResult<AcdRsGzlYear>> getRsGzlYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                // 共享表 acd_rs_gzl_year：按 jaflag 区分"后续跟踪"/"人伤调解"，各自取段内最大日期
                tjDate = reportTableService.getMaxTjDateByTableAndFlag(
                        "acd_rs_gzl_year", "jaflag", "后续跟踪");
            }
            return Result.success(reportTableService.getRsGzlYearDataPage(
                    tjDate,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取人伤跟踪量-年度每月分页失败", e);
            return Result.error("获取人伤跟踪量-年度每月分页失败", e);
        }
    }

    /** 人伤调解量-年度每月 - 分页（无 comnameSgs 筛选） */
    @GetMapping("/rs_tjl_year/page")
    public Result<PageResult<AcdRsGzlYear>> getRsTjlYearPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                // 共享表 acd_rs_gzl_year：按 jaflag 区分"后续跟踪"/"人伤调解"，各自取段内最大日期
                tjDate = reportTableService.getMaxTjDateByTableAndFlag(
                        "acd_rs_gzl_year", "jaflag", "人伤调解");
            }
            return Result.success(reportTableService.getRsTjlYearDataPage(
                    tjDate,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取人伤调解量-年度每月分页失败", e);
            return Result.error("获取人伤调解量-年度每月分页失败", e);
        }
    }

    /** 查勘量-月度每日 - 分页 */
    @GetMapping("/chakan_month/page")
    public Result<PageResult<AcdChakanMonth>> getChakanMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chakan_month");
            }
            return Result.success(reportTableService.getChakanMonthDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取查勘量-月度每日分页失败", e);
            return Result.error("获取查勘量-月度每日分页失败", e);
        }
    }

    /** 查勘量+定损完成-月度每日 - 分页 */
    @GetMapping("/ck_dswc_month/page")
    public Result<PageResult<AcdCkDswcMonth>> getCkDswcMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_ck_dswc_month");
            }
            return Result.success(reportTableService.getCkDswcMonthDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取查勘量+定损完成-月度每日分页失败", e);
            return Result.error("获取查勘量+定损完成-月度每日分页失败", e);
        }
    }

    /** 定损提交量-月度每日 */
    @GetMapping("/dingsun_tjl_month/list")
    public Result<List<AcdDingsunTjlMonth>> getDingsunTjlMonthList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_tjl_month");
            }
            List<AcdDingsunTjlMonth> data = reportTableService.getDingsunTjlMonthData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取定损提交量-月度每日失败", e);
            return Result.error("获取定损提交量-月度每日失败", e);
        }
    }

    /** 定损完成量-月度每日 */
    @GetMapping("/dingsun_wcl_month/list")
    public Result<List<AcdDingsunWclMonth>> getDingsunWclMonthList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_wcl_month");
            }
            List<AcdDingsunWclMonth> data = reportTableService.getDingsunWclMonthData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取定损完成量-月度每日失败", e);
            return Result.error("获取定损完成量-月度每日失败", e);
        }
    }

    /** 定损支付量-月度每日 */
    @GetMapping("/dingsun_zfl_month/list")
    public Result<List<AcdDingsunZflMonth>> getDingsunZflMonthList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_zfl_month");
            }
            List<AcdDingsunZflMonth> data = reportTableService.getDingsunZflMonthData(tjDate, comnameSgs);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取定损支付量-月度每日失败", e);
            return Result.error("获取定损支付量-月度每日失败", e);
        }
    }

    /** 定损提交量-月度每日 - 分页 */
    @GetMapping("/dingsun_tjl_month/page")
    public Result<PageResult<AcdDingsunTjlMonth>> getDingsunTjlMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_tjl_month");
            }
            return Result.success(reportTableService.getDingsunTjlMonthDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取定损提交量-月度每日分页失败", e);
            return Result.error("获取定损提交量-月度每日分页失败", e);
        }
    }

    /** 定损完成量-月度每日 - 分页 */
    @GetMapping("/dingsun_wcl_month/page")
    public Result<PageResult<AcdDingsunWclMonth>> getDingsunWclMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_wcl_month");
            }
            return Result.success(reportTableService.getDingsunWclMonthDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取定损完成量-月度每日分页失败", e);
            return Result.error("获取定损完成量-月度每日分页失败", e);
        }
    }

    /** 定损支付量-月度每日 - 分页 */
    @GetMapping("/dingsun_zfl_month/page")
    public Result<PageResult<AcdDingsunZflMonth>> getDingsunZflMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_dingsun_zfl_month");
            }
            return Result.success(reportTableService.getDingsunZflMonthDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取定损支付量-月度每日分页失败", e);
            return Result.error("获取定损支付量-月度每日分页失败", e);
        }
    }

    /** 人伤跟踪量-月度每日 - 分页 */
    @GetMapping("/rs_gzl_month/page")
    public Result<PageResult<AcdRsGzlMonth>> getRsGzlMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_rs_gzl_month");
            }
            return Result.success(reportTableService.getRsGzlMonthDataPage(
                    tjDate,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取人伤跟踪量-月度每日分页失败", e);
            return Result.error("获取人伤跟踪量-月度每日分页失败", e);
        }
    }

    /** 人伤调解量-月度每日 - 分页 */
    @GetMapping("/rs_tjl_month/page")
    public Result<PageResult<AcdRsGzlMonth>> getRsTjlMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_rs_gzl_month");
            }
            return Result.success(reportTableService.getRsTjlMonthDataPage(
                    tjDate,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取人伤调解量-月度每日分页失败", e);
            return Result.error("获取人伤调解量-月度每日分页失败", e);
        }
    }

    /** 理算量-月度每日 - 分页 */
    @GetMapping("/lisuan_month/page")
    public Result<PageResult<AcdLisuanMonth>> getLisuanMonthPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lisuan_month");
            }
            return Result.success(reportTableService.getLisuanMonthDataPage(
                    tjDate,
                    comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取理算量-月度每日分页失败", e);
            return Result.error("获取理算量-月度每日分页失败", e);
        }
    }

    // ==================== 分页端点（lpcenter/efficiency 16 端点） ====================

    /** 人员日工作量 - 分页 */
    @GetMapping("/cur_gzl/page")
    public Result<PageResult<CurGzlTableRy>> getCurGzlPage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_ry");
                startDate = maxDate; endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) endDate = startDate;
            return Result.success(reportTableService.getCurGzlDataPage(
                    startDate, endDate, comName, groups, userName,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取人员日工作量分页失败", e);
            return Result.error("获取人员日工作量分页失败", e);
        }
    }

    /** 部门日工作量 - 分页 */
    @GetMapping("/cur_gzl_bm/page")
    public Result<PageResult<CurGzlTableBm>> getCurGzlBmPage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_bm");
                startDate = maxDate; endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) endDate = startDate;
            return Result.success(reportTableService.getCurGzlDataBmPage(
                    startDate, endDate, comName,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取部门日工作量分页失败", e);
            return Result.error("获取部门日工作量分页失败", e);
        }
    }

    /** 小组日工作量 - 分页 */
    @GetMapping("/cur_gzl_group/page")
    public Result<PageResult<CurGzlTableGroup>> getCurGzlGroupPage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_group");
                startDate = maxDate; endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) endDate = startDate;
            return Result.success(reportTableService.getCurGzlDataGroupPage(
                    startDate, endDate, comName, groups,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取小组日工作量分页失败", e);
            return Result.error("获取小组日工作量分页失败", e);
        }
    }

    /** 人伤日工作量 - 分页 */
    @GetMapping("/cur_gzl_rs/page")
    public Result<PageResult<CurGzlTableRs>> getCurGzlRsPage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = reportTableService.getMaxTjDate("acd_dangri_gzl_rs");
                startDate = maxDate; endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) endDate = startDate;
            return Result.success(reportTableService.getCurGzlDataRsPage(
                    startDate, endDate, comName,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取人伤日工作量分页失败", e);
            return Result.error("获取人伤日工作量分页失败", e);
        }
    }

    /** 周期-市公司 - 分页 */
    @GetMapping("/zhouqi_qs/page")
    public Result<PageResult<AcdZhouqiQs>> getZhouqiQsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhouqi_qs");
            }
            return Result.success(reportTableService.getZhouqiQsDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取周期-市公司分页失败", e);
            return Result.error("获取周期-市公司分页失败", e);
        }
    }

    /** 周期-部门 - 分页 */
    @GetMapping("/zhouqi_bm/page")
    public Result<PageResult<AcdZhouqiBm>> getZhouqiBmPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhouqi_bm");
            }
            return Result.success(reportTableService.getZhouqiBmDataPage(
                    tjDate, comname,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取周期-部门分页失败", e);
            return Result.error("获取周期-部门分页失败", e);
        }
    }

    /** 综合赔付率-客户群 - 分页 */
    @GetMapping("/zhpfl_khq/page")
    public Result<PageResult<AcdZhpflKhq>> getZhpflKhqPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhpfl_khq");
            }
            return Result.success(reportTableService.getZhpflKhqDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取综合赔付率-客户群分页失败", e);
            return Result.error("获取综合赔付率-客户群分页失败", e);
        }
    }

    /** 车险结案率-部门 - 分页 */
    @GetMapping("/pacll_bm/page")
    public Result<PageResult<AcdPacllBm>> getPacllBmPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pacll_bm");
            }
            return Result.success(reportTableService.getPacllBmDataPage(
                    tjDate, comname,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取车险结案率-部门分页失败", e);
            return Result.error("获取车险结案率-部门分页失败", e);
        }
    }

    /** 车险结案率-小组 - 分页 */
    @GetMapping("/pacll_xz/page")
    public Result<PageResult<AcdPacllXz>> getPacllXzPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pacll_xz");
            }
            return Result.success(reportTableService.getPacllXzDataPage(
                    tjDate, comname, groups,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取车险结案率-小组分页失败", e);
            return Result.error("获取车险结案率-小组分页失败", e);
        }
    }

    /** 车险结案率-人员 - 分页 */
    @GetMapping("/pacll_ry/page")
    public Result<PageResult<AcdPacllRy>> getPacllRyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String bm,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pacll_ry");
            }
            return Result.success(reportTableService.getPacllRyDataPage(
                    tjDate, bm, groups, username,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取车险结案率-人员分页失败", e);
            return Result.error("获取车险结案率-人员分页失败", e);
        }
    }

    /** 事故年赔付率-支公司 - 分页 */
    @GetMapping("/pflsgn_zgs/page")
    public Result<PageResult<AcdPflsgnZgs>> getPflsgnZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_zgs");
            }
            return Result.success(reportTableService.getPflsgnZgsDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取事故年赔付率-支公司分页失败", e);
            return Result.error("获取事故年赔付率-支公司分页失败", e);
        }
    }

    /** 事故年赔付率-客户群 - 分页 */
    @GetMapping("/pflsgn_khq/page")
    public Result<PageResult<AcdPflsgnKhq>> getPflsgnKhqPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_khq");
            }
            return Result.success(reportTableService.getPflsgnKhqDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取事故年赔付率-客户群分页失败", e);
            return Result.error("获取事故年赔付率-客户群分页失败", e);
        }
    }

    /** 事故年赔付率-新能源 - 分页 */
    @GetMapping("/pflsgn_xny/page")
    public Result<PageResult<AcdPflsgnXny>> getPflsgnXnyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_xny");
            }
            return Result.success(reportTableService.getPflsgnXnyDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取事故年赔付率-新能源分页失败", e);
            return Result.error("获取事故年赔付率-新能源分页失败", e);
        }
    }

    /** 案均赔款-支公司（车险）- 分页 */
    @GetMapping("/anjun_cx_zgs/page")
    public Result<PageResult<AcdAnjunCxZgs>> getAnjunCxZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_anjun_cx_zgs");
            }
            return Result.success(reportTableService.getAnjunCxZgsDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取案均赔款-支公司（车险）分页失败", e);
            return Result.error("获取案均赔款-支公司（车险）分页失败", e);
        }
    }

    /** 案均赔款-客户群（车险）- 分页 */
    @GetMapping("/anjun_cx_khq/page")
    public Result<PageResult<AcdAnjunCxKhq>> getAnjunCxKhqPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_anjun_cx_khq");
            }
            return Result.success(reportTableService.getAnjunCxKhqDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取案均赔款-客户群（车险）分页失败", e);
            return Result.error("获取案均赔款-客户群（车险）分页失败", e);
        }
    }

    /** 案均赔款-新能源（车险）- 分页 */
    @GetMapping("/anjun_cx_xny/page")
    public Result<PageResult<AcdAnjunCxXny>> getAnjunCxXnyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_anjun_cx_xny");
            }
            return Result.success(reportTableService.getAnjunCxXnyDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取案均赔款-新能源（车险）分页失败", e);
            return Result.error("获取案均赔款-新能源（车险）分页失败", e);
        }
    }

    // ==================== 2026-06 新增 7 张表 (list + page) ====================

    /** 车险案件量-承保地 */
    @GetMapping("/ba_la_ja_wj_pk/list")
    public Result<List<AcdBaLaJaWjPk>> getBaLaJaWjPkList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_ba_la_ja_wj_pk");
            }
            return Result.success(reportTableService.getBaLaJaWjPkData(tjDate, comnameSgs));
        } catch (Exception e) {
            log.error("获取车险案件量-承保地失败", e);
            return Result.error("获取车险案件量-承保地失败", e);
        }
    }

    @GetMapping("/ba_la_ja_wj_pk/page")
    public Result<PageResult<AcdBaLaJaWjPk>> getBaLaJaWjPkPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_ba_la_ja_wj_pk");
            }
            return Result.success(reportTableService.getBaLaJaWjPkDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取车险案件量-承保地分页失败", e);
            return Result.error("获取车险案件量-承保地分页失败", e);
        }
    }

    /** 周期-人员 */
    @GetMapping("/zhouqi_ry/list")
    public Result<List<AcdZhouqiRy>> getZhouqiRyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhouqi_ry");
            }
            return Result.success(reportTableService.getZhouqiRyData(tjDate, comnameSgs));
        } catch (Exception e) {
            log.error("获取周期-人员失败", e);
            return Result.error("获取周期-人员失败", e);
        }
    }

    @GetMapping("/zhouqi_ry/page")
    public Result<PageResult<AcdZhouqiRy>> getZhouqiRyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhouqi_ry");
            }
            return Result.success(reportTableService.getZhouqiRyDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取周期-人员分页失败", e);
            return Result.error("获取周期-人员分页失败", e);
        }
    }

    /** 每日结案量-部门实时 */
    @GetMapping("/jieanl_bm/list")
    public Result<List<AcdJieanlBm>> getJieanlBmList(
            @RequestParam(required = false) String tjDate
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_jieanl_bm");
            }
            return Result.success(reportTableService.getJieanlBmData(tjDate));
        } catch (Exception e) {
            log.error("获取每日结案量-部门实时失败", e);
            return Result.error("获取每日结案量-部门实时失败", e);
        }
    }

    @GetMapping("/jieanl_bm/page")
    public Result<PageResult<AcdJieanlBm>> getJieanlBmPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_jieanl_bm");
            }
            return Result.success(reportTableService.getJieanlBmDataPage(
                    tjDate,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取每日结案量-部门实时分页失败", e);
            return Result.error("获取每日结案量-部门实时分页失败", e);
        }
    }

    /** 每日结案量-人员实时 */
    @GetMapping("/jieanl_ry/list")
    public Result<List<AcdJieanlRy>> getJieanlRyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_jieanl_ry");
            }
            return Result.success(reportTableService.getJieanlRyData(tjDate, comname));
        } catch (Exception e) {
            log.error("获取每日结案量-人员实时失败", e);
            return Result.error("获取每日结案量-人员实时失败", e);
        }
    }

    @GetMapping("/jieanl_ry/page")
    public Result<PageResult<AcdJieanlRy>> getJieanlRyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_jieanl_ry");
            }
            return Result.success(reportTableService.getJieanlRyDataPage(
                    tjDate, comname,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取每日结案量-人员实时分页失败", e);
            return Result.error("获取每日结案量-人员实时分页失败", e);
        }
    }

    /** 车险结案率-支公司 */
    @GetMapping("/pacll_cx_zgs/list")
    public Result<List<AcdPacllCxZgs>> getPacllCxZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pacll_cx_zgs");
            }
            return Result.success(reportTableService.getPacllCxZgsData(tjDate, comnameSgs));
        } catch (Exception e) {
            log.error("获取车险结案率-支公司失败", e);
            return Result.error("获取车险结案率-支公司失败", e);
        }
    }

    @GetMapping("/pacll_cx_zgs/page")
    public Result<PageResult<AcdPacllCxZgs>> getPacllCxZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pacll_cx_zgs");
            }
            return Result.success(reportTableService.getPacllCxZgsDataPage(
                    tjDate, comnameSgs,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取车险结案率-支公司分页失败", e);
            return Result.error("获取车险结案率-支公司分页失败", e);
        }
    }

    /** 零结案-人员 */
    @GetMapping("/lingjie_ry/list")
    public Result<List<AcdLingjieRy>> getLingjieRyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false) String username
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lingjie_ry");
            }
            return Result.success(reportTableService.getLingjieRyData(tjDate, groups, username));
        } catch (Exception e) {
            log.error("获取零结案-人员失败", e);
            return Result.error("获取零结案-人员失败", e);
        }
    }

    /**
     * 零结案-小组（合成：bm + groups + zxzt 三表内存合并）
     * comname、groups 可选模糊过滤；tjDate 为空时回退到 groups 表的 max(tjdate)
     */
    @GetMapping("/lingjie_group/list")
    public Result<List<AcdLingjieGroup>> getLingjieGroupList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String groups
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lingjie_groups");
            }
            return Result.success(reportTableService.getLingjieGroupData(tjDate, comname, groups));
        } catch (Exception e) {
            log.error("获取零结案-小组失败", e);
            return Result.error("获取零结案-小组失败", e);
        }
    }

    @GetMapping("/lingjie_ry/page")
    public Result<PageResult<AcdLingjieRy>> getLingjieRyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_lingjie_ry");
            }
            return Result.success(reportTableService.getLingjieRyDataPage(
                    tjDate, groups, username,
                    current == null ? 1 : current,
                    size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取零结案-人员分页失败", e);
            return Result.error("获取零结案-人员分页失败", e);
        }
    }

    // ==================== 成本管控新增 14 张表 (2026-06) ====================

    /** 事故年赔付率-使用性质 */
    @GetMapping("/pflsgn_syxz/list")
    public Result<List<AcdPflsgnSyxz>> getPflsgnSyxzList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String usenaturename) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_syxz");
            }
            return Result.success(reportTableService.getPflsgnSyxzData(tjDate, comnameSgs, usenaturename));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-使用性质失败", e);
        }
    }
    @GetMapping("/pflsgn_syxz/page")
    public Result<PageResult<AcdPflsgnSyxz>> getPflsgnSyxzPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String usenaturename,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_syxz");
            }
            return Result.success(reportTableService.getPflsgnSyxzDataPage(tjDate, comnameSgs, usenaturename,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-使用性质分页失败", e);
        }
    }

    /** 事故年赔付率-支公司-客户群 */
    @GetMapping("/pflsgn_khq_zgs/list")
    public Result<List<AcdPflsgnKhqZgs>> getPflsgnKhqZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String khq) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_khq_zgs");
            }
            return Result.success(reportTableService.getPflsgnKhqZgsData(tjDate, comnameSgs, comname, khq));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-支公司-客户群失败", e);
        }
    }
    @GetMapping("/pflsgn_khq_zgs/page")
    public Result<PageResult<AcdPflsgnKhqZgs>> getPflsgnKhqZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String khq,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_khq_zgs");
            }
            return Result.success(reportTableService.getPflsgnKhqZgsDataPage(tjDate, comnameSgs, comname, khq,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-支公司-客户群分页失败", e);
        }
    }

    /** 事故年赔付率-支公司-使用性质 */
    @GetMapping("/pflsgn_syxz_zgs/list")
    public Result<List<AcdPflsgnSyxzZgs>> getPflsgnSyxzZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String usenaturename) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_syxz_zgs");
            }
            return Result.success(reportTableService.getPflsgnSyxzZgsData(tjDate, comnameSgs, comname, usenaturename));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-支公司-使用性质失败", e);
        }
    }
    @GetMapping("/pflsgn_syxz_zgs/page")
    public Result<PageResult<AcdPflsgnSyxzZgs>> getPflsgnSyxzZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String usenaturename,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_syxz_zgs");
            }
            return Result.success(reportTableService.getPflsgnSyxzZgsDataPage(tjDate, comnameSgs, comname, usenaturename,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-支公司-使用性质分页失败", e);
        }
    }

    /** 事故年赔付率-支公司-品牌 */
    @GetMapping("/pflsgn_pp_zgs/list")
    public Result<List<AcdPflsgnPpZgs>> getPflsgnPpZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String brandname) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_pinpai_zgs");
            }
            return Result.success(reportTableService.getPflsgnPpZgsData(tjDate, comnameSgs, comname, brandname));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-支公司-品牌失败", e);
        }
    }
    @GetMapping("/pflsgn_pp_zgs/page")
    public Result<PageResult<AcdPflsgnPpZgs>> getPflsgnPpZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String brandname,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflsgn_pinpai_zgs");
            }
            return Result.success(reportTableService.getPflsgnPpZgsDataPage(tjDate, comnameSgs, comname, brandname,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取事故年赔付率-支公司-品牌分页失败", e);
        }
    }

    /** 保单年赔付率-支公司 */
    @GetMapping("/pflbdn_zgs/list")
    public Result<List<AcdPflbdnZgs>> getPflbdnZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_zgs");
            }
            return Result.success(reportTableService.getPflbdnZgsData(tjDate, comnameSgs));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司失败", e);
        }
    }
    @GetMapping("/pflbdn_zgs/page")
    public Result<PageResult<AcdPflbdnZgs>> getPflbdnZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_zgs");
            }
            return Result.success(reportTableService.getPflbdnZgsDataPage(tjDate, comnameSgs,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司分页失败", e);
        }
    }

    /** 保单年赔付率-客户群 */
    @GetMapping("/pflbdn_khq/list")
    public Result<List<AcdPflbdnKhq>> getPflbdnKhqList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String khq) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_khq");
            }
            return Result.success(reportTableService.getPflbdnKhqData(tjDate, comnameSgs, khq));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-客户群失败", e);
        }
    }
    @GetMapping("/pflbdn_khq/page")
    public Result<PageResult<AcdPflbdnKhq>> getPflbdnKhqPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String khq,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_khq");
            }
            return Result.success(reportTableService.getPflbdnKhqDataPage(tjDate, comnameSgs, khq,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-客户群分页失败", e);
        }
    }

    /** 保单年赔付率-使用性质 */
    @GetMapping("/pflbdn_syxz/list")
    public Result<List<AcdPflbdnSyxz>> getPflbdnSyxzList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String usenaturename) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_syxz");
            }
            return Result.success(reportTableService.getPflbdnSyxzData(tjDate, comnameSgs, usenaturename));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-使用性质失败", e);
        }
    }
    @GetMapping("/pflbdn_syxz/page")
    public Result<PageResult<AcdPflbdnSyxz>> getPflbdnSyxzPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String usenaturename,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_syxz");
            }
            return Result.success(reportTableService.getPflbdnSyxzDataPage(tjDate, comnameSgs, usenaturename,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-使用性质分页失败", e);
        }
    }

    /** 保单年赔付率-新能源 */
    @GetMapping("/pflbdn_xny/list")
    public Result<List<AcdPflbdnXny>> getPflbdnXnyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String xnyflag) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_xny");
            }
            return Result.success(reportTableService.getPflbdnXnyData(tjDate, xnyflag));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-新能源失败", e);
        }
    }
    @GetMapping("/pflbdn_xny/page")
    public Result<PageResult<AcdPflbdnXny>> getPflbdnXnyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String xnyflag,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_xny");
            }
            return Result.success(reportTableService.getPflbdnXnyDataPage(tjDate, xnyflag,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-新能源分页失败", e);
        }
    }

    /** 保单年赔付率-支公司-使用性质 */
    @GetMapping("/pflbdn_syxz_zgs/list")
    public Result<List<AcdPflbdnSyxzZgs>> getPflbdnSyxzZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String usenaturename) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_syxz_zgs");
            }
            return Result.success(reportTableService.getPflbdnSyxzZgsData(tjDate, comnameSgs, comname, usenaturename));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-使用性质失败", e);
        }
    }
    @GetMapping("/pflbdn_syxz_zgs/page")
    public Result<PageResult<AcdPflbdnSyxzZgs>> getPflbdnSyxzZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String usenaturename,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_syxz_zgs");
            }
            return Result.success(reportTableService.getPflbdnSyxzZgsDataPage(tjDate, comnameSgs, comname, usenaturename,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-使用性质分页失败", e);
        }
    }

    /** 保单年赔付率-支公司-客户群 */
    @GetMapping("/pflbdn_khq_zgs/list")
    public Result<List<AcdPflbdnKhqZgs>> getPflbdnKhqZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String khq) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_khq_zgs");
            }
            return Result.success(reportTableService.getPflbdnKhqZgsData(tjDate, comnameSgs, comname, khq));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-客户群失败", e);
        }
    }
    @GetMapping("/pflbdn_khq_zgs/page")
    public Result<PageResult<AcdPflbdnKhqZgs>> getPflbdnKhqZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String khq,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_khq_zgs");
            }
            return Result.success(reportTableService.getPflbdnKhqZgsDataPage(tjDate, comnameSgs, comname, khq,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-客户群分页失败", e);
        }
    }

    /** 保单年赔付率-支公司-新能源 */
    @GetMapping("/pflbdn_xny_zgs/list")
    public Result<List<AcdPflbdnXnyZgs>> getPflbdnXnyZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String xnyflag) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_xny_zgs");
            }
            return Result.success(reportTableService.getPflbdnXnyZgsData(tjDate, comnameSgs, comname, xnyflag));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-新能源失败", e);
        }
    }
    @GetMapping("/pflbdn_xny_zgs/page")
    public Result<PageResult<AcdPflbdnXnyZgs>> getPflbdnXnyZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String xnyflag,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_xny_zgs");
            }
            return Result.success(reportTableService.getPflbdnXnyZgsDataPage(tjDate, comnameSgs, comname, xnyflag,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-新能源分页失败", e);
        }
    }

    /** 保单年赔付率-支公司-品牌 */
    @GetMapping("/pflbdn_pp_zgs/list")
    public Result<List<AcdPflbdnPpZgs>> getPflbdnPpZgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String brandname) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_pinpai_zgs");
            }
            return Result.success(reportTableService.getPflbdnPpZgsData(tjDate, comnameSgs, comname, brandname));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-品牌失败", e);
        }
    }
    @GetMapping("/pflbdn_pp_zgs/page")
    public Result<PageResult<AcdPflbdnPpZgs>> getPflbdnPpZgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String comname,
            @RequestParam(required = false) String brandname,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_pflbdn_pinpai_zgs");
            }
            return Result.success(reportTableService.getPflbdnPpZgsDataPage(tjDate, comnameSgs, comname, brandname,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取保单年赔付率-支公司-品牌分页失败", e);
        }
    }

    /** 综合赔付率-险种 */
    @GetMapping("/zhpfl_xz/list")
    public Result<List<AcdZhpflXz>> getZhpflXzList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String xl) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhpfl_xl3");
            }
            return Result.success(reportTableService.getZhpflXzData(tjDate, xl));
        } catch (Exception e) {
            return Result.error("获取综合赔付率-险种失败", e);
        }
    }
    @GetMapping("/zhpfl_xz/page")
    public Result<PageResult<AcdZhpflXz>> getZhpflXzPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String xl,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zhpfl_xl3");
            }
            return Result.success(reportTableService.getZhpflXzDataPage(tjDate, xl,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取综合赔付率-险种分页失败", e);
        }
    }

    // ==================== 维修单位 (2026-06) ====================
    /** 各支公司产保比 */
    @GetMapping("/zgs_cbb/list")
    public Result<List<AcdZgsCbb>> getZgsCbbList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zgs_cbb");
            }
            return Result.success(reportTableService.getZgsCbbData(tjDate, comnameSgs));
        } catch (Exception e) {
            return Result.error("获取各支公司产保比失败", e);
        }
    }
    @GetMapping("/zgs_cbb/page")
    public Result<PageResult<AcdZgsCbb>> getZgsCbbPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_zgs_cbb");
            }
            return Result.success(reportTableService.getZgsCbbDataPage(tjDate, comnameSgs,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取各支公司产保比分页失败", e);
        }
    }

    /** 维修单位关键指标 */
    @GetMapping("/wxdw_gjzb/list")
    public Result<List<AcdWxdwGjzb>> getWxdwGjzbList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String repairfactoryname) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_wxdw_gjzb");
            }
            return Result.success(reportTableService.getWxdwGjzbData(tjDate, comnameSgs, repairfactoryname));
        } catch (Exception e) {
            return Result.error("获取维修单位关键指标失败", e);
        }
    }
    @GetMapping("/wxdw_gjzb/page")
    public Result<PageResult<AcdWxdwGjzb>> getWxdwGjzbPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false) String repairfactoryname,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_wxdw_gjzb");
            }
            return Result.success(reportTableService.getWxdwGjzbDataPage(tjDate, comnameSgs, repairfactoryname,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取维修单位关键指标分页失败", e);
        }
    }

    // ==================== 车均定损 (2026-06) ====================
    @GetMapping("/chejun_ry/list")
    public Result<List<AcdChejunRy>> getChejunRyList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_ry");
            }
            return Result.success(reportTableService.getChejunRyData(tjDate, comnameSgs));
        } catch (Exception e) {
            return Result.error("获取车均定损-人员失败", e);
        }
    }
    @GetMapping("/chejun_ry/page")
    public Result<PageResult<AcdChejunRy>> getChejunRyPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_ry");
            }
            return Result.success(reportTableService.getChejunRyDataPage(tjDate, comnameSgs,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取车均定损-人员分页失败", e);
        }
    }

    @GetMapping("/chejun_clbm/list")
    public Result<List<AcdChejunClbm>> getChejunClbmList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_clbm");
            }
            return Result.success(reportTableService.getChejunClbmData(tjDate, comnameSgs));
        } catch (Exception e) {
            return Result.error("获取车均定损-处理部门失败", e);
        }
    }
    @GetMapping("/chejun_clbm/page")
    public Result<PageResult<AcdChejunClbm>> getChejunClbmPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_clbm");
            }
            return Result.success(reportTableService.getChejunClbmDataPage(tjDate, comnameSgs,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取车均定损-处理部门分页失败", e);
        }
    }

    @GetMapping("/chejun_bm/list")
    public Result<List<AcdChejunBm>> getChejunBmList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String dsqy) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_bm");
            }
            return Result.success(reportTableService.getChejunBmData(tjDate, dsqy));
        } catch (Exception e) {
            return Result.error("获取车均定损-定损区域失败", e);
        }
    }
    @GetMapping("/chejun_bm/page")
    public Result<PageResult<AcdChejunBm>> getChejunBmPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String dsqy,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_bm");
            }
            return Result.success(reportTableService.getChejunBmDataPage(tjDate, dsqy,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取车均定损-定损区域分页失败", e);
        }
    }

    @GetMapping("/chejun_sgs/list")
    public Result<List<AcdChejunSgs>> getChejunSgsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_sgs");
            }
            return Result.success(reportTableService.getChejunSgsData(tjDate, comnameSgs));
        } catch (Exception e) {
            return Result.error("获取车均定损-市公司失败", e);
        }
    }
    @GetMapping("/chejun_sgs/page")
    public Result<PageResult<AcdChejunSgs>> getChejunSgsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_chejun_sgs");
            }
            return Result.success(reportTableService.getChejunSgsDataPage(tjDate, comnameSgs,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            return Result.error("获取车均定损-市公司分页失败", e);
        }
    }

    /** 未决存量-案件类型 */
    @GetMapping("/wjxs/list")
    public Result<List<AcdWjxs>> getWjxsList(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_wjxs");
            }
            return Result.success(reportTableService.getWjxsData(tjDate, comnameSgs));
        } catch (Exception e) {
            log.error("获取未决存量-案件类型失败", e);
            return Result.error("获取未决存量-案件类型失败", e);
        }
    }
    @GetMapping("/wjxs/page")
    public Result<PageResult<AcdWjxs>> getWjxsPage(
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String comnameSgs,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            if (tjDate == null || tjDate.trim().isEmpty()) {
                tjDate = reportTableService.getMaxTjDate("acd_wjxs");
            }
            return Result.success(reportTableService.getWjxsDataPage(tjDate, comnameSgs,
                    current == null ? 1 : current, size == null ? 20 : size));
        } catch (Exception e) {
            log.error("获取未决存量-案件类型分页失败", e);
            return Result.error("获取未决存量-案件类型分页失败", e);
        }
    }

    // ==================== 下拉去重端点（轻量级） ====================

    /**
     * 取市公司（comname_sgs）去重名单，用于搜索下拉（替代"全量 list"构建下拉的做法）。
     *
     * 三态日期过滤（可组合）：
     *   - 单日：tjDate
     *   - 区间：startDate + endDate（如 cur_gzl 系列）
     *   - 共享表分段：flagValue（jaflag）
     * table 必填，且必须在 TableNames 白名单内。
     */
    @GetMapping("/distinct_comnames")
    public Result<List<String>> getDistinctComnames(
            @RequestParam(required = false) String table,
            @RequestParam(required = false) String tjDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String flagValue) {
        try {
            if (table == null || table.trim().isEmpty()) {
                return Result.error("表名不能为空", null);
            }
            List<String> data = reportTableService.getDistinctComnameSgs(
                    table.trim(), tjDate, startDate, endDate, flagValue);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            log.warn("下拉去重非法参数: {}", e.getMessage());
            return Result.error(e.getMessage(), null);
        } catch (Exception e) {
            log.error("获取市公司去重名单失败", e);
            return Result.error("获取市公司去重名单失败", e);
        }
    }
}
