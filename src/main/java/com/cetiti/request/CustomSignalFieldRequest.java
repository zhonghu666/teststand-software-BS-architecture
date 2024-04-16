package com.cetiti.request;

import com.cetiti.constant.ValueType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CustomSignalFieldRequest implements Serializable {

    @ApiModelProperty("原始路径")
    private String originalPath;

    @ApiModelProperty("新路径")
    private String newPath;

    @ApiModelProperty("字段类型")
    private ValueType type;

    @ApiModelProperty("字段含义")
    private String fieldMsg;
}
