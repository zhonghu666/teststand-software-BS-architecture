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

    private String id;

    @ApiModelProperty("序列名称")
    private String sequenceName;

    @ApiModelProperty("step")
    @NotEmpty(message = "步骤不可为空")
    @Valid
    private List<StepBase> stepList;

    @ApiModelProperty("数据调用")
    private StepBase dataCallStep;

    @ApiModelProperty("变量")
    private StepVariableDTO stepVariable;
}
