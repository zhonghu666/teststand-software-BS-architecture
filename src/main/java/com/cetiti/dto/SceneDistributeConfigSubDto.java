package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SceneDistributeConfigSubDto implements Serializable {

    @ApiModelProperty(value = "rsu的esn")
    String esn;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "下发信息的主键编号")
    List<String> sceneConfigs;
}