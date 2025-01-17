package com.cetiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.cetiti.config.IMqttSender;
import com.cetiti.config.TokenManagerConfig;
import com.cetiti.constant.Assert;
import com.cetiti.constant.ValueType;
import com.cetiti.entity.StepVariable;
import com.cetiti.entity.step.DataCallInfo;
import com.cetiti.entity.step.DataCallStencil;
import com.cetiti.expression.GrammarCheckUtils;
import com.cetiti.request.CustomSignalParesRequest;
import com.cetiti.request.CustomSignalRequest;
import com.cetiti.response.BracketValidationResponse;
import com.cetiti.service.CustomSignalService;
import com.cetiti.service.TestSequenceService;
import com.cetiti.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomSignalServiceImpl implements CustomSignalService {

    @Resource
    private TokenManagerConfig tokenManagerConfig;

    @Resource
    private IMqttSender iMqttSender;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private TestSequenceService testSequenceService;

    @Resource
    private GrammarCheckUtils grammarCheckUtils;

    @Override
    public Boolean startCustomSignal(CustomSignalRequest request) {
        log.info("开始/停止自定义信号数据同步入参:{}", JSON.toJSON(request));
        DataCallStencil dataCallStencil = new DataCallStencil();
        if (request.getStart()) {
            Map<String, List<String>> groupedData = request.getCustomSignalFieldRequests().stream()
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
            List<DataCallInfo> infos = groupedData.entrySet().stream()
                    .map(entry -> new DataCallInfo(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            dataCallStencil.setMainProto(request.getInterval());
            dataCallStencil.setId(request.getEsn());
            dataCallStencil.setInfo(infos);
            dataCallStencil.setTestStart(true);
            log.info("自定义信号开始mqtt发送:{}", JSON.toJSON(dataCallStencil));
            String token = tokenManagerConfig.manageToken(request.getStart(), request.getEsn());
            Assert.handle(token != null, "数据拉齐窗口消耗完，请等待", false);
            iMqttSender.sendToMqtt(token, JSON.toJSONString(dataCallStencil));
            redisUtil.set(request.getEsn() + "startCustomSignal", request.getCustomSignalFieldRequests());
        } else {
            dataCallStencil.setTestStart(false);
            dataCallStencil.setId(request.getEsn());
            String token = tokenManagerConfig.manageToken(request.getStart(), request.getEsn());
            Assert.handle(token != null, "数据拉齐窗口回收失败", false);
            log.info("自定义信号结束");
            iMqttSender.sendToMqtt(token, JSON.toJSONString(dataCallStencil));
            redisUtil.del(request.getEsn() + "startCustomSignal");
        }
        return true;
    }

    @Override
    public BracketValidationResponse parseCustomSignal(CustomSignalParesRequest request) {
        log.info("解析自定义信号表达式入参:{}", JSON.toJSON(request));
        StepVariable stepVariable = new StepVariable();
        for (Map.Entry<String, String> entry : request.getParam().entrySet()) {
            String key = entry.getKey();
            String valueType = entry.getValue();
            Object value = null;
            if (ValueType.NUMBER.name().equals(valueType)) {
                value = 0;
            } else if (ValueType.STRING.name().equals(valueType)) {
                value = "a";
            } else if (ValueType.TREE_NODE.name().equals(valueType)) {
                value = new StepVariable();
            } else if (ValueType.LIST.name().equals(valueType)) {
                value = new ArrayList<>();
            } else if (ValueType.BOOLEAN.name().equals(valueType)) {
                value = true;
            }
            stepVariable.addNestedAttributeObject(key, value, "");
        }
        BracketValidationResponse response = new BracketValidationResponse();
        grammarCheckUtils.hasMatchingBrackets(request.getExpression(), response);
        if (!response.isValid()) {
            return response;
        }
        grammarCheckUtils.processExpression(request.getExpression(), stepVariable, response, request.getType(), false);
        if (!response.isValid()) {
            redisUtil.hset(request.getUuid() + "parseCustomSignal", request.getName(), request, 3600);
        }
        return response;
    }


    @Override
    public Boolean removeCustomSignal(String name, String uuid) {
        if (uuid != null) {
            redisUtil.del(uuid + "parseCustomSignal");
        } else {
            redisUtil.hdel(uuid + "parseCustomSignal", name);
        }
        return true;
    }

    @Override
    public BracketValidationResponse checkCustomSignalSyntax(CustomSignalParesRequest request) {
        StepVariable stepVariable = new StepVariable();
        for (Map.Entry<String, String> entry : request.getParam().entrySet()) {
            String key = entry.getKey();
            String valueType = entry.getValue();
            Object value = null;
            if (ValueType.NUMBER.name().equals(valueType)) {
                value = 0;
            } else if (ValueType.STRING.name().equals(valueType)) {
                value = "a";
            } else if (ValueType.TREE_NODE.name().equals(valueType)) {
                value = new StepVariable();
            } else if (ValueType.LIST.name().equals(valueType)) {
                value = new ArrayList<>();
            } else if (ValueType.BOOLEAN.name().equals(valueType)) {
                value = true;
            }
            stepVariable.addNestedAttributeObject(key, value, "");
        }
        BracketValidationResponse response = new BracketValidationResponse();
        grammarCheckUtils.hasMatchingBrackets(request.getExpression(), response);
        if (!response.isValid()) {
            return response;
        }
        grammarCheckUtils.processExpression(request.getExpression(), stepVariable, response, request.getType(), false);
        return response;
    }
}
