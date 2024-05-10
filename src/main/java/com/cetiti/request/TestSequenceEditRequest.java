package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestSequenceEditRequest {

    @ApiModelProperty(value = "id",required = true)
    private String id;

    @ApiModelProperty(value = "序列名称",required = true)
    private String sequenceName;
}
