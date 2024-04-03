package com.cetiti.constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("分页对象")
@Data
public class Page<T> implements Serializable {

    @ApiModelProperty("数据集合")
    private List<T> list;
    @ApiModelProperty("当前页")
    private int currentPage;
    @ApiModelProperty("每页条数")
    private int pageSize;
    @ApiModelProperty("总页数")
    private long totalPage;
    @ApiModelProperty("总条数")
    private long totalNum;

}

