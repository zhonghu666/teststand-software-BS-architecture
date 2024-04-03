package com.cetiti.constant;

import lombok.Getter;

@Getter
public enum BASE_JSON_CODE {

    SUCCESS(0, "成功"),
    COMMON_FAIL(1, "普通失败"),
    TOKEN_EMPTY(2, "token为空"),
    TOKEN_VALIDATED(3, "token校验失败"),
    TOKEN_EXPIRE(4, "token过期"),
    APP_EXCEPTION(5, "程序异常");

    private int code;
    private String desc;

    private BASE_JSON_CODE(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
