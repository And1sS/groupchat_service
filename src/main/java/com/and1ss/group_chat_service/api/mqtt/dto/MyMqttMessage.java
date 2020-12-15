package com.and1ss.group_chat_service.api.mqtt.dto;

import lombok.Data;

@Data
public class MyMqttMessage<T> {
    String token;

    T payload;
}
