package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class v2xMsgDTO implements Serializable {
    @ApiModelProperty(value = "消息集类型：1-map 2-spat 3-rsm 4-rsi 5-bsm")
    private int type;

    @ApiModelProperty(value = "广播间隔，单位毫秒(HZ)")
    private int interval;

    @ApiModelProperty(value = "是否广播平台下发V2X")
    private Boolean platV2xEnable;

    @ApiModelProperty(value = "是否广播本地配置V2X")
    private Boolean localV2xEnable;
}
