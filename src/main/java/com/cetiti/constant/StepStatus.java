package com.cetiti.constant;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum StepStatus {

    PASSED("passed", "合格"),
    FAILED("failed", "失败"),
    DONE("done", "完成"),
    ERROR("Error", "错误"),
    SKIPPED("skipped", "跳过"),
    TERMINATED("terminated", "终止"),
    LOOPING("looping", "循环"),
    RUNNING("Running", "运行"),
    Waiting("Waiting", "等待"),
    ;

    private String code;
    private String desc;

    private StepStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (StepStatus stepStatus : StepStatus.values()) {
            if (Objects.equals(stepStatus.getCode(), code)) {
                return stepStatus.getDesc();
            }
        }
        return null;
    }
}
