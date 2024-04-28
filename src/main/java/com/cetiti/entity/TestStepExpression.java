package com.cetiti.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestStepExpression implements Serializable {
    @ApiModelProperty("参数表达式")
    private String paramExpression;

    @ApiModelProperty("符号")
    private String met;

    @ApiModelProperty("条件1")
    private String low;

    @ApiModelProperty("条件2")
    private String high;

    @ApiModelProperty("单位")
    private String unit;

    public String getExpression() {
        if (StringUtils.isBlank(high)) {
            return paramExpression + met + low;
        } else {
            return paramExpression + ">" + low + "&&" + paramExpression + "<" + high;
        }
    }
}
