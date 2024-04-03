package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class batchExecuteRequest implements Serializable {

    @ApiModelProperty("序列调用步骤id")
    private String stepId;

    @ApiModelProperty("子序列批量执行步骤Id")
    private List<String> stepIdList;
}
