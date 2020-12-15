package com.and1ss.group_chat_service.api.rest;

import com.and1ss.group_chat_service.api.rest.dto.*;
import com.and1ss.group_chat_service.exceptions.BadRequestException;
import com.and1ss.group_chat_service.services.GroupChatMessageService;
import com.and1ss.group_chat_service.services.GroupChatService;
import com.and1ss.group_chat_service.services.UserService;
import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupChat;
import com.and1ss.group_chat_service.services.model.GroupMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chats/group")
public class GroupChatController {
    @Autowired
    GroupChatService groupChatService;

    @Autowired
    GroupChatMessageService groupChatMessageService;

    @Autowired
    UserService userService;

    @GetMapping("/all")
    public List<GroupChatRetrievalDTO>
    getAllGroupChats(@RequestHeader("Authorization") String token) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);

        List<GroupChat> groupChats =
                groupChatService.getAllGroupChatsForUser(authorizedUser);
        return groupChats.stream()
                .map(groupChat ->
                        formGroupChatRetrievalDTOForAuthorizedUser(
                                groupChat,
                                authorizedUser
                        )
                ).collect(Collectors.toList());
    }

    @GetMapping("/{chat_id}")
    public GroupChatRetrievalDTO getGroupChat(
            @PathVariable("chat_id") UUID chatId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);
        return formGroupChatRetrievalDTOForAuthorizedUser(groupChat, authorizedUser);
    }

    @PostMapping
    public GroupChatRetrievalDTO createGroupChat(
            @RequestBody GroupChatCreationDTO chatCreationDTO,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
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

        return formGroupChatRetrievalDTOForAuthorizedUser(createdChat, authorizedUser);
    }

    @PatchMapping("/{chat_id}")
    public void patchGroupChat(
            @RequestBody GroupChatPatchDTO chatPatchDTO,
            @PathVariable("chat_id") UUID chatId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService
                .getGroupChatById(chatId, authorizedUser);

        if (chatPatchDTO.getAbout() != null && !chatPatchDTO.getAbout().isEmpty()) {
            groupChat.setAbout(chatPatchDTO.getAbout());
        }

        if (chatPatchDTO.getTitle() != null && !chatPatchDTO.getTitle().isEmpty()) {
            groupChat.setTitle(chatPatchDTO.getTitle());
        }

        groupChatService.patchGroupChat(groupChat, authorizedUser);
    }

    @PostMapping("/{chat_id}/users")
    public void addUserToGroupChat(
            @PathVariable("chat_id") UUID chatId,
            @RequestHeader("Authorization") String token,
            @RequestBody UUID userId
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);
        AccountInfo toBeAddedUser = userService.findUserById(userId);
        groupChatService.addUser(groupChat, authorizedUser, toBeAddedUser);
    }

    @DeleteMapping("/{chat_id}/users/{user_id}")
    public void deleteUserFromGroupChat(
            @PathVariable("chat_id") UUID chatId,
            @PathVariable("user_id") UUID userId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);
        AccountInfo toBeDeletedUser = userService.findUserById(userId);
        groupChatService.deleteUser(groupChat, authorizedUser, toBeDeletedUser);
    }

    @GetMapping("/{chat_id}/messages")
    public List<GroupMessageRetrievalDTO> getGroupChatMessages(
            @PathVariable("chat_id") UUID chatId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);

        List<GroupMessageRetrievalDTO> messagesDTO = groupChatMessageService
                .getAllMessages(groupChat, authorizedUser)
                .stream()
                .map(GroupMessageRetrievalDTO::fromGroupMessage)
                .collect(Collectors.toList());


        return messagesDTO;
    }

    @PostMapping("/{chat_id}/messages")
    public GroupMessageRetrievalDTO addMessageToGroupChat(
            @RequestBody GroupMessageCreationDTO messageCreationDTO,
            @PathVariable("chat_id") UUID chatId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);
        GroupMessage message = GroupMessage.builder()
                .authorId(authorizedUser.getId())
                .chatId(chatId)
                .contents(messageCreationDTO.getContents())
                .build();

        GroupMessage savedMessage = groupChatMessageService
                .addMessage(groupChat, message, authorizedUser);

        return GroupMessageRetrievalDTO.fromGroupMessage(savedMessage);
    }

    @PatchMapping("/{chat_id}/messages/{message_id}")
    public GroupMessageRetrievalDTO patchMessageOfGroupChat(
            @RequestBody GroupMessageCreationDTO messageCreationDTO,
            @PathVariable("chat_id") UUID chatId,
            @PathVariable("message_id") UUID messageId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);
        GroupMessage message = GroupMessage.builder()
                .id(messageId)
                .authorId(authorizedUser.getId())
                .chatId(chatId)
                .contents(messageCreationDTO.getContents())
                .build();

        GroupMessage savedMessage = groupChatMessageService
                .patchMessage(groupChat, message, authorizedUser);

        return GroupMessageRetrievalDTO.fromGroupMessage(savedMessage);
    }

    @DeleteMapping("/{chat_id}/messages/{message_id}")
    public void patchMessageOfGroupChat(
            @PathVariable("chat_id") UUID chatId,
            @PathVariable("message_id") UUID messageId,
            @RequestHeader("Authorization") String token
    ) {
        AccountInfo authorizedUser = authorizeUserByBearerToken(token);
        GroupChat groupChat = groupChatService.getGroupChatById(chatId, authorizedUser);
        GroupMessage message = groupChatMessageService.getMessageById(messageId);

        groupChatMessageService
                .deleteMessage(groupChat, message, authorizedUser);
    }


    private AccountInfo authorizeUserByBearerToken(String token) {
        String parsedAccessToken = token.replaceFirst("Bearer\\s", "");
        return userService.authorizeUserByAccessToken(parsedAccessToken);
    }

    private GroupChatRetrievalDTO
    formGroupChatRetrievalDTOForAuthorizedUser(
            GroupChat groupChat,
            AccountInfo author
    ) {
        List<AccountInfo> participants =
                groupChatService.getGroupChatMembers(groupChat, author);
        return GroupChatRetrievalDTO.fromGroupChat(groupChat, participants);
    }
}
