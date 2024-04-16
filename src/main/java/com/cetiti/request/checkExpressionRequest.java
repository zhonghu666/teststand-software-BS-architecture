package com.cetiti.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class checkExpressionRequest implements Serializable {

    private String expression;
}
