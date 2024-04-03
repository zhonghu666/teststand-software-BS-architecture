package com.cetiti.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class StepVariableDTO implements Serializable {

    private Map<String, ValueWrapperDTO> attributes;


}
