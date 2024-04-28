package com.cetiti.entity.step;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.IMqttSender;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.Assert;
import com.cetiti.constant.FlowControlType;
import com.cetiti.constant.StepStatus;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.TestSequence;
import com.cetiti.service.impl.CacheService;
import com.cetiti.utils.RedisUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.cetiti.constant.FlowControlType.*;


@EqualsAndHashCode(callSuper = true)
@Data
public class SequenceCallStep extends StepBase {

    @ApiModelProperty("子序列Id")
    @NotNull(message = "子序列Id不能为空")
    private String childTestSequenceId;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        if (pram.get("intoStatus") != null) {
            StepVariable step;
            StepVariable stepVariable = cacheService.getStepVariable(childTestSequenceId);
            String childTestSequenceStatus = stepVariable.getValueByPath("RunState.SequenceStatus");
            if (StepStatus.PASSED.getCode().equals(childTestSequenceStatus)) {
                step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
            } else {
                String errorMsg = stepVariable.getValueByPath("Result.Error.Msg");
                step = StepVariable.RESULT_Fail(StepStatus.FAILED, errorMsg != null ? errorMsg : "");
            }
            StepVariable error = stepVariable.getValueByPath("RunState.SequenceError");
            step.addNestedAttribute("Result.Error", error, "");
            return step;
        }
        MongoTemplate mongoTemplate = ApplicationContextHolder.getBean(MongoConfig.MONGO_TEMPLATE, MongoTemplate.class);
        List<StepBase> stepBases = mongoTemplate.find(new Query().addCriteria(Criteria.where("testSequenceId").is(childTestSequenceId)), StepBase.class);
        if (cacheService.getStepVariable(childTestSequenceId) == null) {
            TestSequence testSequence = mongoTemplate.findById(childTestSequenceId, TestSequence.class);
            Assert.handle(testSequence != null, "序列不存在");
            cacheService.saveOrUpdateStepVariable(childTestSequenceId, testSequence.getStepVariable());
        }
        RedisUtil redisUtil = ApplicationContextHolder.getBean(RedisUtil.class);
        Assert.handle(redisUtil.checkIfKeyExistsWithScan(childTestSequenceId + "execute") == null, "子序列在执行中");
        redisUtil.set(pram.get("exceptVersion") + ":" + childTestSequenceId + "execute", "子序列");
        cacheService.saveOrUpdateStep(childTestSequenceId, stepBases);
        get(stepBases);
        pram.put("executeType", 1);
        Iterator<StepBase> iterator = stepBases.iterator();
        Boolean dataCallFlag = false;
        boolean currentBlockExecuted = false; // 跟踪当前条件块是否已执行
        boolean selectBlockExecuted = false;  // 跟踪当前select-case块是否有case已执行

        while (iterator.hasNext()) {
            StepBase currentStep = iterator.next();
            String stepType = currentStep.getType(); // 获取当前步骤的类型
            if (stepType.equals("N_FLOW_CONTROL")) {
                FlowControlStep flowControlStep = (FlowControlStep) currentStep;
                FlowControlType subType = flowControlStep.getSubType();
                // 检测是否需要重置状态（遇到新的条件块或非条件块的步骤）
                if (subType.equals(F_IF) || (!subType.equals(F_ELSE_IF) && !subType.equals(F_ELSE) && currentBlockExecuted)) {
                    currentBlockExecuted = false; // 重置当前条件块的执行状态
                }
                if (subType.equals(F_SELECT) || (!subType.equals(F_CASE) && selectBlockExecuted)) {
                    selectBlockExecuted = false; // 重置当前select-case块的执行状态
                }
                // 处理条件块和select-case块
                if (subType.equals(F_IF) || subType.equals(F_ELSE_IF) || subType.equals(F_ELSE)) {
                    if (!currentBlockExecuted) {
                        StepVariable executeResult = currentStep.execute(cacheService, pram);
                        Boolean FlowStatus = executeResult.getValueByPath("FlowStatus");
                        if (FlowStatus != null && FlowStatus) {
                            currentBlockExecuted = true; // 标记当前条件块已执行
                        }
                    }
                } else if (subType.equals(F_CASE)) {
                    if (!selectBlockExecuted) {
                        StepVariable executeResult = currentStep.execute(cacheService, pram);
                        Boolean FlowStatus = executeResult.getValueByPath("FlowStatus");
                        if (FlowStatus != null && FlowStatus) {
                            selectBlockExecuted = true; // 标记当前条件块已执行
                        }
                    }
                } else {
                    // 处理非条件和非case步骤
                    if (currentStep instanceof DataCallStep) {
                        dataCallFlag = true;
                        pram.put("DATA_CALL_TOPIC", "guoqi/scene/auto/sub/command");
                    }
                    StepVariable executeResult = currentStep.execute(cacheService, pram);
                    String gotoId = executeResult.getValueByPath("GotoId");
                    if (StringUtils.isNotBlank(gotoId)) {
                        iterator = stepBases.stream()
                                .filter(step -> gotoId.equals(step.getId()))
                                .findFirst()
                                .map(step -> stepBases.iterator())
                                .orElseThrow(() -> new RuntimeException("Goto ID not found"));
                    }
                }
            } else {
                // 处理非条件和非case步骤
                if (currentStep instanceof DataCallStep) {
                    dataCallFlag = true;
                    pram.put("DATA_CALL_TOPIC", "guoqi/scene/auto/sub/command");
                }
                StepVariable executeResult = currentStep.execute(cacheService, pram);
                String gotoId = executeResult.getValueByPath("GotoId");
                if (StringUtils.isNotBlank(gotoId)) {
                    iterator = stepBases.stream()
                            .filter(step -> gotoId.equals(step.getId()))
                            .findFirst()
                            .map(step -> stepBases.iterator())
                            .orElseThrow(() -> new RuntimeException("Goto ID not found"));
                }
            }
        }
        if (dataCallFlag) {
            DataCallStencil dataCallStencil = new DataCallStencil();
            dataCallStencil.setTestStart(false);
            dataCallStencil.setId(childTestSequenceId);
            IMqttSender iMqttSender = ApplicationContextHolder.getBean(IMqttSender.class);
            iMqttSender.sendToMqtt("guoqi/scene/auto/sub/command", JSON.toJSONString(dataCallStencil));
        }
        redisUtil.del(pram.get("exceptVersion") + ":" + childTestSequenceId + "execute");
        StepVariable childStepVariable = cacheService.getStepVariable(childTestSequenceId);
        String runStatus = childStepVariable.getValueByPath("RunState.SequenceStatus");
        StepVariable step;
        if (StepStatus.PASSED.getCode().equals(runStatus)) {
            step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        } else {
            String errorMsg = childStepVariable.getValueByPath("Result.Error.Msg");
            step = StepVariable.RESULT_Fail(StepStatus.FAILED, errorMsg != null ? errorMsg : "");
        }
        step.addNestedAttribute("childTestSequenceId", childTestSequenceId, "子序列Id");
        return step;
    }

    /**
     * 筛选步骤，把流控包含的步骤删除
     *
     * @param stepBaseList 序列步骤列表
     */
    private void get(List<StepBase> stepBaseList) {
        boolean[] keepStep = new boolean[stepBaseList.size()];
        Arrays.fill(keepStep, false);
        Set<FlowControlType> subTypesToSkip = new HashSet<>(Arrays.asList(F_END, F_CONTINUE, F_BREAK, F_GOTO, F_SELECT));

        // 临时存储最多一个类型为SEQUENCE_CALL的步骤及其索引
        StepBase sequenceCall = null;
        int sequenceCallIndex = -1;

        // 第一次遍历寻找SEQUENCE_CALL步骤
        for (int i = 0; i < stepBaseList.size(); i++) {
            StepBase step = stepBaseList.get(i);
            if ("DATA_CALL".equals(step.getStepType())) {
                sequenceCall = step;
                sequenceCallIndex = i;
                break; // 因为只可能有一个，找到后即可退出循环
            }
        }

        // 如果存在，从原列表中移除SEQUENCE_CALL步骤
        if (sequenceCall != null) {
            stepBaseList.remove(sequenceCallIndex);
        }

        // 遍历处理其他步骤
        for (StepBase step : stepBaseList) {
            if ("FLOW_CONTROL".equals(step.getStepType()) && step instanceof FlowControlStep) {
                if (subTypesToSkip.contains(((FlowControlStep) step).getSubType())) {
                    continue;
                }
                FlowControlStep flowControlStep = (FlowControlStep) step;
                int startIndex = flowControlStep.getStartIndex();
                int endIndex = flowControlStep.getEndIndex();
                for (int i = startIndex + 1; i < endIndex && i < stepBaseList.size(); i++) {
                    keepStep[i] = true;
                }
            }
        }

        // 反向遍历并移除未被标记保留的步骤
        for (int i = stepBaseList.size() - 1; i >= 0; i--) {
            if (keepStep[i]) {
                stepBaseList.remove(i);
            }
        }

        // 如果存在，将SEQUENCE_CALL步骤放回原来的位置
        if (sequenceCall != null) {
            stepBaseList.add(sequenceCallIndex, sequenceCall);
        }
    }


}
