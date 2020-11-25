package com.and1ss.group_chat_service.api.grpc;

import com.and1ss.group_chat_service.api.dto.AccountInfoRetrievalDTO;
import com.and1ss.user_service.GrpcAccessTokenIncomingDTO;
import com.and1ss.user_service.GrpcAuthenticationServiceGrpc;
import com.and1ss.user_service.GrpcUserIdDTO;
import com.and1ss.user_service.GrpcUsersIdsDTO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrpcUserServiceConnection {

    private final ManagedChannel channel;

    @Autowired
    public GrpcUserServiceConnection(Environment env) {
        channel = ManagedChannelBuilder
                .forAddress(env.getProperty("grpc_auth_url"), Integer.valueOf(env.getProperty("grpc_auth_port")))
                .usePlaintext()
                .build();
    }

    public AccountInfoRetrievalDTO authorizeUserByAccessToken(String accessToken) {
        GrpcAuthenticationServiceGrpc.GrpcAuthenticationServiceBlockingStub stub =
                GrpcAuthenticationServiceGrpc.newBlockingStub(channel);

        final var dto = GrpcAccessTokenIncomingDTO.newBuilder()
                .setToken(accessToken)
                .build();
        final var response = stub.identifyByToken(dto);

        return AccountInfoRetrievalDTO.builder()
                .id(UUID.fromString(response.getId()))
                .name(response.getName())
                .surname(response.getSurname())
                .build();
    }

    public AccountInfoRetrievalDTO getUserById(UUID id) {
        GrpcAuthenticationServiceGrpc.GrpcAuthenticationServiceBlockingStub stub =
                GrpcAuthenticationServiceGrpc.newBlockingStub(channel);

        final var dto = GrpcUserIdDTO.newBuilder()
                .setUserId(id.toString())
                .build();
        final var response = stub.identifyUserById(dto);

        return AccountInfoRetrievalDTO.builder()
                .id(UUID.fromString(response.getId()))
                .name(response.getName())
                .surname(response.getSurname())
                .build();
    }

    public List<AccountInfoRetrievalDTO> getUsersByListOfIds(List<UUID> ids) {
        GrpcAuthenticationServiceGrpc.GrpcAuthenticationServiceBlockingStub stub =
                GrpcAuthenticationServiceGrpc.newBlockingStub(channel);


        final var dto = GrpcUsersIdsDTO.newBuilder()
                .addAllUsersIds(
                        ids.stream()
                                .map(UUID::toString)
                                .collect(Collectors.toList())
                )
                .build();
        final var response = stub.identifyUsersByIds(dto);
        return response.getUsersList()
                .stream()
                .map(user -> AccountInfoRetrievalDTO.builder()
                        .id(UUID.fromString(user.getId()))
                        .name(user.getName())
                        .surname(user.getSurname())
                        .build()
                ).collect(Collectors.toList());
    }
}

