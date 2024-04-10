package com.cetiti.entity.step;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cetiti.annotation.ActionValid;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.RestPathConfig;
import com.cetiti.constant.ActionType;
import com.cetiti.constant.DeviceType;
import com.cetiti.constant.StepStatus;
import com.cetiti.dto.*;
import com.cetiti.entity.RestResult;
import com.cetiti.entity.StepVariable;
import com.cetiti.service.MqttProcessingService;
import com.cetiti.service.impl.CacheService;
import com.cetiti.utils.RedisUtil;
import com.cetiti.utils.RestUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@ActionValid
public class ActionStep extends StepBase {

    @ApiModelProperty("是否记录动作配置")
    @NotNull(message = "是否记录动作配置不能为空")
    private Boolean configEnable;
    @ApiModelProperty("动作类型")
    @NotNull(message = "动作类型不能为空")
    private ActionType actionType;
    @ApiModelProperty("设备类型")
    private DeviceType deviceType;
    @ApiModelProperty("子类型")
    private Integer childrenType;
    @ApiModelProperty("设备编号")
    private String esn;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("场景编号")
    private String sceneId;
    @ApiModelProperty("场景名称")
    private String sceneName;
    @ApiModelProperty("数据记录仪 1开始存 2结束存 3开始传 4结束传")
    private Integer action;
    @ApiModelProperty("广播配置")
    private RsuBroadcastDto rsuBroadcastDto;
    @ApiModelProperty("CGF下发")
    private RsuCfgDto rsuCfgDto;
    @ApiModelProperty("场景算法使能")
    private RsuSceneConfigDto rsuSceneConfigDto;
    @ApiModelProperty("Mec交通事件配置")
    private MecSceneConfigDto mecSceneConfigDto;
    @ApiModelProperty("信号机控制")
    private Pl7SignalFaDto pl7SignalFaDto;
    @ApiModelProperty("Obu场景算法配置")
    private List<ObuSceneConfigDto> obuSceneConfigDtoList;
    @ApiModelProperty("数据记录仪")
    private DataRecordInfoDto dataRecordInfoDto;
    @ApiModelProperty("数据预制")
    private SceneDistributeConfigDto sceneDistributeConfigDto;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        StepVariable step = StepVariable.RESULT_SUCCESS(StepStatus.DONE);
        step.addNestedAttribute("ActionSettings.configEnable", configEnable, "是否记录动作配置");
        RestUtil rest = ApplicationContextHolder.getBean(RestUtil.class);
        RestPathConfig restPathConfig = ApplicationContextHolder.getBean(RestPathConfig.class);
        StepVariable stepVariable2 = cacheService.getStepVariable(testSequenceId);
        switch (actionType) {
            case CONFIGURATION:
                step.addNestedAttribute("ActionSettings.actionType", actionType.name(), "类型");
                step.addNestedAttribute("ActionSettings.deviceType", deviceType.name(), "设备类型");
                step.addNestedAttribute("ActionSettings.esn", esn, "设备编号");
                switch (deviceType) {
                    case RSU:
                        if (rsuBroadcastDto != null && rsuBroadcastDto.isChange()) {
                          /*  step.addNestedAttribute("ActionSettings.platV2xEnable", rsuBroadcastDto.getPlatV2xEnable(), "平台下发V2X是否广播");
                            step.addNestedAttribute("ActionSettings.localV2xEnable", rsuBroadcastDto.getLocalV2xEnable(), "本地配置V2X是否广播");
                            step.addNestedAttribute("ActionSettings.rsiFrequency", rsuBroadcastDto.getRsiFrequency(), "rsi频率（hz）");
                            step.addNestedAttribute("ActionSettings.rsmFrequency", rsuBroadcastDto.getRsmFrequency(), "rsm频率（hz）");
                            step.addNestedAttribute("ActionSettings.spatFrequency", rsuBroadcastDto.getSpatFrequency(), "spat频率（hz）");
                            step.addNestedAttribute("ActionSettings.bsmFrequency", rsuBroadcastDto.getBsmFrequency(), "bsm频率（hz）");*/
                            rsuBroadcastDto.setEsn(esn);
                            rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/saveRsuBroadcast", rsuBroadcastDto, null, HttpMethod.POST, null);
                        }
                        //
                        if (rsuCfgDto != null && rsuCfgDto.isChange()) {
                            step.addNestedAttribute("ActionSettings.sendBsm", rsuCfgDto.isSendBsm(), "BSM数据上报");
                            step.addNestedAttribute("ActionSettings.sendMap", rsuCfgDto.isSendMap(), "MAP数据上报");
                            step.addNestedAttribute("ActionSettings.sendSpat", rsuCfgDto.isSendSpat(), "SPAT数据上报");
                            step.addNestedAttribute("ActionSettings.sendRsi", rsuCfgDto.isSendRsi(), "RSI数据上报");
                            step.addNestedAttribute("ActionSettings.sendRsm", rsuCfgDto.isSendRsm(), "RSM数据上报");
                            step.addNestedAttribute("ActionSettings.sendHeartbeat", rsuCfgDto.isSendHeartbeat(), "心跳数据上报");
                            step.addNestedAttribute("ActionSettings.sendStatus", rsuCfgDto.isSendStatus(), "状态数据上报");
                            rsuCfgDto.setEsn(esn);
                            //获取id
                            RestResult rr = rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/getRsuCfg", null, "esn=" + esn, HttpMethod.GET, null);
                            RsuCfgDto rsuCfgDto1 = JSON.parseObject(JSON.toJSONString(rr.getData()), RsuCfgDto.class);
                            rsuCfgDto.setId(rsuCfgDto1.getId());
                            rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/saveRsuCfg", rsuCfgDto, null, HttpMethod.POST, null);
                            log.info("rsu cfg:{}", JSON.toJSONString(rsuCfgDto));

                        }
                        if (rsuSceneConfigDto != null && rsuSceneConfigDto.isChange()) {
                            step.addNestedAttribute("ActionSettings.sdsEnable", rsuSceneConfigDto.isSdsEnable(), "感知数据共享");
                            step.addNestedAttribute("ActionSettings.clcEnable", rsuSceneConfigDto.isClcEnable(), "协作式变道");
                            step.addNestedAttribute("ActionSettings.cvmRampInEnable", rsuSceneConfigDto.isCvmRampInEnable(), "协作式匝道汇入");
                            step.addNestedAttribute("ActionSettings.cipLanechangingAtIntersectionEnable", rsuSceneConfigDto.isCipLanechangingAtIntersectionEnable(), "协作式交叉口提前引导车辆换道");
                            step.addNestedAttribute("ActionSettings.cipNoSignalPassingEnable", rsuSceneConfigDto.isCipNoSignalPassingEnable(), "协作式无信控交叉口通行");
                            step.addNestedAttribute("ActionSettings.ddsEnable", rsuSceneConfigDto.isDdsEnable(), "差分数据服务");
                            step.addNestedAttribute("ActionSettings.dlmEnable", rsuSceneConfigDto.isDlmEnable(), "动态车道管理");
                            step.addNestedAttribute("ActionSettings.chpvpLaneReservationEnable", rsuSceneConfigDto.isChpvpLaneReservationEnable(), "协作式优先车辆通行--车道预留");
                            step.addNestedAttribute("ActionSettings.chpvpLaneRestrictionEnable", rsuSceneConfigDto.isChpvpLaneRestrictionEnable(), "协作式优先车辆通行--车道封闭/禁行");
                            step.addNestedAttribute("ActionSettings.chpvpSignalPriorityEnable", rsuSceneConfigDto.isChpvpLaneRestrictionEnable(), "协作式优先车辆通行--信号优先");
                            step.addNestedAttribute("ActionSettings.gspaEnable", rsuSceneConfigDto.isGspaEnable(), "场站路径引导");
                            step.addNestedAttribute("ActionSettings.rtsEnable", rsuSceneConfigDto.isRtsEnable(), "道路收费服务");
                            rsuSceneConfigDto.setEsn(esn);
                            rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/saveRsuSceneConfig", rsuSceneConfigDto, null, HttpMethod.POST, null);
                        }
                        break;
                    case MEC:
                        step.addNestedAttribute("ActionSettings.mecId", mecSceneConfigDto.getMecId(), "");
                        MecSceneConfigSubDto trafficEventCfg = mecSceneConfigDto.getTrafficEventCfg();
                        step.addNestedAttribute("ActionSettings.mecSceneConfig", "Mec交通事件配置", "Mec交通事件配置");
                        mecSceneConfigDto.setEsn(esn);
                        rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/saveMecSceneConfig", mecSceneConfigDto, null, HttpMethod.POST, null);
                        break;
                    case SIGNAL:
                        step.addNestedAttribute("ActionSettings.number", pl7SignalFaDto.getNumber(), "方案编号");
                        String signalParam = "esn=" + esn + "&type=" + pl7SignalFaDto.getNumber();
                        rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/setTrafficLightContrType", null, signalParam, HttpMethod.POST, null);
                        break;
                    case OBU:
                        for (ObuSceneConfigDto obuSceneConfigDto : obuSceneConfigDtoList) {
                            StepVariable stepVariable = new StepVariable();
                            stepVariable.addNestedAttribute("isEnable", obuSceneConfigDto.getIsEnable(), "");
                            stepVariable.addNestedAttribute("appType", obuSceneConfigDto.getAppType(), "");
                            stepVariable.addNestedAttribute("appTypeName", obuSceneConfigDto.getAppTypeName(), "");
                            stepVariable.addNestedAttribute("angle_parallel_threshold", obuSceneConfigDto.getAngle_parallel_threshold(), "");
                            stepVariable.addNestedAttribute("angle_vertical_threshold", obuSceneConfigDto.getAngle_vertical_threshold(), "");
                            stepVariable.addNestedAttribute("angle_Opposite_threshold", obuSceneConfigDto.getAngle_Opposite_threshold(), "");
                            stepVariable.addNestedAttribute("dist_maxRange", obuSceneConfigDto.getDist_maxRange(), "");
                            stepVariable.addNestedAttribute("dist_minRange", obuSceneConfigDto.getDist_minRange(), "");
                            stepVariable.addNestedAttribute("ttc_maxRange", obuSceneConfigDto.getTtc_maxRange(), "");
                            stepVariable.addNestedAttribute("ttc_minRange", obuSceneConfigDto.getTtc_minRange(), "");
                            stepVariable.addNestedAttribute("hvSpeed_minRange", obuSceneConfigDto.getHvSpeed_minRange(), "");
                            stepVariable.addNestedAttribute("rvSpeed_minRange", obuSceneConfigDto.getRvSpeed_minRange(), "");
                            stepVariable.addNestedAttribute("deltattc_maxRange", obuSceneConfigDto.getDeltattc_maxRange(), "");
                            stepVariable.addNestedAttribute("vruhonDist_Range", obuSceneConfigDto.getVruhonDist_Range(), "");
                            stepVariable.addNestedAttribute("blindtime_maxRange", obuSceneConfigDto.getBlindtime_maxRange(), "");
                            stepVariable.addNestedAttribute("blindtime_minRange", obuSceneConfigDto.getBlindtime_minRange(), "");
                            step.addToListAtPath("ActionSettings.obu", stepVariable);
                            obuSceneConfigDto.setEsn(esn);
                            rest.getResultFromApi(restPathConfig.getBaseApi() + "/equ/config/saveObuSceneConfig", obuSceneConfigDto, null, HttpMethod.POST, null);
                        }
                        break;
                    case DATA_RECORDING_DEVICE:
                        step.addNestedAttribute("ActionSettings.action", action, "数据记录仪1开始存 2结束存 3开始传 4结束传");
                        switch (action) {
                            case 1:
                                String startSaveParam = "esn=" + esn;
                                rest.getResultFromApi(restPathConfig.getArtificial() + "/equ/config/dataRecorder/startSave", null, startSaveParam, HttpMethod.POST, null);
                                break;
                            case 2:
                                String endSaveParam = "esn=" + esn;
                                rest.getResultFromApi(restPathConfig.getArtificial() + "/equ/config/dataRecorder/stopSave", null, endSaveParam, HttpMethod.POST, null);
                                break;
                            case 3:
                                String siteId = (String) pram.get("siteId");
                                step.addNestedAttribute("ActionSettings.siteId", siteId, "场地编号");
                                String startUploadParam = "siteId=" + siteId + "&esn=" + esn;
                                RestResult resultFromApi = rest.getResultFromApi(restPathConfig.getAnalysis() + "/scene/data/startDataRecord", null, startUploadParam, HttpMethod.POST, null);
                                String jsonString = JSON.toJSONString(resultFromApi.getData());
                                RedisUtil redisUtil = ApplicationContextHolder.getBean(RedisUtil.class);
                                redisUtil.set(testSequenceId + "recordId", jsonString);
                                break;
                            case 4:
                                step.addNestedAttribute("ActionSettings.siteId", (String) pram.get("siteId"), "场地编号");
                                step.addNestedAttribute("ActionSettings.siteName", (String) pram.get("siteName"), "场地名称");
                                String endUploadParam = "siteId=" + pram.get("siteId") + "&siteName=" + URLEncoder.encode((String) pram.get("siteName"), StandardCharsets.UTF_8) + "&esn=" + esn;
                                rest.getResultFromApi(restPathConfig.getAnalysis() + "/scene/data/finishDataRecord", null, endUploadParam, HttpMethod.POST, null);
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported device type");
                }
                break;
            case SCENE:
                switch (childrenType) {
                    case 1:
                        for (SceneDistributeConfigSubDto rsuSceneConfig : sceneDistributeConfigDto.getRsuSceneConfigs()) {
                            StepVariable stepVariable1 = new StepVariable();
                            stepVariable1.addNestedAttribute("esn", rsuSceneConfig.getEsn(), "rsu的esn");
                            stepVariable1.addNestedAttribute("sceneConfigs", rsuSceneConfig.getSceneConfigs(), "下发信息的主键编号");
                            step.addToListAtPath("ActionSettings.rsuSceneConfigs", stepVariable1);
                        }
                        sceneDistributeConfigDto.setSceneId(sceneId);
                        RestResult resultFromApi = rest.getResultFromApi(restPathConfig.getArtificial() + "/test/data/preSendSceneInfo", sceneDistributeConfigDto, null, HttpMethod.POST, null);
                        String jsonString = JSON.toJSONString(resultFromApi.getData());
                        JSONObject jsonObject = JSON.parseObject(jsonString);
                        String uuid = (String) jsonObject.get("uuid");
                        stepVariable2.addNestedAttribute("RunState.Scene." + sceneId, uuid, "数据预制");
                        cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable2);
                        MqttProcessingService mqttProcessingService = ApplicationContextHolder.getBean(MqttProcessingService.class);
                        if (!mqttProcessingService.waitForResponse(uuid)) {
                            step = StepVariable.RESULT_Fail(StepStatus.ERROR);
                        }
                        break;
                    case 2:
                        String valueByPath = stepVariable2.getValueByPath("RunState.Scene." + sceneId);
                        String sceneBroadcastParam = "uuid=" + valueByPath;
                        rest.getResultFromApi(restPathConfig.getArtificial() + "/test/data/startDistributeData", null, sceneBroadcastParam, HttpMethod.POST, null);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported children type");
                }
                step.addNestedAttribute("ActionSettings.actionType", actionType.name(), "类型");
                step.addNestedAttribute("ActionSettings.childrenType", childrenType, "子类型");
                step.addNestedAttribute("ActionSettings.sceneId", sceneId, "场景编号");
                break;
            default:
                throw new IllegalArgumentException("Unsupported action type");
        }
        return step;

    }
}
