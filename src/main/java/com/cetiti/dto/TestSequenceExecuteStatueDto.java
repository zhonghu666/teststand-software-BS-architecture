package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TestSequenceExecuteStatueDto implements Serializable {

    @ApiModelProperty("唯一标识")
    private String id;

    @ApiModelProperty("状态")
    private Boolean runStatus;

    @ApiModelProperty("版本号")
    private String exceptVersion;
}
