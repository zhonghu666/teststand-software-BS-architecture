package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author ZYL
 * @date 2023/9/8 10:24
 * @from ENJOYOR
 * five star
 */
@Data
@Document(collection = "RsuSceneConfig")
public class RsuSceneConfigDto implements Serializable {
    String uuid;

    String esn;

    @ApiModelProperty(value = "是否有改动")
    private boolean change;

    @ApiModelProperty(value = "感知数据共享")
    private boolean sdsEnable;

    @ApiModelProperty(value = "协作式变道")
    private boolean clcEnable;

    @ApiModelProperty(value = "协作式匝道汇入")
    private boolean cvmRampInEnable;

    @ApiModelProperty(value = "协作式交叉口提前引导车辆换道")
    private boolean cipLanechangingAtIntersectionEnable;

    @ApiModelProperty(value = "协作式无信控交叉口通行")
    private boolean cipNoSignalPassingEnable;

    @ApiModelProperty(value = "差分数据服务")
    private boolean ddsEnable;

    @ApiModelProperty(value = "动态车道管理")
    private boolean dlmEnable;

    @ApiModelProperty(value = "协作式优先车辆通行--车道预留")
    private boolean chpvpLaneReservationEnable;

    @ApiModelProperty(value = "协作式优先车辆通行--车道封闭/禁行")
    private boolean chpvpLaneRestrictionEnable;

    @ApiModelProperty(value = "协作式优先车辆通行--信号优先")
    private boolean chpvpSignalPriorityEnable;

    @ApiModelProperty(value = "场站路径引导")
    private boolean gspaEnable;

    @ApiModelProperty(value = "道路收费服务")
    private boolean rtsEnable;

}
