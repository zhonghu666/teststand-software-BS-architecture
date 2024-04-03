package com.cetiti.constant;

import lombok.Getter;


@Getter
public enum ErrorCode {

    SUCCESS(0, "成功", false),
    COMMON_FAIL(1, "普通失败", true);

    private int code;
    private String desc;
    private Boolean occurred;

    private ErrorCode(int code, String desc, Boolean occurred) {
        this.code = code;
        this.desc = desc;
        this.occurred = occurred;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public void setOccurred(Boolean occurred) {
        this.occurred = occurred;
    }
}
