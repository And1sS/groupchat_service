package com.and1ss.group_chat_service.services.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class GroupChatUserId implements Serializable {
    @Column(columnDefinition = "group_chat_id")
    private UUID groupChatId;

    @Column(columnDefinition = "user_id")
    private UUID userId;
}
