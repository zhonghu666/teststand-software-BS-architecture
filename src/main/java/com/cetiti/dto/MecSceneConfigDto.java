package com.cetiti.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ZYL
 * @date 2023/9/8 10:24
 * @from ENJOYOR
 * five star
 */
@Data
public class MecSceneConfigDto implements Serializable {
    String mecId;

    String esn;

    MecSceneConfigSubDto trafficEventCfg;


}
