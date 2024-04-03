package com.cetiti.constant;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("返回Json数据格式")
@Data
public class BaseJson<T> implements Serializable {

    @ApiModelProperty("请求结果：true 成功 false 失败")
    private boolean success;
    @ApiModelProperty("请求结果描述，成功为空 失败会有描述内容")
    private String msg;
    @ApiModelProperty("状态")
    private int code = BASE_JSON_CODE.COMMON_FAIL.getCode();
    @ApiModelProperty("返回结果的时间戳")
    public long timeStamp = System.currentTimeMillis();
    @ApiModelProperty("数据内容")
    private T data;

    public BaseJson<T> Success(T data) {
        return this.Success("操作成功", data);
    }

    public BaseJson<T> Success(String msg, T data) {
        this.success = true;
        this.msg = msg;
        this.code = BASE_JSON_CODE.SUCCESS.getCode();
        if (data != null) {
            this.data = data;
        }
        return this;
    }

    public BaseJson<T> Fail(String msg, Integer code) {
        this.success = true;
        this.msg = msg;
        this.code = code;
        return this;
    }

    public BaseJson<T> Fail(String msg) {
        this.success = true;
        this.msg = msg;
        this.code = BASE_JSON_CODE.COMMON_FAIL.getCode();
        return this;
    }
    public BaseJson<T> Fail(String msg, T data) {
        this.success = false;
        this.msg = msg;
        this.code = BASE_JSON_CODE.COMMON_FAIL.getCode();
        if (data != null) {
            this.data = data;
        }
        return this;
    }
    public void setData(T data) {
        this.data = data;
        //如果插入数据，默认就成功
        this.code = BASE_JSON_CODE.SUCCESS.getCode();
        this.success = true;
    }


}
