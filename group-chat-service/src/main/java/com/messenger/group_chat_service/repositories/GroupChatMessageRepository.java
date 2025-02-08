package com.messenger.group_chat_service.repositories;


import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatMessage;
import com.messenger.group_chat_service.models.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Integer> {
    List<GroupChatMessage> findByGroupChatOrderBySentAtDesc(GroupChat groupChat);
    List<GroupChatMessage> findByGroupChatAndStatusAndSenderIdNot(
            GroupChat groupChat,
            MessageStatus status,
            int readerId
    );
}
