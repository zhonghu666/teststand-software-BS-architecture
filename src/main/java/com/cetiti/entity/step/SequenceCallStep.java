package com.cetiti.entity.step;

import com.cetiti.config.ApplicationContextHolder;
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
        while (iterator.hasNext()) {
            StepBase currentStep = iterator.next();
            if (currentStep instanceof DataCallStep) {
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
        redisUtil.del(pram.get("exceptVersion") + ":" + childTestSequenceId + "execute");
        StepVariable childStepVariable = cacheService.getStepVariable(childTestSequenceId);
        String runStatus = childStepVariable.getValueByPath("RunState.SequenceStatus");
        StepVariable step;
        if (StepStatus.PASSED.getCode().equals(runStatus)) {
            step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        } else {
            step = StepVariable.RESULT_Fail(StepStatus.FAILED);
        }
        step.addNestedAttribute("childTestSequenceId", childTestSequenceId, "子序列Id");
        return step;
    }

    private void get(List<StepBase> stepBaseList) {
        boolean[] keepStep = new boolean[stepBaseList.size()];
        Arrays.fill(keepStep, false);
        Set<FlowControlType> subTypesToSkip = new HashSet<>(Arrays.asList(F_END, F_CONTINUE, F_BREAK, F_GOTO, F_SELECT));
        for (StepBase step : stepBaseList) {
            if ("FLOW_CONTROL".equals(step.getStepType()) && step instanceof FlowControlStep) {
                if (subTypesToSkip.contains(((FlowControlStep) step).getSubType())) {
                    continue;
                }
                FlowControlStep flowControlStep = (FlowControlStep) step;
                int startIndex = flowControlStep.getStartIndex();
                int endIndex = flowControlStep.getEndIndex();
                for (int i = startIndex; i <= endIndex && i < stepBaseList.size(); i++) {
                    keepStep[i] = true;
                }
            }
        }
        for (int i = stepBaseList.size() - 1; i >= 0; i--) {
            if (keepStep[i]) {
                stepBaseList.remove(i);
            }
        }
    }

}
