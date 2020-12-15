package com.and1ss.group_chat_service.api.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageCreationDTO {
    private UUID chatId;

    @NonNull
    private String contents;
}
