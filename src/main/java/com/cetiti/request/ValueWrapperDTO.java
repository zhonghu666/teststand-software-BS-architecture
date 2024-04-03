package com.cetiti.request;

import com.cetiti.constant.ValueType;
import com.cetiti.entity.Info;
import lombok.Data;

import java.util.Map;

@Data
public class ValueWrapperDTO {
    private ValueType type;
    private Object value;
    private Info info;
    private String desc;
    private Map<String, ValueWrapperDTO> children;

}
