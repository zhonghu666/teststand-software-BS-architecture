package com.cetiti.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * @author ZYL
 * @date 2023/9/8 16:52
 * @from ENJOYOR
 * five star
 */
@Data
public class MecSceneConfigDetailDto implements Serializable {

    @JsonProperty(value= "add_area_name1")
    @JSONField(name = "add_area_name1")
    @Field("add_area_name1")
    String addAreaName1;

    @JsonProperty(value = "add_area_name2")
    @JSONField(name = "add_area_name2")
    @Field("add_area_name2")
    String addAreaName2;

    @JsonProperty(value = "add_area_name3")
    @JSONField(name = "add_area_name3")
    @Field("add_area_name3")
    String addAreaName3;

    @JsonProperty(value = "area_list")
    @JSONField(name = "area_list")
    @Field("area_list")
    List<MecSceneConfigAreaDto> areaList;

    @JsonProperty(value = "area_name")
    @JSONField(name = "area_name")
    @Field("area_name")
    String area_name;

    @JsonProperty(value= "current_mode")
    @JSONField(name = "current_mode")
    @Field("current_mode")
    String current_mode;

    @JsonProperty(value = "enable")
    @JSONField(name = "enable")
    @Field("enable")
    String enable;

    @JsonProperty(value= "event_Remark")
    @JSONField(name = "event_Remark")
    @Field("event_Remark")
    String eventRemark;

    @JsonProperty(value = "event_id")
    @JSONField(name = "event_id")
    @Field("event_id")
    String eventId;

    @JsonProperty(value = "event_name")
    @JSONField(name = "event_name")
    @Field("event_name")
    String eventName;

    @JsonProperty(value= "event_priority")
    @JSONField(name = "event_priority")
    @Field("event_priority")
    String eventPriority;

    @JsonProperty(value = "param1")
    @JSONField(name = "param1")
    @Field("param1")
    String param1;

    @JsonProperty(value= "param1_desc")
    @JSONField(name = "param1_desc")
    @Field("param1_desc")
    String param1Desc;

    @JsonProperty(value= "param1_prompt")
    @JSONField(name = "param1_prompt")
    @Field("param1_prompt")
    String param1Prompt;

    @JsonProperty(value = "param2")
    @JSONField(name = "param2")
    @Field("param2")
    String param2;

    @JsonProperty(value = "param2_desc")
    @JSONField(name = "param2_desc")
    @Field("param2_desc")
    String param2Desc;

    @JsonProperty(value = "param2_prompt")
    @JSONField(name = "param2_prompt")
    @Field("param2_prompt")
    String param2Prompt;

    @JsonProperty(value = "param3")
    @JSONField(name = "param3")
    @Field("param3")
    String param3;

    @JsonProperty(value = "param3_desc")
    @JSONField(name = "param3_desc")
    @Field("param3_desc")
    String param3Desc;

    @JsonProperty(value= "param3_prompt")
    @JSONField(name = "param3_prompt")
    @Field("param3_prompt")
    String param3Prompt;

    @JsonProperty(value= "param4")
    @JSONField(name = "param4")
    @Field("param4")
    String param4;

    @JsonProperty(value = "param4_desc")
    @JSONField(name = "param4_desc")
    @Field("param4_desc")
    String param4Desc;

    @JsonProperty(value = "param4_prompt")
    @JSONField(name = "param4_prompt")
    @Field("param4_prompt")
    String param4Prompt;

    @JsonProperty(value= "param5")
    @JSONField(name = "param5")
    @Field("param5")
    String param5;

    @JsonProperty(value = "param5_desc")
    @JSONField(name = "param5_desc")
    @Field("param5_desc")
    String param5Desc;

    @JsonProperty(value= "param5_prompt")
    @JSONField(name = "param5_prompt")
    @Field("param5_prompt")
    String param5Prompt;

    @JsonProperty(value = "param6")
    @JSONField(name = "param6")
    @Field("param6")
    String param6;

    @JsonProperty(value = "param6_desc")
    @JSONField(name = "param6_desc")
    @Field("param6_desc")
    String param6Desc;

    @JsonProperty(value= "param6_prompt")
    @JSONField(name = "param6_prompt")
    @Field("param6_prompt")
    String param6Prompt;
}