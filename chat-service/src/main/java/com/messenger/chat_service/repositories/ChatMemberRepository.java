package com.messenger.chat_service.repositories;

import com.messenger.chat_service.models.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
    Optional<ChatMember> findByChatIdAndUserId(int chatId, int userId);
}
