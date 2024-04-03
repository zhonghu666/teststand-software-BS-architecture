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

    //    @JsonFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
//    @DateTimeFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS)
//    @JsonSerialize(using = DateSerializer.class)
//    @ApiModelProperty(value = "开始记录时间")
//    Date startRecordTime;

//    @JsonSerialize(using = DateSerializer.class)
//    @JsonFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
//    @DateTimeFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS)
//    @ApiModelProperty(value = "开始记录时间")
//    Date endRecordTime;

    @ApiModelProperty(value = "记录仪设备esn")
    private String esn;

//    @ApiModelProperty(value = "记录ID")
//    private String id;

//    @JsonSerialize(using = DateSerializer.class)
//    @JsonFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
//    @DateTimeFormat(pattern = DateUtils.YYYY_MM_DD_HH_MM_SS)
//    @ApiModelProperty(value = "创建时间戳")
//    private Date createTimestamp;

}