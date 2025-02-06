package com.messenger.private_chat_service.repositories;


import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.models.PrivateChatMessage;
import com.messenger.private_chat_service.models.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateChatMessageRepository extends JpaRepository<PrivateChatMessage, Integer> {
    List<PrivateChatMessage> findByPrivateChatOrderBySentAtDesc(PrivateChat privateChat);
    List<PrivateChatMessage> findByPrivateChatAndStatusAndReceiverIdAndSenderIdNot(
            PrivateChat privateChat,
            MessageStatus status,
            int receiverId,
            int senderId
    );
}
