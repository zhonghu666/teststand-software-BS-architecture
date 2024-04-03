package com.cetiti.entity.step;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DataCallStencil implements Serializable {

    @ApiModelProperty("序列Id")
    private String id;

    @ApiModelProperty("开启关闭")
    private Boolean testStart;

    @ApiModelProperty("协议详情")
    private List<Info> info;

    @ApiModelProperty("主协议")
    private String mainProto;

}
