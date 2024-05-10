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

    /**
     * 根据id获取报告链接
     *
     * @param id
     * @return
     */
    String getUrlById(String id);

    /**
     * 删除报告
     *
     * @param id
     * @return
     */
    Boolean removeReportById(String id);

    /**
     * 统计查询报告
     *
     * @param id             id
     * @param testSequenceId 序列id
     * @param pageNum        第几页
     * @param pageSize       每页条数
     * @return
     */
    Page<Report> getReportByQuery(String id, String testSequenceId, Integer pageNum, Integer pageSize);

    /**
     * 生成报告
     *
     * @param id 序列id
     * @return url
     */
    String generateReport(String id);

    /**
     * 下载报告
     *
     * @param id       报告id
     * @param response 返回
     * @return 是否成功
     */
    Boolean downloadReport(String id, HttpServletResponse response);

}
