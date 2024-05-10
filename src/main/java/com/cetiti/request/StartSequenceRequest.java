package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StartSequenceRequest {

    @ApiModelProperty(value = "序列执行版本号",required = true)
    private String exceptVersion;

    @ApiModelProperty(value = "是否主序列",required = true)
    private Boolean isMain;

    @ApiModelProperty(value = "序列id",required = true)
    private String sequenceId;


}
