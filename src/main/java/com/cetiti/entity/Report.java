package com.cetiti.entity;

import com.cetiti.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Document(collection = "Report")
public class Report implements Serializable {

    @Indexed(unique = true)
    private String id;
    @ApiModelProperty("状态 0未生成 1已生成 ")
    private Integer status;
    @ApiModelProperty("报告名称")
    private String name;
    @ApiModelProperty("序列id")
    private String testSequenceId;
    @ApiModelProperty("序列名称")
    private String testSequenceName;
    @ApiModelProperty("word文件路径")
    private String wordFilePath;
    @ApiModelProperty("pdf文件路径")
    private String pdfFilePath;
    @JsonFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;
    @JsonFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updateTime;
}
