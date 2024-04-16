package com.cetiti.entity.step;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DataCallInfo implements Serializable {
    @ApiModelProperty("协议名称")
    private String name;

    @ApiModelProperty("协议唯一标识")
    private List<String> esn;

    public DataCallInfo(String name, List<String> esnList) {
        this.name = name;
        this.esn = esnList;
    }
}
