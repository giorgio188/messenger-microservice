package com.messenger.private_chat_service.repositories;


import com.messenger.private_chat_service.models.PrivateChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateChatRepository extends JpaRepository<PrivateChat, Integer> {
    Optional<PrivateChat> findPrivateChatById(int id);
    PrivateChat findPrivateChatBySenderAndReceiver(int senderId, int receiverId);
    List<PrivateChat> findPrivateChatBySender(int senderId);
    List<PrivateChat> findPrivateChatByReceiver(int receiverId);
}
