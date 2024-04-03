package com.cetiti.response;

import com.cetiti.entity.StepVariable;
import com.cetiti.entity.step.DataCallStep;
import com.cetiti.entity.step.StepBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TestSequenceResponse implements Serializable {

    private String id;

    private String sequenceName;

    @ApiModelProperty("step")
    private List<StepBase> stepList;

    @ApiModelProperty("数据调用")
    private DataCallStep dataCallStep;

    private StepVariable stepVariable;

    @ApiModelProperty("场地id")
    private String siteId;

    @ApiModelProperty("序列是否执行")
    private Boolean executeStatus;

}
