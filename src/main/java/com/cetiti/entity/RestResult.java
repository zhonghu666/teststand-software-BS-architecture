package com.cetiti.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RestResult {

    @ApiModelProperty("是否成功")
    private boolean success;
    @ApiModelProperty("信息提示")
    private String msg;
    @ApiModelProperty("状态码")
    private int code;
    @ApiModelProperty("sjc")
    public long timeStamp;
    @ApiModelProperty("接口返回")
    private Object data;
}
