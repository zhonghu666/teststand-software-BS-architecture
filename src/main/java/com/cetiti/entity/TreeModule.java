package com.cetiti.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "TreeModule")
public class TreeModule implements Serializable {

    private Integer code;

    private Integer parentCode;
}
