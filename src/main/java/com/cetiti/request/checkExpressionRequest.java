package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class checkExpressionRequest implements Serializable {

    @ApiModelProperty(value = "表达式", required = true)
    private String expression;

    @ApiModelProperty(value = "变量树", required = true)
    private StepVariableDTO stepVariableDTO;

    @ApiModelProperty(value = "返回类型", required = true)
    private String resultType;

    public checkExpressionRequest(String expression, StepVariableDTO stepVariableDTO) {
        this.expression = expression;
        this.stepVariableDTO = stepVariableDTO;
    }

    public checkExpressionRequest() {
    }

    public checkExpressionRequest(String expression, StepVariableDTO stepVariableDTO, String resultType) {
        this.expression = expression;
        this.stepVariableDTO = stepVariableDTO;
        this.resultType = resultType;
    }
}
