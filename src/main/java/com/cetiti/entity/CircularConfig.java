package com.cetiti.entity;

import com.cetiti.constant.SpinType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CircularConfig implements Serializable {

    @ApiModelProperty("循环类型")
    private SpinType spinType;

    @ApiModelProperty("初始化表达式")
    private String initializationExpression;

    @ApiModelProperty("增量表达式")
    private String incrementExpression;

    @ApiModelProperty("while表达式")
    private String whileExpression;

    @ApiModelProperty("结果表达式")
    private String resultExpression;

    @ApiModelProperty("是否记录迭代结果")
    private Boolean recordRes;

}
