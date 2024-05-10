package com.cetiti.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Info implements Serializable {

    @ApiModelProperty(value = "数据调用原名")
    private String callFieldName;

    @ApiModelProperty("数据类型")
    private String dataType;

    @ApiModelProperty("新命名")
    private String variableName;
}
