package com.cetiti.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("预制返回结果")
@Data
public class PreSendResultDto implements Serializable {

    @ApiModelProperty(value = "uuid")
    String uuid;

    @ApiModelProperty(value = "时长")
    Long time;
}