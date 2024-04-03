package com.cetiti.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Info implements Serializable {

    private String callFieldName;

    private String dataType;

    private String variableName;
}
