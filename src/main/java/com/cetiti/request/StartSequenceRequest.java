package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StartSequenceRequest {

    @ApiModelProperty("序列执行版本号")
    private String exceptVersion;

    @ApiModelProperty("是否主序列")
    private Boolean isMain;

    private String sequenceId;


}
