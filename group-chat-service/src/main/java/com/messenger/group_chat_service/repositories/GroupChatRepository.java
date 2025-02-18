package com.messenger.group_chat_service.repositories;

import com.messenger.group_chat_service.models.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Integer> {

}
