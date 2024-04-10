package com.cetiti.entity.step;

import com.alibaba.fastjson.JSON;
import com.cetiti.constant.Assert;
import com.cetiti.constant.StepStatus;
import com.cetiti.constant.TestStepType;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.TestStepExpression;
import com.cetiti.expression.ExpressionParserUtils;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class TestStep extends StepBase {

    @ApiModelProperty("测试步骤类型")
    private TestStepType subType;

    @ApiModelProperty("表达式内容")
    private List<TestStepExpression> testStepExpressions;

    @ApiModelProperty("关联符号")
    private String associationMet;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        StepVariable step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        StepVariable stepVariable = cacheService.getStepVariable(getTestSequenceId());
        switch (subType) {
            case T_STRING_VALUE:
                TestStepExpression StringExpression = testStepExpressions.get(0);
                String stringParam = stepVariable.getValueByPath(StringExpression.getParamExpression());
                if (ExpressionParserUtils.stringExpression(stringParam + StringExpression.getMet() + StringExpression.getLow())) {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.PASSED);
                } else {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.FAILED);
                }
                step.addNestedAttribute("Limits.StringExpr", StringExpression.getLow(), "字符串");
                step.addNestedAttribute("String", stringParam, "");
                step.addNestedAttribute("Met", StringExpression.getMet(), "比较方式");
                break;
            case T_MULTIPLE_NUMERIC_LIMIT:
                String collect;
                if (StringUtils.isNotBlank(associationMet)) {
                    collect = testStepExpressions.stream().map(TestStepExpression::getExpression).collect(Collectors.joining(associationMet));
                } else {
                    collect = testStepExpressions.get(0).getExpression();
                }
                int res = ExpressionParserUtils.conditionalExecution(collect, stepVariable, cacheService, testSequenceId);
                if (res != 2) {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.PASSED);
                } else {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.FAILED);
                }
                for (TestStepExpression i : testStepExpressions) {
                    StepVariable Measurement = new StepVariable();
                    Map<String, Object> response = ExpressionParserUtils.expressionParsingExecution(i.getParamExpression(), stepVariable, cacheService, testSequenceId);
                    Measurement.addNestedAttributeObject("Result.Numeric", response.get("result"), "测试参数结果");
                    Measurement.addNestedAttribute("Limits.LowExpr", i.getLow(), "下限");
                    Measurement.addNestedAttribute("RawLimits.Low", i.getLow(), "下限内容");
                    if (StringUtils.isNotBlank(i.getHigh())) {
                        Measurement.addNestedAttribute("Limits.HighExpr", i.getHigh(), "上限");
                        Measurement.addNestedAttribute("RawLimits.High", i.getHigh(), "上限内容");
                    }
                    Measurement.addNestedAttribute("Status", res != 2, "结果");
                    Measurement.addNestedAttribute("Units", i.getUnit(), "单位");
                    Measurement.addNestedAttribute("Met", i.getMet(), "比较方式");
                    step.addToListAtPath("Measurement", Measurement);
                }
                break;
            case T_NUMERIC_LIMIT:
                if (ExpressionParserUtils.conditionalExecution(testStepExpressions.get(0).getExpression(), stepVariable, cacheService, testSequenceId) != 2) {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.PASSED);
                } else {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.FAILED);
                }
                Map<String, Object> response = ExpressionParserUtils.expressionParsingExecution(testStepExpressions.get(0).getParamExpression(), stepVariable, cacheService, testSequenceId);
                step.addNestedAttributeObject("Result.Numeric", response.get("result"), "测试参数结果");
                if (StringUtils.isNotBlank(testStepExpressions.get(0).getHigh())) {
                    step.addNestedAttribute("Limits.HighExpr", testStepExpressions.get(0).getHigh(), "上限");
                    step.addNestedAttribute("Limits.LowExpr", testStepExpressions.get(0).getLow(), "下限");
                } else {
                    step.addNestedAttribute("Limits.LowExpr", testStepExpressions.get(0).getLow(), "下限");
                }
                step.addNestedAttribute("Result.Units", testStepExpressions.get(0).getUnit(), "单位");
                step.addNestedAttribute("Result.Met", testStepExpressions.get(0).getMet(), "比较方式");
                break;
            case T_PASS_FAIL:
                TestStepExpression testStepExpression1 = testStepExpressions.get(0);
                Object valueByPath = stepVariable.getValueByPath(testStepExpression1.getParamExpression());
                Assert.handle(valueByPath instanceof Boolean, "参数格式不对");
                if ((Boolean) valueByPath) {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.PASSED);
                } else {
                    step = StepVariable.RESULT_SUCCESS(StepStatus.FAILED);
                }
                step.addNestedAttribute("Result.PassFail", (Boolean) valueByPath, "是否通过");
                break;
            default:
                break;
        }
        step.addNestedAttribute("TestStepType", subType.name(), "测试步骤类型");
        return step;
    }

    public static void main(String[] args) {
        StepVariable stepVariable = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        //stepVariable.addNestedAttribute("Measurement",new ArrayList<StepVariable>(),"");
        for (int i = 0; i < 10; i++) {
            StepVariable s = new StepVariable();
            s.addNestedAttribute("Limits.High", 10, "");
            s.addNestedAttribute("Limits.Low", 1, "");
            s.addNestedAttribute("Limits.Nominal", 100, "");
            stepVariable.addToListAtPath("Measurement", s);
        }

        System.out.println(JSON.toJSONString(stepVariable));
    }
}
