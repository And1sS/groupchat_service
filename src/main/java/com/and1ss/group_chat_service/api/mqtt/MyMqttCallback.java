package com.and1ss.group_chat_service.api.mqtt;

import com.and1ss.group_chat_service.api.mqtt.dto.MyMqttMessage;
import com.and1ss.group_chat_service.api.rest.dto.GroupChatCreationDTO;
import com.and1ss.group_chat_service.api.rest.dto.GroupMessageCreationDTO;
import com.and1ss.group_chat_service.exceptions.BadRequestException;
import com.and1ss.group_chat_service.services.GroupChatMessageService;
import com.and1ss.group_chat_service.services.GroupChatService;
import com.and1ss.group_chat_service.services.UserService;
import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupChat;
import com.and1ss.group_chat_service.services.model.GroupMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MyMqttCallback implements MqttCallback {
    private final GroupChatService groupChatService;
    private final GroupChatMessageService groupChatMessageService;
    private final UserService userService;

    @Autowired
    public MyMqttCallback(
        GroupChatService groupChatService,
        GroupChatMessageService groupChatMessageService,
        UserService userService
    ) {
        this.groupChatService = groupChatService;
        this.groupChatMessageService = groupChatMessageService;
        this.userService = userService;
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        log.info("Message arrived on topic: " + topic + ", message: " + message);

        try {
            switch (topic) {
                case "/group-chat-service/new-message":
                    handleNewMessageCreation(message);
                    break;

                case "/group-chat-service/new-chat":
                    handleNewChatCreation(message);
                    break;

                default: log.error("Message on invalid topic arrived!");
            }
        } catch (Exception e) {
            log.error("topic: " + topic + " " + e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private void handleNewChatCreation(MqttMessage message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var typeRef = new TypeReference<MyMqttMessage<GroupChatCreationDTO>>() {};

        String messageAsString = new String(message.getPayload(), StandardCharsets.UTF_8);
        MyMqttMessage<GroupChatCreationDTO> object = mapper.readValue(messageAsString, typeRef);

        var stringToken = (String) object.getToken();
        if (stringToken == null) {
            throw new BadRequestException("Access token is not specified");
        }
        final var authorizedUser = userService.authorizeUserByAccessToken(stringToken);

        var payload = object.getPayload();
        if (payload == null) {
            throw new BadRequestException("Incoming message payload is not present");
        }
        final var chatCreationDTO = mapper.convertValue(
                payload, GroupChatCreationDTO.class
        );

        List<UUID> participantsIds = chatCreationDTO.getParticipants();

        System.out.println(authorizedUser.toString());
        List<AccountInfo> participants =
                userService.findUsersByListOfIds(participantsIds);

        if (participants.size() < 2) {
            throw new BadRequestException("Chats must have at least two members");
        }

        GroupChat toBeCreated = GroupChat.builder()
                .title(chatCreationDTO.getTitle())
                .about(chatCreationDTO.getAbout())
                .creatorId(authorizedUser.getId())
                .build();

        GroupChat createdChat = groupChatService
                .createGroupChat(toBeCreated, participants, authorizedUser);

        log.info("Created chat: " + createdChat.toString());
    }

    private void handleNewMessageCreation(MqttMessage message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var typeRef = new TypeReference<MyMqttMessage<GroupMessageCreationDTO>>() {};

        String messageAsString = new String(message.getPayload(), StandardCharsets.UTF_8);
        MyMqttMessage<GroupMessageCreationDTO> object = mapper.readValue(messageAsString, typeRef);

        var stringToken = (String) object.getToken();
        if (stringToken == null) {
            throw new BadRequestException("Access token is not specified");
        }
        final var authorizedUser = userService.authorizeUserByAccessToken(stringToken);

        var payload = object.getPayload();
        if (payload == null) {
            throw new BadRequestException("Incoming message payload is not present");
        }
        final var messageDTO = mapper.convertValue(
                payload, GroupMessageCreationDTO.class
        );

        final var groupChat = groupChatService.getGroupChatById(
                messageDTO.getChatId(),
                authorizedUser
        );
        final var groupMessage = GroupMessage.builder()
                .authorId(authorizedUser.getId())
                .chatId(groupChat.getId())
                .contents(messageDTO.getContents())
                .build();

        final var savedMessage = groupChatMessageService
                .addMessage(groupChat, groupMessage, authorizedUser);

        log.info("Created message: " + savedMessage.toString());
    }
}
