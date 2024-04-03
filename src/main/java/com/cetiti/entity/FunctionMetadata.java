package com.cetiti.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@Document("FunctionMetadata")
public class FunctionMetadata implements Serializable {

    @ApiModelProperty("大类")
    private String type;

    @ApiModelProperty("函数类型")
    private String functionType;

    @ApiModelProperty("函数名称")
    private String functionName;

    @ApiModelProperty("参数数量")
    private int paramCount;

    @ApiModelProperty("模版")
    private String template;

    @ApiModelProperty("描述")
    private String desc;

    @ApiModelProperty("参数描述")
    private String paramDesc;

}
