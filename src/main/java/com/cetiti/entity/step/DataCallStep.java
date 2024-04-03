package com.cetiti.entity.step;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.ApplicationContextHolder;
import com.cetiti.config.IMqttSender;
import com.cetiti.constant.StepStatus;
import com.cetiti.entity.DataCallField;
import com.cetiti.entity.StepVariable;
import com.cetiti.service.impl.CacheService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class DataCallStep extends StepBase {
    @ApiModelProperty("频率")
    @NotNull(message = "频率不能为空")
    private String frequency;

    @ApiModelProperty("字段列表")
    private List<DataCallField> dataCallFields;

    @Override
    protected StepVariable performSpecificTask(CacheService cacheService, Map<String, Object> pram) {
        DataCallStencil dataCallStencil = new DataCallStencil();
        StepVariable stepVariable = cacheService.getStepVariable(testSequenceId);
        stepVariable.addNestedAttribute("RunState.DataCallStatus", true, "数据调用开关");
        cacheService.saveOrUpdateStepVariable(testSequenceId, stepVariable);
        dataCallStencil.setMainProto(frequency);
        log.info("数据调用请求字段解析开始");
        Map<String, List<String>> groupedData = dataCallFields.stream()
                .map(s -> s.getOriginalPath().split(":")[0])
                .map(s -> s.split("[.]"))
                .filter(parts -> parts.length > 1)
                .collect(Collectors.groupingBy(
                        parts -> parts[1],
                        Collectors.mapping(
                                parts -> parts[0],
                                Collectors.collectingAndThen(Collectors.toSet(), ArrayList::new)
                        )
                ));
        List<Info> infos = groupedData.entrySet().stream()
                .map(entry -> new Info(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        dataCallStencil.setId(testSequenceId);
        dataCallStencil.setInfo(infos);
        dataCallStencil.setTestStart(true);
        IMqttSender iMqttSender = ApplicationContextHolder.getBean(IMqttSender.class);
        log.info("数据调用mqtt发送:{}", JSON.toJSON(dataCallStencil));
        iMqttSender.sendToMqtt((String) pram.get("DATA_CALL_TOPIC"), JSON.toJSONString(dataCallStencil));
        return StepVariable.RESULT_SUCCESS(StepStatus.DONE);
    }
}

@Data
 class Info implements Serializable {

    @ApiModelProperty("协议名称")
    private String name;

    @ApiModelProperty("协议唯一标识")
    private List<String> esn;

    public Info(String name, List<String> esnList) {
        this.name = name;
        this.esn = esnList;
    }
}

