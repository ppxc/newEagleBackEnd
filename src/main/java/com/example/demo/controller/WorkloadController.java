package com.example.demo.controller;

import com.example.demo.entity.Result;
import com.example.demo.entity.WorkloadDeptData;
import com.example.demo.entity.WorkloadGroupData;
import com.example.demo.entity.WorkloadEmpData;
import com.example.demo.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class WorkloadController {

    private static final Logger log = LoggerFactory.getLogger(WorkloadController.class);

    @Autowired
    private WorkloadService workloadService;

    /**
     * 部门级工作量趋势
     */
    @GetMapping("/workload/department")
    public Result<List<WorkloadDeptData>> getDepartmentWorkload(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String comName,
            @RequestParam(required = false, defaultValue = "month") String granularity
    ) {
        try {
            // 日期默认值
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = workloadService.getMaxTjDate("acd_dangri_gzl_bm");
                if (maxDate == null) {
                    log.warn("表 acd_dangri_gzl_bm 为空（无最大统计日期），返回空列表");
                    return Result.success(Collections.emptyList());
                }
                startDate = maxDate;
                endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<WorkloadDeptData> data = workloadService.getDepartmentWorkload(
                    startDate, endDate, comName, granularity);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取部门工作量失败", e);
            return Result.error("获取部门工作量失败", e);
        }
    }

    /**
     * 小组级工作量趋势
     */
    @GetMapping("/workload/group")
    public Result<List<WorkloadGroupData>> getGroupWorkload(
            @RequestParam(required = false) String comCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String groups,
            @RequestParam(required = false, defaultValue = "month") String granularity
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = workloadService.getMaxTjDate("acd_dangri_gzl_group");
                if (maxDate == null) {
                    log.warn("表 acd_dangri_gzl_group 为空（无最大统计日期），返回空列表");
                    return Result.success(Collections.emptyList());
                }
                startDate = maxDate;
                endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<WorkloadGroupData> data = workloadService.getGroupWorkload(
                    comCode, startDate, endDate, groups, granularity);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取小组工作量失败", e);
            return Result.error("获取小组工作量失败", e);
        }
    }

    /**
     * 员工级工作量趋势（含异常标识）
     */
    @GetMapping("/workload/employee")
    public Result<List<WorkloadEmpData>> getEmployeeWorkload(
            @RequestParam(required = false) String groupsCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false, defaultValue = "month") String granularity
    ) {
        try {
            if ((startDate == null || startDate.trim().isEmpty())
                    && (endDate == null || endDate.trim().isEmpty())) {
                String maxDate = workloadService.getMaxTjDate("acd_dangri_gzl_ry");
                if (maxDate == null) {
                    log.warn("表 acd_dangri_gzl_ry 为空（无最大统计日期），返回空列表");
                    return Result.success(Collections.emptyList());
                }
                startDate = maxDate;
                endDate = maxDate;
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            List<WorkloadEmpData> data = workloadService.getEmployeeWorkload(
                    groupsCode, startDate, endDate, userName, granularity);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取员工工作量失败", e);
            return Result.error("获取员工工作量失败", e);
        }
    }

    /**
     * 根据部门名称获取编码（用于钻取）
     */
    @GetMapping("/workload/comcode")
    public Result<String> getComCodeByName(@RequestParam String comName) {
        try {
            String code = workloadService.getComCodeByName(comName);
            return Result.success(code);
        } catch (Exception e) {
            log.error("获取部门编码失败", e);
            return Result.error("获取部门编码失败", e);
        }
    }

    /**
     * 根据部门编码 + 小组名称获取小组编码（用于钻取时的精确定位）
     */
    @GetMapping("/workload/groupscode")
    public Result<String> getGroupsCodeByComAndName(
            @RequestParam String comCode,
            @RequestParam String groupsName
    ) {
        try {
            String code = workloadService.getGroupsCodeByComAndName(comCode, groupsName);
            return Result.success(code);
        } catch (Exception e) {
            log.error("获取小组编码失败", e);
            return Result.error("获取小组编码失败", e);
        }
    }
}