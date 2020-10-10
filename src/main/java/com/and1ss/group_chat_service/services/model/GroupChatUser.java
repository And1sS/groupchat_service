package com.and1ss.group_chat_service.services.model;

import lombok.*;

import javax.persistence.*;

@Data
@Table(name = "group_user")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatUser {
    public enum MemberType { read, readwrite, admin }

    @EmbeddedId
    GroupChatUserId id;

    @NonNull
    @Column(name = "member_type")
    @Enumerated(EnumType.STRING)
    private MemberType memberType;
}
