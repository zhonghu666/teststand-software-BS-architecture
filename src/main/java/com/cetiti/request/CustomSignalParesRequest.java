package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class CustomSignalParesRequest implements Serializable {

    @ApiModelProperty(value = "uuid",required = true)
    private String uuid;

    @ApiModelProperty(value = "表达式",required = true)
    private String expression;

    @ApiModelProperty(value = "自定义信号名称",required = true)
    private String name;

    @ApiModelProperty(value = "信号类型",required = true)
    private String type;

    @ApiModelProperty(value = "变量",required = true)
    private Map<String, Object> param;

}
