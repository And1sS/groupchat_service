package com.and1ss.group_chat_service.services.impl;

import com.and1ss.group_chat_service.api.connections.UserServiceConnection;
import com.and1ss.group_chat_service.api.dto.AccountInfoRetrievalDTO;
import com.and1ss.group_chat_service.exceptions.BadRequestException;
import com.and1ss.group_chat_service.exceptions.InternalServerException;
import com.and1ss.group_chat_service.exceptions.UnauthorizedException;
import com.and1ss.group_chat_service.services.UserService;
import com.and1ss.group_chat_service.services.model.AccountInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserServiceConnection userServiceConnection;

    @Autowired
    public UserServiceImpl(UserServiceConnection userServiceConnection) {
        this.userServiceConnection = userServiceConnection;
    }

    @Override
    public AccountInfo authorizeUserByAccessToken(String accessToken) {
        AccountInfo accountInfo;
        try {
            AccountInfoRetrievalDTO retrievalDTO = userServiceConnection
                    .authorizeUserByAccessToken(accessToken).block();

            accountInfo = AccountInfo.builder()
                    .id(retrievalDTO.getId())
                    .name(retrievalDTO.getName())
                    .surname(retrievalDTO.getSurname())
                    .build();
        } catch (WebClientResponseException e) {
            switch (e.getRawStatusCode()) {
                case 401:
                case 404:
                    throw new UnauthorizedException("User is not Authorized");

                default:
                    throw new InternalServerException(e.getMessage());
            }
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }

        return accountInfo;
    }

    @Override
    public AccountInfo findUserById(UUID id) {
        AccountInfoRetrievalDTO retrievalDTO;
        try {
            retrievalDTO = userServiceConnection
                    .getUserById(id)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException("Invalid user id");
            }
            throw new InternalServerException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }

        return AccountInfo.builder()
                .id(retrievalDTO.getId())
                .name(retrievalDTO.getName())
                .surname(retrievalDTO.getSurname())
                .build();
    }

    @Override
    public List<AccountInfo> findUsersByListOfIds(List<UUID> ids) {
        AccountInfoRetrievalDTO[] users;
        try {
            users = userServiceConnection.getUsersByListOfIds(ids).block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException("Invalid user id");
            }
            throw new InternalServerException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }

        if (users == null) {
            throw new InternalServerException();
        }

        return Arrays.stream(users).map((user) -> AccountInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .build())
                .collect(Collectors.toList());
    }
}
