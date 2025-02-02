package com.messenger.private_chat_service.repositories;


import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.models.PrivateChatFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateChatFileRepository extends JpaRepository<PrivateChatFiles, Integer> {
    PrivateChatFiles findByPrivateChatId(int privateChatId);
    List<PrivateChatFiles> findByPrivateChatOrderBySentAtDesc(PrivateChat privateChat);
}
