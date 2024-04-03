package com.cetiti.constant;

import lombok.Data;

import java.io.Serializable;

@Data
public class ValidationError implements Serializable {
    private String field;
    private String message;

    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
