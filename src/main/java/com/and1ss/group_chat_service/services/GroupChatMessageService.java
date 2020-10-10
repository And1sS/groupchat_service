package com.and1ss.group_chat_service.services;

import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupChat;
import com.and1ss.group_chat_service.services.model.GroupMessage;

import java.util.List;
import java.util.UUID;

public interface GroupChatMessageService {
    List<GroupMessage> getAllMessages(GroupChat groupChat, AccountInfo author);
    GroupMessage addMessage(GroupChat groupChat, GroupMessage message, AccountInfo author);
    GroupMessage patchMessage(GroupChat groupChat, GroupMessage message, AccountInfo author);
    GroupMessage getMessageById(UUID id);
    void deleteMessage(GroupChat groupChat, GroupMessage message, AccountInfo author);
}
