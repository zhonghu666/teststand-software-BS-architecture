package com.cetiti.constant;

public enum OPERATING_MODE {

    NORMAL(0, "正常"),
    SKIP(1, "跳过"),
    FORCED_PASSAGE(2, "强制通过"),
    FORCED_FAILURE(3, "强制失败");

    public Integer getCode() {
        return code;
    }

    private Integer code;

    public String getDesc() {
        return desc;
    }

    private String desc;

    private OPERATING_MODE(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
