package com.cetiti.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface MqttProcessingService {

    /**
     * 数据调用消息
     *
     * @param topic
     * @param msg
     */
    void dataCallParse(String topic, String msg);

    /**
     * @param topic
     * @param msg
     * @return
     */
    Boolean sceneDistributeResult(String topic, String msg);

    Boolean waitForResponse(String uuid);
}
