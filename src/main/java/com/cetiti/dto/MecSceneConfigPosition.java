package com.cetiti.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ZYL
 * @date 2023/9/8 16:52
 * @from ENJOYOR
 * five star
 */
@Data
public class MecSceneConfigPosition implements Serializable {

    @JSONField(name = "lon")
    Double lon;
    @JSONField(name = "lat")
    Double lat;

}