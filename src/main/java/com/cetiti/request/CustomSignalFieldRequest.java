package com.cetiti.request;

import com.cetiti.constant.ValueType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CustomSignalFieldRequest implements Serializable {

    @ApiModelProperty(value = "原始路径",required = true)
    private String originalPath;

    @ApiModelProperty(value = "新路径",required = true)
    private String newPath;

    @ApiModelProperty(value = "字段类型",required = true)
    private ValueType type;

    @ApiModelProperty(value = "字段含义",required = true)
    private String fieldMsg;
}
