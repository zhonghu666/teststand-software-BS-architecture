package com.cetiti.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ZYL
 * @date 2023/9/8 10:24
 * @from ENJOYOR
 * five star
 */
@Data
public class ObuSceneConfigDto implements Serializable {

    String esn;

    private Boolean isEnable;

    @ApiModelProperty(value = "场景类型1-fcw,2-ebw,3-icw,5-lcw,6-clw,7-avw,8-evw,9-dnpw,10-lta,12-vrucw,14-vrusp,15-vru")
    private int appType;

    private String appTypeName;

    @ApiModelProperty(value = "两方向平行角度阈值，单位度，取值范围[0,45]")
    private int angle_parallel_threshold;

    @ApiModelProperty(value = "两方向垂直角度阈值，单位度，取值范围[0,45]")
    private int angle_vertical_threshold;

    @ApiModelProperty(value = "两方向同向角度阈值，单位度，取值范围[0,45]")
    private int angle_sameWay_threshold;

    @ApiModelProperty(value = "两方向逆向角度阈值，单位度，取值范围[0,45]")
    private int angle_Opposite_threshold;

    @ApiModelProperty(value = "最大预警距离, 单位m，取值范围[50,500]")
    private int dist_maxRange;

    @ApiModelProperty(value = "最小预警距离, 单位m，取值范围[0,20]")
    private int dist_minRange;

    @ApiModelProperty(value = "最大预警碰撞时间, 单位:s，取值范围[5,15]")
    private int ttc_maxRange;

    @ApiModelProperty(value = "最小预警碰撞时间, 单位:s，取值范围[0,2]")
    private int ttc_minRange;

    @ApiModelProperty(value = "最小本车速度, 单位:km/h， 取值范围[0,10]")
    private int hvSpeed_minRange;

    @ApiModelProperty(value = "最小远车速度, 单位:km/h， 取值范围[0,10]")
    private int rvSpeed_minRange;

    @ApiModelProperty(value = "碰撞预警时间差, 单位:s，取值范围[0,5]")
    private int deltattc_maxRange;

    @ApiModelProperty(value = "弱势交通参与者横向距离阈值, 单位:s，取值范围[1,4]")
    private int vruhonDist_Range;

    @ApiModelProperty(value = "快速驶向盲区最大预警碰撞时间, 单位:s，取值范围[5,15]")
    private int blindtime_maxRange;

    @ApiModelProperty(value = "快速驶向盲区最小预警碰撞时间, 单位:s，取值范围[0,2]")
    private int blindtime_minRange;

}