package com.cetiti.async;

import com.alibaba.fastjson.JSON;
import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.SaveFormat;
import com.cetiti.config.MongoConfig;
import com.cetiti.config.RestPathConfig;
import com.cetiti.constant.StepStatus;
import com.cetiti.entity.Report;
import com.cetiti.entity.RestResult;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.TestSequence;
import com.cetiti.entity.step.SequenceCallStep;
import com.cetiti.service.impl.CacheService;
import com.cetiti.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import utils.entity.BusinessException;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
public class ReportAsync {

    @Resource(name = MongoConfig.MONGO_TEMPLATE)
    private MongoTemplate mongoTemplate;
    @Resource
    private MinioUtil minioUtil;
    @Resource
    private CacheService cacheService;
    @Resource
    private RedisUtil redisUtil;


    /**
     * @param id           序列id
     * @param uuid         报告uuid
     * @param sequenceName 序列名称
     * @param username     执行用户
     */
    @Async
    public void reportTask(String id, String uuid, String sequenceName, String username, String dataPath) {
        try {
            log.info("异步开始.....");
            //缓存
            StepVariable stepVariable = cacheService.getStepVariable(id);
//            log.info("stepVariable:{}", JSON.toJSONString(stepVariable));

            // 创建一个新的Word文档
            XWPFDocument document = new XWPFDocument();
            //设置A4纸
            WordUtil.setPaper(document);
            //设置标题和第一个表格

            WordUtil.setTitleAndSummaryTable(document);
            //换行
            WordUtil.setBreak(document);
            //表格上方文字
            WordUtil.setParagraphText(document, "【主序列】步骤详情");
            //设置表格表头
            XWPFTable table = WordUtil.setTableHeader(document);
            //创建表格
            List<String> childSequenceId = CreateWord.word(table, stepVariable, sequenceName);
            WordUtil.setParagraphText(document, "【主序列】步骤详情 End");
            WordUtil.setBreak(document);

            Set<String> processedIds = new HashSet<>(childSequenceId); // 使用 HashSet 追踪已处理的 ID
            int index = 0;
            while (index < childSequenceId.size()) {
                String sequenceId = childSequenceId.get(index);
                if (!processedIds.contains(sequenceId)) {
                    TestSequence childSequence = mongoTemplate.findById(sequenceId, TestSequence.class);
                    WordUtil.setParagraphText(document, "【" + childSequence.getSequenceName() + "子序列】步骤详情");
                    XWPFTable childTable = WordUtil.setTableHeader(document);
                    List<String> newIds = CreateWord.word(childTable, cacheService.getStepVariable(sequenceId), Objects.requireNonNull(childSequence).getSequenceName());
                    childSequenceId.addAll(newIds);
                    processedIds.addAll(newIds); // 添加新的 ID 到已处理集合
                    WordUtil.setParagraphText(document, "【" + childSequence.getSequenceName() + "子序列】步骤详情 End");
                    WordUtil.setBreak(document);
                }
                index++;
            }

            Date date = new Date();
            String reportName = sequenceName + "测试报告_" + DateUtils.date2String(date, DateUtils.YYYYMMDD_HHMMSS) + ".docx";
            log.info("username:{}", username);
            String time = DateUtils.convertTimestampToDHMS(redisUtil.getElapsedTime(id));
            log.info("time:{}", time);
            String result = stepVariable.getValueByPath("RunState.SequenceStatus");

            String wordPath = "word/" + sequenceName + "测试报告_" + date.getTime() + ".docx";
            String dateStr = DateUtils.date2String(date, DateUtils.YYYY_MM_DD_HH_MM_SS);
            List<String> resultList = Arrays.asList(uuid, reportName, sequenceName, username, time, StepStatus.getDescByCode(result), dataPath, dateStr);
            //填充summary表格数据
            WordUtil.fillFirstTable(document, resultList);

            //上传word
            byte[] wordBytes = null;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.write(outputStream);
                wordBytes = outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert wordBytes != null;
            byte[] newBytes = Arrays.copyOf(wordBytes, wordBytes.length);
            minioUtil.upload(wordBytes, wordPath);

            //上传pdf
            byte[] pdfBytes = null;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(newBytes);
                 ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
                com.aspose.words.Document doc = new Document(inputStream);

                FontSettings fontSettings = FontSettings.getDefaultInstance();
                fontSettings.setFontsFolder("/usr/share/fonts/test" + File.separator, true);

                doc.setFontSettings(fontSettings);
                doc.save(pdfOutputStream, SaveFormat.PDF);
                pdfBytes = pdfOutputStream.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pdfPath = "pdf/" + sequenceName + "测试报告_" + System.currentTimeMillis() + ".pdf";
            minioUtil.upload(pdfBytes, pdfPath);


            //更新mongo记录
            Query query = new Query(Criteria.where("id").is(uuid));
            Update update = new Update();
            update.set("status", 1);
            update.set("name", reportName);
            update.set("wordFilePath", wordPath);
            update.set("pdfFilePath", pdfPath);
            update.set("updateTime", LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
            mongoTemplate.updateFirst(query, update, Report.class);
            log.info("异步结束.....");
        } catch (Exception e) {
            //删除记录
            Report report = mongoTemplate.findById(uuid, Report.class);
            if (Objects.nonNull(report)) {
                String wordFilePath = report.getWordFilePath();
                String pdfFilePath = report.getPdfFilePath();
                if (StringUtils.isNotBlank(wordFilePath)) {
                    minioUtil.deleteFile(wordFilePath);
                }
                if (StringUtils.isNotBlank(pdfFilePath)) {
                    minioUtil.deleteFile(pdfFilePath);
                }
                Query query = Query.query(Criteria.where("id").is(uuid));
                mongoTemplate.remove(query, Report.class);
            }
            log.error("生成报告异常: ", e);
            throw new BusinessException("500", "生成报告异常,请联系管理员!");
        } finally {
            //删除缓存
            //testSequenceService.removeCache(id);
          /*  cacheService.deleteStepVariable(id);
            cacheService.deleteStepVariable("SequenceData-" + id);
            redisUtil.del(id + "Main");
            List<SequenceCallStep> sequenceCallList = mongoTemplate.find(new Query().addCriteria(Criteria.where("testSequenceId").is(id).and("stepType").is("SEQUENCE_CALL")), SequenceCallStep.class);
            sequenceCallList.forEach(s -> {
                cacheService.deleteStepVariable(s.getChildTestSequenceId());
                cacheService.deleteStepVariable("SequenceData-" + s.getChildTestSequenceId());
                redisUtil.del(s.getChildTestSequenceId() + "Main");
            });
            //删除执行时间
            redisUtil.del(id + ":start");
            redisUtil.del(id + ":elapsed");*/
        }
    }
}
