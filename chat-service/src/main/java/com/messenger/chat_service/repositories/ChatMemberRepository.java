package com.messenger.chat_service.repositories;

import com.messenger.chat_service.models.ChatMember;
import com.messenger.chat_service.models.enums.ChatRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Integer> {
    Optional<ChatMember> findByChatIdAndUserId(int chatId, int userId);
    List<ChatMember> findByChatIdAndIsMutedTrue(int chatId);
    List<ChatMember> findByChatIdAndRoleIn(int chatId, List<ChatRole> roles);

}
