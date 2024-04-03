package com.cetiti.entity.step;

import com.cetiti.constant.StepStatus;
import com.cetiti.entity.StepVariable;
import com.cetiti.expression.ExpressionParserUtils;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class StatementStep extends StepBase {

    @ApiModelProperty("表达式")
    @NotNull(message = "表达式不能为空")
    private String expression;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        StepVariable stepVariable = cacheService.getStepVariable(getTestSequenceId());
        ExpressionParserUtils.currencyExecution(expression, stepVariable,cacheService,testSequenceId);
        StepVariable step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        step.addNestedAttribute("Expression", expression, "表达式");
        return step;
    }
}
