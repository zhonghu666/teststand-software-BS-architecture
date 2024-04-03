package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TestSequenceEditRequest {

    private String id;

    @ApiModelProperty("序列名称")
    private String sequenceName;
}
