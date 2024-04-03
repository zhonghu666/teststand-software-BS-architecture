package com.cetiti.response;

import com.cetiti.entity.StepVariable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class StepExecuteResponse implements Serializable {

    @ApiModelProperty("结果树")
    private StepVariable stepVariable;

    @ApiModelProperty("结果")
    private Object flowCondition;

    @ApiModelProperty("步骤状态")
    private String status;

    @ApiModelProperty("报告地址")
    private String reportUrl;

    public StepExecuteResponse() {
    }

    public StepExecuteResponse(StepVariable step) {
        String type = step.getValueByPath("type");
        if (type != null) {
            if (type.equals("N_WAIT")) {
                this.flowCondition = step.getValueByPath("TimeoutExpr");
            } else if (type.equals("N_FLOW_CONTROL")) {
                this.flowCondition = step.getValueByPath("FlowStatus");
            }
        }
        this.status = step.getValueByPath("Result.Status");
        this.stepVariable = step;
    }
}
