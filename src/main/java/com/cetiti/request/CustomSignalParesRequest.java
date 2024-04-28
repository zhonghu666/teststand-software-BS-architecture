package com.cetiti.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class CustomSignalParesRequest implements Serializable {

    private String uuid;

    private String expression;

    private String name;

    private String type;

    private Map<String, Object> param;

}
