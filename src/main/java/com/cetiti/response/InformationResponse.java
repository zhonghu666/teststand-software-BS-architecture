package com.cetiti.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class InformationResponse implements Serializable {

    @ApiModelProperty("名称")
    private String key;

    @ApiModelProperty("信息详情")
    private Object informationValue;

}
