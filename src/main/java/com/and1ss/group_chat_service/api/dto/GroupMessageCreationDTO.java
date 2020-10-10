package com.and1ss.group_chat_service.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageCreationDTO {
    @NonNull
    private String contents;
}
