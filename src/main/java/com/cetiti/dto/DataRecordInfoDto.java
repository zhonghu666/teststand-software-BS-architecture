package com.cetiti.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ZYL
 * @date 2023/8/11 11:34
 * @from ENJOYOR
 * five star
 */
@Data
public class DataRecordInfoDto implements Serializable {
    @ApiModelProperty(value = "名称")
    private String siteName;

    @ApiModelProperty(value = "场地id")
    private String siteId;


    @ApiModelProperty(value = "记录仪设备esn")
    private String esn;
}