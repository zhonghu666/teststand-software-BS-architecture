package com.cetiti.service;

import com.cetiti.constant.Page;
import com.cetiti.entity.Report;

import javax.servlet.http.HttpServletResponse;


public interface ReportService {

    /**
     * 保存报告
     *
     * @param report
     * @return
     */
    String saveReport(Report report);

    /**
     * 根据ID查询报告
     *
     * @param id
     * @return
     */
    Report getReportById(String id);

    String getUrlById(String id);

    /**
     * 删除报告
     *
     * @param id
     * @return
     */
    Boolean removeReportById(String id);

    Page<Report> getReportByQuery(String id, String testSequenceId, Integer pageNum, Integer pageSize);

    /**
     * 生成报告
     *
     * @param id 序列id
     * @return url
     */
    String generateReport(String id);

    Boolean downloadReport(String id, HttpServletResponse response);

}
