package com.cetiti.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
public class StepAdditional implements Serializable {


    @ApiModelProperty("引用表达式")
    private String sourceExpression;

    @ApiModelProperty("目标表达式")
    private String targetExpression;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("是否绘制图表")
    private Boolean isGraphs;

    @ApiModelProperty("id")
    private String id;

}
