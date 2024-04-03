package com.cetiti.entity;

import com.cetiti.constant.ValueType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DataCallField implements Serializable {

    @ApiModelProperty("原始路径")
    private String originalPath;

    @ApiModelProperty("新路径")
    private String newPath;

    @ApiModelProperty("字段类型")
    private ValueType type;

    public DataCallField(String originalPath, String newPath, ValueType type) {
        this.originalPath = originalPath;
        this.newPath = newPath;
        this.type = type;
    }

    public DataCallField() {
    }
}
