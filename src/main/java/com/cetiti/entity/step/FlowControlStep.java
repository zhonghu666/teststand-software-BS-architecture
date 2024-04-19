package com.cetiti.entity.step;

import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.MongoConfig;
import com.cetiti.constant.Assert;
import com.cetiti.constant.FlowControlType;
import com.cetiti.constant.SpinType;
import com.cetiti.constant.StepStatus;
import com.cetiti.entity.CircularConfig;
import com.cetiti.entity.StepVariable;
import com.cetiti.expression.ExpressionParserUtils;
import com.cetiti.service.impl.CacheService;
import com.cetiti.utils.RedisUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cetiti.constant.FlowControlType.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class FlowControlStep extends StepBase {

    @ApiModelProperty("流控类型")
    private FlowControlType subType;
    @ApiModelProperty("for循环类型")
    private SpinType forType;
    @ApiModelProperty("case类型对应selectId")
    private String selectId;
    @ApiModelProperty("表达式")
    private String condition;
    @ApiModelProperty("流控内部步骤开始索引")
    private Integer startIndex;
    @ApiModelProperty("流控内部步骤结束索引")
    private Integer endIndex;
    @ApiModelProperty("goto步骤Id")
    private String gotoStepId;
    @ApiModelProperty("是否为default")
    private Boolean defaultCase;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        StepVariable step;
        StepVariable stepVariable = cacheService.getStepVariable(getTestSequenceId());
        RedisUtil redisUtil = ApplicationContextHolder.getBean(RedisUtil.class);
        MongoTemplate mongoTemplate = ApplicationContextHolder.getBean(MongoConfig.MONGO_TEMPLATE, MongoTemplate.class);
        String key = testSequenceId + "_" + getId();
        if (pram != null && pram.get("executeType") != null) {
            boolean breakFlag = false;
            step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
            switch (subType) {
                case F_IF:
                case F_ELSE_IF:
                    if (ExpressionParserUtils.conditionalExecution(condition, stepVariable, cacheService, testSequenceId) == 1) {
                        for (StepBase stepBase : cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex)) {
                            stepBase.execute(cacheService, pram);
                        }
                    }
                    break;
                case F_WHILE:
                    while (ExpressionParserUtils.conditionalExecution(condition, stepVariable, cacheService, testSequenceId) == 1) {
                        for (StepBase stepBase : cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex)) {
                            StepVariable execute = stepBase.execute(cacheService, pram);
                            if (stepBase.getType().equals("N_FLOW_CONTROL")) {
                                Boolean flag = execute.getValueByPath("FlowStatus");
                                String subType = execute.getValueByPath("subType");
                                if (F_CONTINUE.name().equals(subType) && flag) {
                                    break;
                                } else if (F_BREAK.name().equals(subType) && flag) {
                                    breakFlag = true;
                                    break;
                                }
                            }
                        }
                        if (breakFlag) {
                            break;
                        }
                    }
                    break;
                case F_DO_WHILE:
                    do {
                        for (StepBase stepBase : cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex)) {
                            StepVariable execute = stepBase.execute(cacheService, pram);
                            if (stepBase.getType().equals("N_FLOW_CONTROL")) {
                                Boolean flag = execute.getValueByPath("FlowStatus");
                                String subType = execute.getValueByPath("subType");
                                if (F_CONTINUE.name().equals(subType) && flag) {
                                    break;
                                } else if (F_BREAK.name().equals(subType) && flag) {
                                    breakFlag = true;
                                    break;
                                }
                            }
                        }
                        if (breakFlag) {
                            break;
                        }
                    } while (ExpressionParserUtils.conditionalExecution(condition, stepVariable, cacheService, testSequenceId) == 1);
                    break;
                case F_CONTINUE:
                case F_BREAK:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                    step.addNestedAttribute("FlowStatus", ExpressionParserUtils.conditionalExecution(condition, stepVariable, cacheService, testSequenceId), "流控表达式结果");
                    step.addNestedAttribute("subType", ExpressionParserUtils.conditionalExecution(condition, stepVariable, cacheService, testSequenceId), "流控子类");
                    break;
                case F_FOR:
                    String[] expressions = condition.split(";");
                    if (expressions.length != 3) {
                        throw new IllegalArgumentException("Expression must contain initialization, condition, and increment.");
                    }
                    CircularConfig circularConfig = new CircularConfig();
                    circularConfig.setInitializationExpression(expressions[0]);
                    circularConfig.setWhileExpression(expressions[1]);
                    circularConfig.setIncrementExpression(expressions[2]);
                    try {
                        StepVariable finalStep = step;
                        ExpressionParserUtils.parseAndExecuteForLoop(circularConfig, cacheService, testSequenceId, () -> {
                            cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex).forEach(i -> {
                                StepVariable execute = i.execute(cacheService, pram);
                            });
                            return StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                        });
                    } catch (Exception e) {
                        log.error("流控-for异常", e);
                    }
                    break;
                case F_SELECT:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                    break;
                case F_END:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                    String stepPath = "RunState.SequenceFile.Data.Seq." + testSequenceName + "." + scope + "." + name + "[" + id + "].endType";
                    String valueByPath = stepVariable.getValueByPath(stepPath);
                    step.addNestedAttribute("endType", valueByPath, "");
                    break;
                case F_CASE:
                    if (defaultCase != null && defaultCase) {
                        for (StepBase stepBase : cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex)) {
                            stepBase.execute(cacheService, pram);
                        }
                    } else {
                        FlowControlStep select = mongoTemplate.findById(selectId, FlowControlStep.class);
                        Assert.handle(select != null, "case对应select步骤不存在");
                        String selectCondition = select.getCondition();
                        Assert.handle(StringUtils.isNotBlank(selectCondition), "select步骤表达式为空");
                        if (ExpressionParserUtils.conditionalExecution(selectCondition + "==" + condition, stepVariable, cacheService, testSequenceId) == 1) {
                            for (StepBase stepBase : cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex)) {
                                stepBase.execute(cacheService, pram);
                            }
                        }
                    }
                    break;
                case F_GOTO:
                    step.addNestedAttribute("GotoId", gotoStepId, "gotoId");
                    break;
                case F_ELSE:
                    for (StepBase stepBase : cacheService.getStep(testSequenceId).subList(startIndex + 1, endIndex)) {
                        stepBase.execute(cacheService, pram);
                    }
                    break;
            }
        } else {
            // 根据控制流类型执行不同的逻辑
            switch (subType) {
                case F_IF:
                case F_ELSE_IF:
                case F_WHILE:
                case F_DO_WHILE:
                case F_BREAK:
                case F_CONTINUE:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                    step.addNestedAttribute("FlowStatus", ExpressionParserUtils.conditionalExecution(condition, stepVariable, cacheService, testSequenceId) == 1, "流控表达式结果");
                    break;
                case F_FOR:
                    if (redisUtil.hasKey(key)) {
                        condition = (String) redisUtil.get(key);
                    }
                    //表达式正则
                    Pattern forPattern = Pattern.compile("\\s*for\\(\\s*([\\w\\.]+)\\s*=\\s*(\\d+)\\s*;\\s*\\1\\s*([<>]=?)\\s*(\\d+)\\s*;\\s*\\1\\s*(\\+\\+|--|\\+=\\d+|\\-=\\d+)\\s*\\)\\s*");
                    Matcher matcher = forPattern.matcher(condition);
                    if (!matcher.matches()) {
                        throw new IllegalArgumentException("Invalid for loop expression.");
                    }
                    String variableName = matcher.group(1);
                    int initialValue = Integer.parseInt(matcher.group(2));
                    String comparison = matcher.group(3);
                    int boundaryValue = Integer.parseInt(matcher.group(4));
                    String increment = matcher.group(5).replaceAll("\\s+", "");
                    // 获取增加
                    int incrementValue = increment.startsWith("++") || increment.startsWith("--") ? 1 : Integer.parseInt(increment.substring(2));
                    boolean isPositiveIncrement = increment.startsWith("+") || increment.startsWith("++");
                    // 计算增量
                    // 循环是否跳出
                    boolean exitLoop = comparison.equals("<") ? initialValue >= boundaryValue : comparison.equals("<=") ? initialValue > boundaryValue : comparison.equals(">") ? initialValue <= boundaryValue : !comparison.equals(">=") || initialValue < boundaryValue;
                    if (exitLoop) {
                        step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                        step.addNestedAttribute("FlowStatus", false, "流控表达式结果");
                        redisUtil.del(key);
                        break;
                    }
                    initialValue += isPositiveIncrement ? incrementValue : -incrementValue;
                    stepVariable.addNestedAttribute(variableName, initialValue, "For循环索引");
                    // 生成下一次循环表达式
                    String newIncrementPart = increment.startsWith("++") || increment.startsWith("--") ? increment : increment.charAt(0) + "=" + incrementValue;
                    String newExpression = String.format("for(%s=%d;%s%s%d;%s%s)", variableName, initialValue, variableName, comparison, boundaryValue, variableName, newIncrementPart);
                    redisUtil.set(key, newExpression);
                    step = StepVariable.RESULT_SUCCESS(StepStatus.LOOPING);
                    step.addNestedAttribute("FlowStatus", true, "流控表达式结果");
                    break;
                case F_CASE:
                    // 执行switch-case逻辑
                    if (defaultCase != null && defaultCase) {
                        step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                        step.addNestedAttribute("FlowStatus", true, "流控表达式结果");
                    } else {
                        FlowControlStep select = mongoTemplate.findById(selectId, FlowControlStep.class);
                        Assert.handle(select != null, "case对应select步骤不存在");
                        String selectCondition = select.getCondition();
                        if (selectCondition.isBlank()) {
                            step = StepVariable.RESULT_SUCCESS(StepStatus.FAILED);
                            step.addNestedAttribute("FlowStatus", false, "流控表达式结果");
                            break;
                        }
                        String expression = selectCondition + "==" + condition;
                        step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                        step.addNestedAttribute("FlowStatus", ExpressionParserUtils.conditionalExecution(expression, stepVariable, cacheService, testSequenceId), "流控表达式结果");
                    }
                    break;
                case F_END:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                    String stepPath = "RunState.SequenceFile.Data.Seq." + testSequenceName + "." + scope + "." + name + "[" + id + "].endType";
                    String valueByPath = stepVariable.getValueByPath(stepPath);
                    step.addNestedAttribute("endType", valueByPath, "");
                    break;
                case F_ELSE:
                case F_GOTO:
                case F_SELECT:
                    step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported control flow type");
            }
        }
        String expression = "";
        if (StringUtils.isNotBlank(condition)) {
            expression = ExpressionParserUtils.getCondition(condition, stepVariable, cacheService, testSequenceId);
        }
        step.addNestedAttribute("Expression", expression, "表达式");
        step.addNestedAttribute("FlowControlType", subType.name(), "流控类型");
        return step;
    }

    public static void main(String[] args) {
        Pattern forPattern = Pattern.compile("\\s*for\\(\\s*([\\w\\.]+)\\s*=\\s*(\\d+)\\s*;\\s*\\1\\s*([<>]=?)\\s*(\\d+)\\s*;\\s*\\1\\s*(\\+\\+|--|\\+=\\d+|\\-=\\d+)\\s*\\)\\s*");
        //Matcher matcher = forPattern.matcher("for( Locals.num1=0; Locals.num1<10 ; Locals.num1+=1 )");
        Matcher matcher = forPattern.matcher("for(Locals.num1=1;Locals.num1<10;Locals.num1+=1)");
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid for loop expression.");
        } else {
            System.out.println("sccess");
        }
    }
}
