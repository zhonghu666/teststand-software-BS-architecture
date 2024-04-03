package com.cetiti.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author ZYL
 * @date 2023/9/8 10:24
 * @from ENJOYOR
 * five star
 */
@Data
public class MecSceneConfigSubDto implements Serializable {

    @JsonProperty(value = "event_name")
    @JSONField(name = "event_name")
    @Field("event_name")
    String eventName;

    @JsonProperty(value = "ReverseEvent")
    @JSONField(name = "ReverseEvent")
    @Field("ReverseEvent")
    MecSceneConfigDetailDto reverseEvent;

    @JsonProperty(value = "SlowEvent")
    @JSONField(name = "SlowEvent")
    @Field("SlowEvent")
    MecSceneConfigDetailDto slowEvent;

    @JsonProperty(value = "SpeedEvent")
    @JSONField(name = "SpeedEvent")
    @Field("SpeedEvent")
    MecSceneConfigDetailDto speedEvent;

    @JsonProperty(value = "carRealline")
    @JSONField(name = "carRealline")
    @Field("carRealline")
    MecSceneConfigDetailDto carRealline;

    @JsonProperty(value = "notAllowDrect")
    @JSONField(name = "notAllowDrect")
    @Field("notAllowDrect")
    MecSceneConfigDetailDto notAllowDrect;

    @JsonProperty(value = "RunRedLighEvent")
    @JSONField(name = "RunRedLighEvent")
    @Field("RunRedLighEvent")
    MecSceneConfigDetailDto runRedLighEvent;

    @JsonProperty(value = "IllegalOccupancy")
    @JSONField(name = "IllegalOccupancy")
    @Field("IllegalOccupancy")
    MecSceneConfigDetailDto illegalOccupancy;

    @JsonProperty(value = "AccidenteEvent")
    @JSONField(name = "AccidenteEvent")
    @Field("accidenteEvent")
    MecSceneConfigDetailDto accidenteEvent;

    @JsonProperty(value = "RoadWork")
    @JSONField(name = "RoadWork")
    @Field("RoadWork")
    MecSceneConfigDetailDto roadWork;

    @JsonProperty(value = "TrafficCongestion")
    @JSONField(name = "TrafficCongestion")
    @Field("TrafficCongestion")
    MecSceneConfigDetailDto trafficCongestion;

    @JsonProperty(value = "ParkingEvent")
    @JSONField(name = "ParkingEvent")
    @Field("ParkingEvent")
    MecSceneConfigDetailDto parkingEvent;

    @JsonProperty(value = "DrivingAwayEvent")
    @JSONField(name = "DrivingAwayEvent")
    @Field("DrivingAwayEvent")
    MecSceneConfigDetailDto drivingAwayEvent;

    @JsonProperty(value = "OccupyLane")
    @JSONField(name = "OccupyLane")
    @Field("OccupyLane")
    MecSceneConfigDetailDto occupyLane;

    @JsonProperty(value = "PedestriansInto")
    @JSONField(name = "PedestriansInto")
    @Field("PedestriansInto")
    MecSceneConfigDetailDto pedestriansInto;

    @JsonProperty(value = "ChangeLane")
    @JSONField(name = "ChangeLane")
    @Field("ChangeLane")
    MecSceneConfigDetailDto changeLane;

    @JsonProperty(value = "OnZebra")
    @JSONField(name = "OnZebra")
    @Field("OnZebra")
    MecSceneConfigDetailDto onZebra;

    @JsonProperty(value = "CarBack")
    @JSONField(name = "CarBack")
    @Field("CarBack")
    MecSceneConfigDetailDto carBack;

}
