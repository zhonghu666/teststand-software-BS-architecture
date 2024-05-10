package com.cetiti.request;

import com.cetiti.constant.ValueType;
import com.cetiti.entity.Info;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ValueWrapperDTO {

    @ApiModelProperty("类型")
    private ValueType type;
    @ApiModelProperty("值")
    private Object value;
    @ApiModelProperty("数据调用-无意义配合使用")
    private Info info;
    @ApiModelProperty("备注")
    private String desc;
    @ApiModelProperty("子集")
    private Map<String, ValueWrapperDTO> children;

}
