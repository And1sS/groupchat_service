package com.and1ss.group_chat_service.config;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class MqttConfig {
    private final String address = "tcp://localhost:1883";

    private final MqttCallback mqttCallback;

    @Autowired
    public MqttConfig(MqttCallback mqttCallback) {
        this.mqttCallback = mqttCallback;
    }
    @Bean
    public IMqttClient getClient() throws MqttException {
        IMqttClient client = new MqttClient(address,
                UUID.randomUUID().toString(), new MemoryPersistence());

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setConnectionTimeout(10);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setMaxReconnectDelay(10000);

        client.setCallback(mqttCallback);
        client.connect(connectOptions);

        subscribe(client);

        return client;
    }

    private void subscribe(IMqttClient client) throws MqttException {
        client.subscribe("/group-chat-service/new-chat");
        client.subscribe("/group-chat-service/new-message");
    }
}