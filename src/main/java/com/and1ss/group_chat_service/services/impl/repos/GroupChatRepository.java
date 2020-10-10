package com.and1ss.group_chat_service.services.impl.repos;

import com.and1ss.group_chat_service.services.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("group_user")
public interface GroupChatRepository extends JpaRepository<GroupChat, UUID> {
    GroupChat findGroupChatById(UUID id);
    List<GroupChat> findAllByIdIn(List<UUID> ids);
}
