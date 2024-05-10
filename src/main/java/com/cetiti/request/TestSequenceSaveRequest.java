package com.cetiti.request;

import com.cetiti.entity.step.DataCallStep;
import com.cetiti.entity.step.StepBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
public class TestSequenceSaveRequest implements Serializable {

    @ApiModelProperty(value = "id", required = true)
    private String id;

    @ApiModelProperty(value = "序列名称", required = true)
    private String sequenceName;

    @ApiModelProperty(value = "step", required = true)
    @NotEmpty(message = "步骤不可为空")
    @Valid
    private List<StepBase> stepList;

    @ApiModelProperty("数据调用")
    private StepBase dataCallStep;

    @ApiModelProperty(value = "变量", required = true)
    private StepVariableDTO stepVariable;
}
