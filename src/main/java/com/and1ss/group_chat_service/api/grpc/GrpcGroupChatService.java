package com.and1ss.group_chat_service.api.grpc;

import com.and1ss.group_chat_service.*;
import com.and1ss.group_chat_service.api.dto.GroupChatRetrievalDTO;
import com.and1ss.group_chat_service.exceptions.BadRequestException;
import com.and1ss.group_chat_service.services.GroupChatMessageService;
import com.and1ss.group_chat_service.services.GroupChatService;
import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupChat;
import com.and1ss.group_chat_service.services.model.GroupMessage;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GRpcService
public class GrpcGroupChatService extends
        GrpcGroupChatServiceGrpc.GrpcGroupChatServiceImplBase {

    private final GrpcUserServiceConnection grpcUserServiceConnection;
    private final GroupChatService groupChatService;
    private final GroupChatMessageService groupChatMessageService;

    @Autowired
    public GrpcGroupChatService(
            GrpcUserServiceConnection grpcUserServiceConnection,
            GroupChatService groupChatService,
            GroupChatMessageService groupChatMessageService
    ) {
        this.grpcUserServiceConnection = grpcUserServiceConnection;
        this.groupChatService = groupChatService;
        this.groupChatMessageService = groupChatMessageService;
    }

    @Override
    @Transactional
    public void getAllChats(
            GrpcAccessTokenIncomingDTO request,
            StreamObserver<GrpcGroupChatsDTO> responseObserver
    ) {
        final var userDto = grpcUserServiceConnection
                .authorizeUserByAccessToken(request.getToken());
        final var authorizedUser = AccountInfo.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .build();

        List<GroupChat> groupChats =
                groupChatService.getAllGroupChatsForUser(authorizedUser);

        final var dto =
                groupChats
                        .stream()
                        .map(groupChat ->
                                formGroupChatRetrievalDTOForAuthorizedUser(
                                        groupChat, authorizedUser
                                )
                        )
                        .map(this::mapDtoToGrpcDTO)
                        .collect(Collectors.toList());

        final var grpcDto =
                GrpcGroupChatsDTO.newBuilder()
                        .addAllChats(dto)
                        .build();

        responseObserver.onNext(grpcDto);
        responseObserver.onCompleted();
    }

    @Transactional
    public GrpcGroupChatDTO mapDtoToGrpcDTO(GroupChatRetrievalDTO groupChatRetrievalDTO) {
        final var usersIds = groupChatRetrievalDTO.getParticipants()
                .stream()
                .map(user -> user.getId().toString())
                .collect(Collectors.toList());

        return GrpcGroupChatDTO.newBuilder()
                .setId(groupChatRetrievalDTO.getId().toString())
                .setAbout(groupChatRetrievalDTO.getAbout())
                .setCreatorId(groupChatRetrievalDTO.getCreatorId().toString())
                .setTitle(groupChatRetrievalDTO.getTitle())
                .addAllParticipants(usersIds)
                .build();
    }

    @Override
    @Transactional
    public void getChatById(
            GrpcChatRetrievalDTO request,
            StreamObserver<GrpcGroupChatDTO> responseObserver
    ) {
        final var userDto = grpcUserServiceConnection
                .authorizeUserByAccessToken(request.getToken());
        final var authorizedUser = AccountInfo.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .build();

        final var chatId = UUID.fromString(request.getChatId());
        final var groupChat =
                groupChatService.getGroupChatById(chatId, authorizedUser);
        final var chatDto = formGroupChatRetrievalDTOForAuthorizedUser(
                groupChat, authorizedUser);
        final var grpcDto = mapDtoToGrpcDTO(chatDto);

        responseObserver.onNext(grpcDto);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void createGroupChat(
            GrpcGroupChatCreationDTO request,
            StreamObserver<GrpcGroupChatDTO> responseObserver
    ) {
        final var authorizedUserDto = grpcUserServiceConnection
                .authorizeUserByAccessToken(request.getToken());
        final var authorizedUser = AccountInfo.builder()
                .id(authorizedUserDto.getId())
                .name(authorizedUserDto.getName())
                .surname(authorizedUserDto.getSurname())
                .build();

        final var participantsIds = request.getParticipantsList()
                .asByteStringList()
                .stream()
                .map(ByteString::toStringUtf8)
                .map(UUID::fromString)
                .collect(Collectors.toList());

        final var participants =
                grpcUserServiceConnection.getUsersByListOfIds(participantsIds)
                        .stream()
                        .map(dto -> AccountInfo.builder()
                                .id(dto.getId())
                                .name(dto.getName())
                                .surname(dto.getSurname())
                                .build()
                        ).collect(Collectors.toList());

        if (participants.size() < 2) {
            throw new BadRequestException("Chats must have at least two members");
        }

        GroupChat toBeCreated = GroupChat.builder()
                .title(request.getTitle())
                .about(request.getAbout())
                .creatorId(authorizedUserDto.getId())
                .build();

        GroupChat createdChat = groupChatService
                .createGroupChat(toBeCreated, participants, authorizedUser);

        final var chatDto = formGroupChatRetrievalDTOForAuthorizedUser(
                createdChat, authorizedUser);
        final var grpcDto = mapDtoToGrpcDTO(chatDto);

        responseObserver.onNext(grpcDto);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void getAllMessageForChat(
            GrpcChatRetrievalDTO request,
            StreamObserver<GrpcGroupMessagesRetrievalDTO> responseObserver
    ) {
        final var userDto = grpcUserServiceConnection
                .authorizeUserByAccessToken(request.getToken());
        final var authorizedUser = AccountInfo.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .build();

        final var chatId = UUID.fromString(request.getChatId());
        final var groupChat =
                groupChatService.getGroupChatById(chatId, authorizedUser);

        final var messages = groupChatMessageService
                .getAllMessages(groupChat, authorizedUser)
                .stream()
                .map(message ->
                        GrpcGroupMessageRetrievalDTO.newBuilder()
                                .setId(message.getId().toString())
                                .setAuthorId(message.getAuthorId().toString())
                                .setChatId(message.getChatId().toString())
                                .setCreatedAt(message.getCreatedAt().toString())
                                .setContents(message.getContents())
                                .build()
                ).collect(Collectors.toList());
        final var grpcDto =
                GrpcGroupMessagesRetrievalDTO.newBuilder()
                        .addAllMessages(messages)
                        .build();

        responseObserver.onNext(grpcDto);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void createMessage(
            GrpcGroupMessageCreationDTO request,
            StreamObserver<GrpcGroupMessageRetrievalDTO> responseObserver
    ) {
        final var userDto = grpcUserServiceConnection
                .authorizeUserByAccessToken(request.getToken());
        final var authorizedUser = AccountInfo.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .build();

        final var chatId = UUID.fromString(request.getChatId());
        final var groupChat =
                groupChatService.getGroupChatById(chatId, authorizedUser);

        GroupMessage message = GroupMessage.builder()
                .authorId(authorizedUser.getId())
                .chatId(chatId)
                .contents(request.getContents())
                .build();

        GroupMessage savedMessage = groupChatMessageService
                .addMessage(groupChat, message, authorizedUser);

        final var grpcDto = GrpcGroupMessageRetrievalDTO.newBuilder()
                .setAuthorId(savedMessage.getAuthorId().toString())
                .setChatId(savedMessage.getChatId().toString())
                .setCreatedAt(savedMessage.getCreatedAt().toString())
                .setId(savedMessage.getId().toString())
                .setContents(savedMessage.getContents())
                .build();

        responseObserver.onNext(grpcDto);
        responseObserver.onCompleted();
    }

    @Transactional
    public GroupChatRetrievalDTO
    formGroupChatRetrievalDTOForAuthorizedUser(
            GroupChat groupChat,
            AccountInfo author
    ) {
        List<AccountInfo> participants =
                groupChatService.getGroupChatMembers(groupChat, author);
        return GroupChatRetrievalDTO.fromGroupChat(groupChat, participants);
    }
}
