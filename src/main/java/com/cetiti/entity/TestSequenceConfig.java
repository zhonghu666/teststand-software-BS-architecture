package com.cetiti.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "TestSequenceConfig")
public class TestSequenceConfig implements Serializable {

    @ApiModelProperty("是否启用跟踪")
    private Boolean trackEnable;

    @ApiModelProperty("运行速度,单位ms")
    private Integer speed;

    @ApiModelProperty("允许跟踪至Setup/Cleanup")
    private Boolean AllowTracingSetupOrCleanup;

    @ApiModelProperty("是否输出报告")
    private Boolean reportEnable;

    @ApiModelProperty("修改时间")
    private Date updateDate;

    public TestSequenceConfig(Boolean trackEnable, Integer speed, Boolean allowTracingSetupOrCleanup, Boolean reportEnable) {
        this.trackEnable = trackEnable;
        this.speed = speed;
        AllowTracingSetupOrCleanup = allowTracingSetupOrCleanup;
        this.reportEnable = reportEnable;
    }

    public TestSequenceConfig() {
    }
}
