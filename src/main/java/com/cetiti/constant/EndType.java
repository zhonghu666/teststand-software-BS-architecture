package com.cetiti.constant;

import java.util.Objects;

public enum EndType {

    END_FOR("END_FOR", "End (For)"),
    END_IF("END_IF", "End (If)"),
    END_WHILE("END_WHILE", "End (While)"),
    END_DO_WHILE("END_DO_WHILE", "End (Do While)"),
    END_SELECT("END_SELECT", "End (Select)"),
    END_CASE("END_CASE", "End (Case)");

    private String code;
    private String desc;

    EndType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByKey(String code) {
        for (EndType stepStatus : EndType.values()) {
            if (Objects.equals(stepStatus.getCode(), code)) {
                return stepStatus.getDesc();
            }
        }
        return "end";
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
