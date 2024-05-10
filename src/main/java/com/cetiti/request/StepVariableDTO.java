package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class StepVariableDTO implements Serializable {

    @ApiModelProperty(value = "子集",required = true)
    private Map<String, ValueWrapperDTO> attributes;


}
