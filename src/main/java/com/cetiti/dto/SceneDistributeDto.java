package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SceneDistributeDto implements Serializable {

    @ApiModelProperty(value = "场景编号")
    private String sceneId;
    @ApiModelProperty(value = "rsu的esn")
    List<String> rsuList;
}
