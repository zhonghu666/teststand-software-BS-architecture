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
public class MecSceneConfigAreaDto implements Serializable {

    @JsonProperty(value = "area")
    @JSONField(name = "area")
    @Field("area")
    List<MecSceneConfigPosition> area;

    @JsonProperty(value = "param7")
    @JSONField(name = "param7")
    @Field("param7")
    String param7;

    @JsonProperty(value = "param7_desc")
    @JSONField(name = "param7_desc")
    @Field("param7_desc")
    String param7Desc;

    @JsonProperty(value = "param7_prompt")
    @JSONField(name = "param7_prompt")
    @Field("param7_prompt")
    String param7Prompt;

    @JsonProperty(value = "param8")
    @JSONField(name = "param8")
    @Field("param8")
    String param8;

    @JsonProperty(value = "param8_desc")
    @JSONField(name = "param8_desc")
    @Field("param8_desc")
    String param8Desc;

    @JsonProperty(value = "param8_prompt")
    @JSONField(name = "param8_prompt")
    @Field("param8_prompt")
    String param8Prompt;

    @JsonProperty(value = "param9")
    @JSONField(name = "param9")
    @Field("param9")
    String param9;

    @JsonProperty(value = "param9_desc")
    @JSONField(name = "param9_desc")
    @Field("param9_desc")
    String param9Desc;

    @JsonProperty(value = "param9_prompt")
    @JSONField(name = "param9_prompt")
    @Field("param9_prompt")
    String param9Prompt;

    @JsonProperty(value = "param10")
    @JSONField(name = "param10")
    @Field("param10")
    String param10;

    @JsonProperty(value = "param10_desc")
    @JSONField(name = "param10_desc")
    @Field("param10_desc")
    String param10Desc;

    @JsonProperty(value = "param10_prompt")
    @JSONField(name = "param10_prompt")
    @Field("param10_prompt")
    String param10Prompt;

}