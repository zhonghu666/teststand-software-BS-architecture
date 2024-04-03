package com.cetiti.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.List;

@ApiModel("rsu广播配置")
@Data
public class RsuBroadcastDto implements Serializable {

    @ApiModelProperty(value = "记录ID")
    @Indexed
    private String id;

    @ApiModelProperty(value = "esn")
    private String esn;
    @ApiModelProperty(value = "是否有改动")
    private boolean change;

    @ApiModelProperty(value = "配置")
    private List<v2xMsgDTO> v2xMsgList;
}
