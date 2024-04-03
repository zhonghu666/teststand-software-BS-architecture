package com.cetiti.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "TestSequence")
public class TestSequence implements Serializable {

    private String id;

    private String sequenceName;

    @ApiModelProperty("step")
    private List<String> stepList;

    @ApiModelProperty("数据调用ids")
    private String dataCallId;

    private StepVariable stepVariable;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String exceptVersion;
}
