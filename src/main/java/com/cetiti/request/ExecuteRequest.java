package com.cetiti.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ExecuteRequest implements Serializable {

    @ApiModelProperty("步骤Id")
    private String id;

    @ApiModelProperty("弹窗->key:应答文本配置的key值,value：弹窗选择或填写的内容;key:chooseButton,value：点击按钮的名称")
    private Map<String, Object> param;

    @ApiModelProperty("是否返回结果变量树")
    private Boolean stepFlag;



}
