package com.cetiti.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomSignalParesRequest implements Serializable {

    private String uuid;

    private String expression;

    private String name;

    private String type;

}
