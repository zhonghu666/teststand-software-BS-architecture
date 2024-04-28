package com.cetiti.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class checkExpressionRequest implements Serializable {

    private String expression;

    private StepVariableDTO stepVariableDTO;

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
