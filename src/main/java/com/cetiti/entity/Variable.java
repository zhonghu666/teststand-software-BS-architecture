package com.cetiti.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "Variable")
public class Variable extends TreeModule implements Serializable {

    private String id;

    private String variableName;

    private Object VariableValue;

    private String VariableType;

    private String remark;

}
