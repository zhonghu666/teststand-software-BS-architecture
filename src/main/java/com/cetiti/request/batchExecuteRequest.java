package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class batchExecuteRequest implements Serializable {

    @ApiModelProperty(value = "序列调用步骤id",required = true)
    private String stepId;

    @ApiModelProperty(value = "子序列批量执行步骤Id",required = true)
    private List<String> stepIdList;
}
