package com.and1ss.group_chat_service.api.dto;

import com.and1ss.group_chat_service.services.model.AccountInfo;
import com.and1ss.group_chat_service.services.model.GroupChat;
import lombok.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatRetrievalDTO {
    @NonNull
    private UUID id;

    @NonNull
    private String title;

    private String about;

    @NonNull
    private UUID creatorId;

    @NonNull
    private List<AccountInfoRetrievalDTO> participants;

    public static GroupChatRetrievalDTO fromGroupChat(
            GroupChat groupChat,
            List<AccountInfo> participants
    ) {
        List<AccountInfoRetrievalDTO> participantsRetrieval =
                participants.stream()
                        .map(AccountInfoRetrievalDTO::fromAccountInfo)
                        .collect(Collectors.toList());

        return GroupChatRetrievalDTO.builder()
                .title(groupChat.getTitle())
                .id(groupChat.getId())
                .about(groupChat.getAbout())
                .creatorId(groupChat.getCreatorId())
                .participants(participantsRetrieval)
                .build();
    }
}

