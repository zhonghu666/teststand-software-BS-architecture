package com.cetiti.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface MqttProcessingService {

    void dataCallParse(String topic, String msg);
}
