package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CustomSignalRequest implements Serializable {

    @ApiModelProperty(value = "esn", required = true)
    private String esn;

    @ApiModelProperty(value = "开关状态", required = true)
    private Boolean start;

    @ApiModelProperty(value = "自定义信号解析数据源", required = true)
    private List<CustomSignalFieldRequest> customSignalFieldRequests;

    @ApiModelProperty(value = "频率", required = true)
    private String interval;
}
