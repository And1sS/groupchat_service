package com.and1ss.group_chat_service.services.impl;

import com.and1ss.group_chat_service.exceptions.BadRequestException;
import com.and1ss.group_chat_service.exceptions.UnauthorizedException;
import com.and1ss.group_chat_service.services.GroupChatMessageService;
import com.and1ss.group_chat_service.services.GroupChatService;
import com.and1ss.group_chat_service.services.impl.repos.GroupMessageRepository;
import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupChat;
import com.and1ss.group_chat_service.services.model.GroupChatUser;
import com.and1ss.group_chat_service.services.model.GroupMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GroupChatMessageServiceImpl implements GroupChatMessageService {
    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Override
    public List<GroupMessage> getAllMessages(GroupChat groupChat, AccountInfo author) {
        if (!groupChatService.userMemberOfGroupChat(groupChat, author)) {
            throw new UnauthorizedException("This user can not view messages of this chat");
        }

        return groupMessageRepository.getGroupMessagesByChatId(groupChat.getId());
    }

    @Override
    public GroupMessage addMessage(GroupChat groupChat, GroupMessage message, AccountInfo author) {
        if (!groupChatService.userMemberOfGroupChat(groupChat, author)) {
            throw new UnauthorizedException("This user can not write messages to this chat");
        }

        if (message.getContents().isEmpty()) {
            throw new BadRequestException("Message contents must not be empty");
        }

        return groupMessageRepository.save(message);
    }

    @Override
    public GroupMessage patchMessage(GroupChat groupChat, GroupMessage message, AccountInfo author) {
        try {
            groupMessageRepository.getOne(message.getId());
        } catch (Exception e) {
            throw new BadRequestException("Invalid message Id");
        }

        if (!userCanPatchMessage(groupChat, message, author)) {
            throw new UnauthorizedException("This user can not patch this message");
        }

        return groupMessageRepository.save(message);
    }

    @Override
    public GroupMessage getMessageById(UUID id) {
        try {
            return groupMessageRepository.getOne(id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid message Id");
        }
    }

    private boolean userCanPatchMessage(GroupChat groupChat, GroupMessage message, AccountInfo user) {
        return groupChatService.userMemberOfGroupChat(groupChat, user) &&
                message.getAuthorId().equals(user.getId());
    }

    private boolean userCanDeleteMessage(GroupChat groupChat, GroupMessage message, AccountInfo user) {
        GroupChatUser.MemberType memberType;
        try {
            memberType = groupChatService.getUserMemberType(groupChat, user);
        } catch (BadRequestException e) {
            return false;
        }

        return memberType == GroupChatUser.MemberType.admin ||
                groupChat.getCreatorId().equals(user.getId()) ||
                message.getAuthorId().equals(user.getId());
    }

    @Override
    public void deleteMessage(GroupChat groupChat, GroupMessage message, AccountInfo author) {
        try {
            groupMessageRepository.getOne(message.getId());
        } catch (Exception e) {
            throw new BadRequestException("Invalid message Id");
        }

        if (!userCanDeleteMessage(groupChat, message, author)) {
            throw new UnauthorizedException("This user can not delete this message");
        }
        groupMessageRepository.delete(message);
    }
}
