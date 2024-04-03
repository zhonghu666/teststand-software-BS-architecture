package com.cetiti.entity;

import lombok.Data;

@Data
public class RestResult {

    private boolean success;
    private String msg;
    private int code;
    public long timeStamp;
    private Object data;
}
