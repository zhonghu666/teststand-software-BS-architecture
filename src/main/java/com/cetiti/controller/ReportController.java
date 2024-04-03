package com.cetiti.controller;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.BaseJson;
import com.cetiti.constant.Page;
import com.cetiti.entity.Report;
import com.cetiti.service.ReportService;
import com.cetiti.utils.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Api("报告管理")
@RestController
@RequestMapping("/report")
public class ReportController {


    @Autowired
    private ReportService reportService;

    @ApiOperation(value = "分页查询")
    @GetMapping("/queryList/{pageIndex}/{pageSize}")
    public BaseJson<Page<Report>> getReportByQuery(@RequestParam(required = false) String id,
                                                   @RequestParam(required = false) String testSequenceId,
                                                   @PathVariable Integer pageIndex,
                                                   @PathVariable Integer pageSize) {
        return new BaseJson<Page<Report>>().Success(reportService.getReportByQuery(id, testSequenceId, pageIndex, pageSize));
    }

    @ApiOperation(value = "删除报告")
    @DeleteMapping("/remove")
    public BaseJson<Boolean> removeReport(@RequestParam String id) {
        return new BaseJson<Boolean>().Success(reportService.removeReportById(id));
    }

    @ApiOperation(value = "查看报告 根据id获取pdf url")
    @GetMapping("/getUrl")
    public BaseJson<String> getUrlById(@RequestParam String id) {
        return new BaseJson<String>().Success(reportService.getUrlById(id));
    }

    @ApiOperation(value = "下载报告", produces = "application/octet-stream")
    @GetMapping("/download")
    public BaseJson<Boolean> downloadReport(@RequestParam String id,
                                            HttpServletResponse response) {
        return new BaseJson<Boolean>().Success(reportService.downloadReport(id, response));
    }


    @Autowired
    private MinioUtil minioUtil;
    @Resource(name = MongoConfig.MONGO_TEMPLATE)
    private MongoTemplate mongoTemplate;

    //上传 mock
    @PostMapping("/upload")
    public Boolean upload(@RequestPart("file") MultipartFile file,
                          @RequestParam("prefixPath") String prefixPath) {
        try {
            String filePath = minioUtil.upload(file, prefixPath, null);
            Report report = new Report();
            report.setId(UUID.randomUUID().toString());
            report.setCreateTime(LocalDateTime.now());
            report.setName(file.getOriginalFilename());
            if (Objects.equals("pdf", prefixPath)) {
                report.setPdfFilePath(filePath);
            } else if (Objects.equals("word", prefixPath)) {
                report.setWordFilePath(filePath);
            }
            log.info("report:{}", JSON.toJSONString(report));
            mongoTemplate.save(report);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @ApiOperation("mock word url")
    @GetMapping("/mock")
    public BaseJson<String> mock() {
        return new BaseJson<String>().Success(minioUtil.getFileUrl("word/output_1702976650156.docx"));
    }

    @ApiOperation("mock pdf url")
    @GetMapping("/pdf")
    public BaseJson<String> pdf() {
        return new BaseJson<String>().Success(minioUtil.getFileUrl("pdf/output_b_1703068912196.pdf"));
    }


    @ApiOperation("生成报告")
    @GetMapping("/word")
    public String word(@ApiParam(value = "55abaaac-81a5-4940-91e8-e679102f9548") @RequestParam("id") String id) {
        return reportService.generateReport(id);
    }
}
