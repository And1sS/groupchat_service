package com.and1ss.group_chat_service.services;

import com.and1ss.group_chat_service.services.model.AccountInfo;

import java.util.List;
import java.util.UUID;

public interface UserService {
        AccountInfo authorizeUserByAccessToken(String accessToken);
        AccountInfo findUserById(UUID id);
        List<AccountInfo> findUsersByListOfIds(List<UUID> ids);
}
