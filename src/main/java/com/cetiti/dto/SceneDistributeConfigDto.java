package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SceneDistributeConfigDto implements Serializable {

    @ApiModelProperty(value = "场景编号")
    String sceneId;
    @ApiModelProperty(value = "mec的esn")
    String esn;
    @ApiModelProperty(value = "rsu的esn和下发选择信息")
    List<SceneDistributeConfigSubDto> rsuSceneConfigs;
}