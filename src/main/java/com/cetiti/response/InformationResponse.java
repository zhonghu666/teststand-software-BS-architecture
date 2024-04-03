package com.cetiti.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class InformationResponse implements Serializable {
    private String key;

    private Object informationValue;

}
