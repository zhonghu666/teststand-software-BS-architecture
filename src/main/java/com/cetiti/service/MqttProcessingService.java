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
     * 数据预制状态消息
     *
     * @param topic
     * @param msg
     * @return
     */
    Boolean sceneDistributeResult(String topic, String msg);

    /**
     * 监听消息
     *
     * @param uuid
     * @return
     */
    Boolean waitForResponse(String uuid);

    /**
     * 开始自定义信号同步
     *
     * @param topic
     * @param msg
     */
    void startCustomSignal(String topic, String msg);

    /**
     * 解析自定义信号并转发结果
     *
     * @param topic
     * @param msg
     */
    void parseCustomSignal(String topic, String msg);
}
