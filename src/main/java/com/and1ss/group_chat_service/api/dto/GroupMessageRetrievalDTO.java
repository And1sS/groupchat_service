package com.and1ss.group_chat_service.api.dto;

import com.and1ss.group_chat_service.services.UserService;
import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupMessage;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageRetrievalDTO {
    protected UUID id;

    private UUID authorId;

    @NonNull
    private UUID chatId;

    private String contents;

    private Timestamp createdAt;

    public static GroupMessageRetrievalDTO
    fromGroupMessage(GroupMessage groupMessage) {

        return builder()
                .id(groupMessage.getId())
                .authorId(groupMessage.getAuthorId())
                .chatId(groupMessage.getChatId())
                .contents(groupMessage.getContents())
                .createdAt(groupMessage.getCreatedAt())
                .build();
    }
}
