package com.cetiti.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class FunctionMetadataDetailsResponse implements Serializable {

    @ApiModelProperty("函数名称")
    private String functionName;

    @ApiModelProperty("模版")
    private String template;

    @ApiModelProperty("描述")
    private String desc;

    @ApiModelProperty("参数描述")
    private String paramDesc;
}
