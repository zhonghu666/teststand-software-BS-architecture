package com.cetiti.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FunctionMetadataResponse implements Serializable {

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("函数子集")
    private List<FunctionMetadataDetailsResponse> functionMetadataDetailsResponses;

}
