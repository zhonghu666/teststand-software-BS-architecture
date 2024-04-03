package com.cetiti.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

@ApiModel("rsu-CFG广播配置")
@Data
public class RsuCfgDto implements Serializable {

    @ApiModelProperty(value = "记录ID")
    @Indexed
    private String id;

    @ApiModelProperty(value = "esn")
    private String esn;

    @ApiModelProperty(value = "是否有改动")
    private boolean change;

    /*******************************RSU CGF下发******************************************************/
    @ApiModelProperty(value = "BSM数据上报")
    private boolean sendBsm;

    @ApiModelProperty(value = "MAP数据上报")
    private boolean sendMap;

    @ApiModelProperty(value = "SPAT数据上报")
    private boolean sendSpat;

    @ApiModelProperty(value = "RSI数据上报")
    private boolean sendRsi;

    @ApiModelProperty(value = "RSM数据上报")
    private boolean sendRsm;

    @ApiModelProperty(value = "心跳数据上报")
    private boolean sendHeartbeat;

    @ApiModelProperty(value = "状态数据上报")
    private boolean sendStatus;
    /*******************************RSU CGF下发******************************************************/

}
