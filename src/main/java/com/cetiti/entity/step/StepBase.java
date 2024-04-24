package com.cetiti.entity.step;

import com.cetiti.constant.ErrorCode;
import com.cetiti.constant.SpinType;
import com.cetiti.constant.StepStatus;
import com.cetiti.entity.CircularConfig;
import com.cetiti.entity.StepAdditional;
import com.cetiti.entity.StepVariable;
import com.cetiti.expression.ExpressionParserUtils;
import com.cetiti.service.impl.CacheService;
import com.cetiti.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import javax.script.ScriptException;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cetiti.constant.SpinType.CANNED_CYCLE;

@Data
@Document(collection = "Step")
@ApiModel("步骤基本属性")
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LabelStep.class, name = "N_LABEL"),
        @JsonSubTypes.Type(value = WaitStep.class, name = "N_WAIT"),
        @JsonSubTypes.Type(value = PopupStep.class, name = "N_MESSAGE_POPUP"),
        @JsonSubTypes.Type(value = SequenceCallStep.class, name = "N_SEQUENCE_CALL"),
        @JsonSubTypes.Type(value = FlowControlStep.class, name = "N_FLOW_CONTROL"),
        @JsonSubTypes.Type(value = StatementStep.class, name = "N_STATEMENT"),
        @JsonSubTypes.Type(value = ActionStep.class, name = "N_ACTION"),
        @JsonSubTypes.Type(value = TestStep.class, name = "N_TEST"),
        @JsonSubTypes.Type(value = DataCallStep.class, name = "N_DATA_CALL")
})
public abstract class StepBase implements Serializable {

    @Indexed(unique = true)
    protected String id;

    @ApiModelProperty("测试序列Id")
    @NotNull(message = "测试序列Id不为空")
    protected String testSequenceId;

    @ApiModelProperty("测试序列名称")
    @NotNull(message = "测试序列名称不为空")
    protected String testSequenceName;

    @ApiModelProperty("步骤名称")
    @NotNull(message = "步骤名称不为空")
    protected String name;

    @ApiModelProperty("反序列化类型-不入库")
    private String type;

    @ApiModelProperty("步骤类型")
    private String stepType;

    @ApiModelProperty("作用域")
    protected String scope;

    @ApiModelProperty("描述")
    private String describe;

    @ApiModelProperty("注释")
    private String comment;

    @ApiModelProperty(value = "运行模式：OPERATING_MODE")
    private Integer operatingMode;

    @ApiModelProperty("结果是否记录: 0记录,1不记录")
    private Integer resultRecordStatus;

    @ApiModelProperty("失败是否中断:0不中断,1中断")
    private Integer interruptProcessStatus;

    @ApiModelProperty("是否忽略错误:1忽略，0不忽略")
    private Integer tryCatchStatus;

    @ApiModelProperty("循环配置")
    private CircularConfig circularConfig;

    @ApiModelProperty("步骤状态表达式")
    private String stepStatusExpression;

    @ApiModelProperty("先决条件表达式")
    private String prerequisitesExpression;

    @ApiModelProperty("额外结果")
    private List<StepAdditional> stepAdditionalList;

    @ApiModelProperty("断点")
    private Boolean breakpoint;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

    /**
     * 步骤执行基本方法-入口逻辑处理通用属性相关逻辑
     *
     * @param cacheService 缓存服务
     * @param pram         部分步骤所需入参
     * @return
     */
    public final StepVariable execute(CacheService cacheService, Map<String, Object> pram) {
        StepVariable step = new StepVariable();
        StepVariable stepVariable = cacheService.getStepVariable(testSequenceId);
        try {
            //判断运行模式
            switch (operatingMode) {
                case 0:
                    //先决条件判断
                    if (evaluatePrerequisitesExpression(stepVariable, cacheService)) {
                        //自旋循环逻辑
                        if (circularConfig == null || SpinType.NONE.equals(circularConfig.getSpinType())) {
                            step = performSpecificTask(cacheService, pram);
                            step.addNestedAttribute("LoopType", SpinType.NONE.name(), "循环类型");
                        } else {
                            step = customLoop(cacheService, stepVariable, pram);
                            step.addNestedAttribute("LoopType", circularConfig.getSpinType().name(), "循环类型");
                        }
                        stepVariable = cacheService.getStepVariable(testSequenceId);
                    }
                    break;
                case 1:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.SKIPPED);
                    break;
                case 2:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.PASSED);
                    break;
                default:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.FAILED);
                    break;
            }
            if (StringUtils.isNotBlank(stepStatusExpression)) {
                ExpressionParserUtils.currencyExecution("Result.Status = " + stepStatusExpression, stepVariable, cacheService, testSequenceId);
            }
            if (!CollectionUtils.isEmpty(stepAdditionalList)) {
                for (StepAdditional i : stepAdditionalList) {
                    if (!i.getIsGraphs()) {
                        // todo 没有加类型校验，后续补充
                        Map<String, Object> response = ExpressionParserUtils.expressionParsingExecution(i.getSourceExpression(), stepVariable, cacheService, testSequenceId);
                        Object result = response.get("result");
                        if (result != null) {
                            step.addNestedAttribute("ExtraResults." + i.getId(), result, i.getTargetExpression());
                        }
                    }
                }
            }
            step.addNestedAttribute("type", type, "步骤类型");
        } catch (Exception e) {
            log.error("步骤:{}执行异常", id, e);
            step = StepVariable.RESULT_Fail(StepStatus.ERROR, e.getMessage());
            if (Objects.equals(tryCatchStatus, 0)) {
                step.addNestedAttribute("Result.Error.Occurred", false, "Occurred");
            }
        } finally {
            String statusCode = step.getValueByPath("Result.Status");
            String runStateStatus = stepVariable.getValueByPath("RunState.SequenceStatus");
            if (!StepStatus.ERROR.getCode().equals(runStateStatus) && !StepStatus.TERMINATED.getCode().equals(runStateStatus) && !StepStatus.FAILED.getCode().equals(runStateStatus)) {
                if (interruptProcessStatus == null || (Objects.equals(interruptProcessStatus, 0)) && statusCode.equals(StepStatus.FAILED.getCode())) {
                    stepVariable.addNestedAttribute("RunState.SequenceError.code", ErrorCode.SUCCESS.getCode(), "SequenceErrorCode");
                    stepVariable.addNestedAttribute("RunState.SequenceError.Msg", ErrorCode.SUCCESS.getDesc(), "SequenceErrorCode");
                    stepVariable.addNestedAttribute("RunState.SequenceError.Occurred", ErrorCode.SUCCESS.getOccurred(), "SequenceErrorCode");
                    stepVariable.addNestedAttribute("RunState.SequenceStatus", StepStatus.PASSED.getCode(), "SequenceStatus");
                } else {
                    StepVariable error = step.getValueByPath("Result.Error");
                    stepVariable.addNestedAttribute("RunState.SequenceError", error, null);
                    if (StepStatus.ERROR.getCode().equals(statusCode) || StepStatus.TERMINATED.getCode().equals(statusCode) || StepStatus.FAILED.getCode().equals(statusCode)) {
                        stepVariable.addNestedAttribute("RunState.SequenceStatus", statusCode, "SequenceStatus");
                    } else {
                        stepVariable.addNestedAttribute("RunState.SequenceStatus", StepStatus.PASSED.getCode(), "SequenceStatus");
                    }
                }
            }
        }
        //插入RunState 内容
        step.addNestedAttribute("resultRecordStatus", resultRecordStatus != null ? resultRecordStatus : 1, "是否进入报告");
        String stepPath = "RunState.SequenceFile.Data.Seq." + testSequenceName + "." + scope + "." + name + "[" + id + "]";
        stepVariable.addNestedAttribute(stepPath, step, name);
        String reportStepPath = "RunState.SequenceFile.Report." + testSequenceName + "." + scope + "." + name + "[" + id + "]";
        if (stepVariable.getValueByPath(reportStepPath + "0") == null) {
            step.addNestedAttribute("no", 0, "序号");
            stepVariable.addNestedAttribute(reportStepPath + "0", step, "报告步骤");
        } else {
            Integer no = stepVariable.getValueByPath(reportStepPath + "0.no");
            Integer nextNo = no + 1;
            stepVariable.addNestedAttribute(reportStepPath + "0.no", nextNo, "序号");
            stepVariable.addNestedAttribute(reportStepPath + nextNo, step, "报告步骤");
        }
        cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable);
        return step;
    }

    protected StepVariable customLoop(CacheService cacheService, StepVariable stepVariable, Map<String, Object> pram) throws ScriptException {
        String passPath = "RunState.LoopNumPassed";
        String failedPath = "RunState.LoopNumFailed";
        String allPath = "RunState.LoopNumIterations";
        StepVariable forLoop = StepVariable.RESULT_SUCCESS(StepStatus.PASSED);
        stepVariable.addNestedAttribute(passPath, 0, "步骤自旋成功次数");
        stepVariable.addNestedAttribute(failedPath, 0, "步骤自旋失败次数");
        ExpressionParserUtils.parseAndExecuteForLoop(circularConfig, cacheService, testSequenceId,
                () -> {
                    long startTime = System.currentTimeMillis();
                    StepVariable step = performSpecificTask(cacheService, pram);
                    String status = step.getValueByPath("Result.Status");
                    Double allNum = stepVariable.getValueByPath(allPath) != null ? stepVariable.getValueByPath(allPath) : 0.0;
                    stepVariable.addNestedAttribute(allPath, allNum + 1.0, "步骤自旋次数");
                    if (status.equals(StepStatus.PASSED.getCode()) || status.equals(StepStatus.DONE.getCode())) {
                        Integer passNum = stepVariable.getValueByPath(passPath) != null ? stepVariable.getValueByPath(passPath) : 0;
                        stepVariable.addNestedAttribute(passPath, passNum + 1, "步骤自旋成功次数");
                    } else if (status.equals(StepStatus.FAILED.getCode()) || status.equals(StepStatus.ERROR.getCode()) || status.equals(StepStatus.TERMINATED.getCode())) {
                        Integer failedNum = stepVariable.getValueByPath(failedPath) != null ? stepVariable.getValueByPath(failedPath) : 0;
                        stepVariable.addNestedAttribute(failedPath, failedNum + 1, "步骤自旋失败次数");
                    }
                    forLoop.addNestedAttribute("stepCopy", step, "步骤执行结果备份");
                    if (circularConfig.getRecordRes() != null && circularConfig.getRecordRes()) {
                        forLoop.addToListAtPath("ForLoop", step);
                    }
                    log.info("自旋单次耗时:{}", System.currentTimeMillis() - startTime);
                    return step;
                });
        forLoop.addNestedAttribute("LoopNumPassed", stepVariable.getValueByPath(passPath) != null ? stepVariable.getValueByPath(passPath) : 0, "步骤自旋成功次数");
        forLoop.addNestedAttribute("LoopNumFailed", stepVariable.getValueByPath(failedPath) != null ? stepVariable.getValueByPath(failedPath) : 0, "步骤自旋失败次数");
        forLoop.addNestedAttribute("LoopNumIterations", stepVariable.getValueByPath(allPath) != null ? stepVariable.getValueByPath(allPath) : 0.0, "步骤自旋次数");
        boolean b = ExpressionParserUtils.conditionalExecution(circularConfig.getResultExpression(), stepVariable, cacheService, testSequenceId) == 1;
        forLoop.addNestedAttribute("LoopStatus", b, "自旋结果");
        if (circularConfig.getSpinType().equals(CANNED_CYCLE) == b) {
            forLoop.addNestedAttribute("Result.Status", StepStatus.FAILED.getCode(), "Status");
        }
        return forLoop;
    }

    /**
     * 先决条件
     *
     * @return
     */
    protected boolean evaluatePrerequisitesExpression(StepVariable stepVariable, CacheService cacheService) throws InterruptedException {
        if (StringUtils.isBlank(prerequisitesExpression)) {
            return true;
        }
        int i = 0;
        while (ExpressionParserUtils.conditionalExecution(prerequisitesExpression, stepVariable, cacheService, testSequenceId) != 1 && i < 1000) {
            Thread.sleep(10);
            i++;
        }
        return i <= 1000;
    }

    // This method will be overridden by specific subclasses to provide their task logic
    protected abstract StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram);

    protected StepBase() {
        this.type = determineType();
    }

    private String determineType() {
        if (this instanceof LabelStep) {
            return "N_LABEL";
        } else if (this instanceof WaitStep) {
            return "N_WAIT";
        } else if (this instanceof PopupStep) {
            return "N_MESSAGE_POPUP";
        } else if (this instanceof SequenceCallStep) {
            return "N_SEQUENCE_CALL";
        } else if (this instanceof FlowControlStep) {
            return "N_FLOW_CONTROL";
        } else if (this instanceof StatementStep) {
            return "N_STATEMENT";
        } else if (this instanceof ActionStep) {
            return "N_ACTION";
        } else if (this instanceof TestStep) {
            return "N_TEST";
        } else if (this instanceof DataCallStep) {
            return "N_DATA_CALL";
        }
        // 如果没有匹配的类型，可以返回 null 或抛出异常
        return null;
    }

}

