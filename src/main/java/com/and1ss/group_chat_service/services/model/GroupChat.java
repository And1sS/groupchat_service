package com.and1ss.group_chat_service.services.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "group_chat")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
public class GroupChat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @NonNull
    private String title;

    private String about;

    @NonNull
    private UUID creatorId;
}