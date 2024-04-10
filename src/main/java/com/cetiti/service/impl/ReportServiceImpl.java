package com.cetiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.cetiti.async.ReportAsync;
import com.cetiti.config.MongoConfig;
import com.cetiti.config.RestPathConfig;
import com.cetiti.constant.Page;
import com.cetiti.entity.Report;
import com.cetiti.entity.RestResult;
import com.cetiti.entity.TestSequence;
import com.cetiti.service.ReportService;
import com.cetiti.utils.JwtToken;
import com.cetiti.utils.MinioUtil;
import com.cetiti.utils.RedisUtil;
import com.cetiti.utils.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import utils.entity.BusinessException;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Value("${minio.bucketName}")
    private String bucketName;
    @Resource(name = MongoConfig.MONGO_TEMPLATE)
    private MongoTemplate mongoTemplate;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private HttpServletRequest request;
    @Resource
    private ReportAsync reportAsync;
    @Resource
    private RestUtil restUtil;

    @Resource
    private RestPathConfig restPathConfig;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public String saveReport(Report report) {
        Report t = mongoTemplate.save(report);
        return t.getId();
    }

    @Override
    public Report getReportById(String id) {
        return mongoTemplate.findById(id, Report.class);
    }

    @Override
    public String getUrlById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("500", "报告id不能为空");
        }
        Report report = mongoTemplate.findById(id, Report.class);
        if (report == null) {
            throw new BusinessException("500", "报告不存在");
        }
        if (!Objects.equals(report.getStatus(), 1)) {
            throw new BusinessException("500", "报告未生成,请稍后查看!");
        }
//        return minioUtil.getFileUrl(report.getPdfFilePath());
        return "/" + bucketName + "/" + report.getPdfFilePath();
    }

    @Override
    public Boolean removeReportById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("500", "报告id不能为空");
        }
        Report report = getReportById(id);
        if (report == null) {
            throw new BusinessException("500", "报告不存在");
        }
        String wordFilePath = report.getWordFilePath();
        String pdfFilePath = report.getPdfFilePath();
        if (StringUtils.isNotBlank(wordFilePath)) {
            minioUtil.deleteFile(wordFilePath);
        }
        if (StringUtils.isNotBlank(pdfFilePath)) {
            minioUtil.deleteFile(pdfFilePath);
        }
        Query query = Query.query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Report.class);
        return true;
    }

    @Override
    public Page<Report> getReportByQuery(String id, String testSequenceId, Integer pageNum, Integer pageSize) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(id)) {
            criteria.and("id").regex(id);
        }
        if (StringUtils.isNotBlank(testSequenceId)) {
            criteria.and("testSequenceId").is(testSequenceId);
        }

        Query query = new Query(criteria);
        long totalNum = mongoTemplate.count(query, Report.class);

        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        List<Report> reportList = mongoTemplate.find(query.with(pageRequest), Report.class);

        long totalPages = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;

        Page<Report> page = new Page<>();
        page.setCurrentPage(pageNum);
        page.setPageSize(pageSize);
        page.setTotalPage(totalPages);
        page.setTotalNum(totalNum);
        page.setList(reportList);
        return page;
    }

    @Override
    public String generateReport(String id) {
        Date date = new Date();
        String uuid = UUID.randomUUID().toString();
        TestSequence testSequence = mongoTemplate.findById(id, TestSequence.class);
        String sequenceName = Objects.requireNonNull(testSequence).getSequenceName();

        Report report = new Report();
        report.setId(uuid);
        report.setStatus(0);
        report.setTestSequenceId(id);
        report.setTestSequenceName(sequenceName);
        report.setCreateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Asia/Shanghai")));
        report.setUpdateTime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Asia/Shanghai")));
        mongoTemplate.save(report);
        String dataPath = "";
        if (redisUtil.get(id + "recordId") != null) {
            String o = (String) redisUtil.get(id + "recordId");
            o = o.replaceAll("^\"|\"$", "");
            String recordId = "recordId=" + o;
            RestResult resultFromApi = restUtil.getResultFromApi(restPathConfig.getAnalysis() + "/scene/data/getDirectoryStringByRecordId", null, recordId, HttpMethod.GET, "token");
            dataPath = (String) resultFromApi.getData();
            log.info("数据存储路径:入参：{}，返回:{}", redisUtil.get(id + "recordId"), JSON.toJSON(resultFromApi));
        }
        //异步生成报告
        reportAsync.reportTask(id, uuid, sequenceName, JwtToken.getUsername(request.getHeader("token")), dataPath);
        return uuid;
    }

    @Override
    public Boolean downloadReport(String id, HttpServletResponse response) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("500", "报告id不能为空");
        }
        Report report = mongoTemplate.findById(id, Report.class);
        if (report == null) {
            throw new BusinessException("500", "报告不存在");
        }
        if (!Objects.equals(report.getStatus(), 1)) {
            throw new BusinessException("500", "报告未生成,请稍后下载!");
        }
        try (InputStream inputStream = minioUtil.download(report.getWordFilePath())) {
            ServletOutputStream outputStream = response.getOutputStream();
//            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(report.getName(), StandardCharsets.UTF_8));
            byte[] bytes = new byte[4096];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.close();
        } catch (Exception e) {
            log.error("file download from minio exception, fileName: {}", report.getName(), e);
            return false;
        }
        return true;
    }
}
