package com.messenger.chat_service.repositories;

import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.models.enums.ChatRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query("SELECT c FROM Chat c " +
            "JOIN c.members m " +
            "WHERE m.userId = :userId " +
            "ORDER BY c.updatedAt DESC")
    List<Chat> findByMembersUserId(@Param("userId") int userId);

    @Query("SELECT c FROM Chat c " +
            "WHERE c.type = 'PRIVATE' " +
            "AND EXISTS (SELECT m FROM ChatMember m WHERE m.chat = c AND m.userId = :user1Id) " +
            "AND EXISTS (SELECT m FROM ChatMember m WHERE m.chat = c AND m.userId = :user2Id)")
    Optional<Chat> findPrivateChat(
            @Param("user1Id") int user1Id,
            @Param("user2Id") int user2Id
    );

    @Query("SELECT COUNT(m) > 0 FROM ChatMember m " +
            "WHERE m.chat.id = :chatId AND m.userId = :userId")
    boolean isMember(
            @Param("chatId") int chatId,
            @Param("userId") int userId
    );

    @Query("SELECT m.role FROM ChatMember m " +
            "WHERE m.chat.id = :chatId AND m.userId = :userId")
    Optional<ChatRole> getMemberRole(
            @Param("chatId") int chatId,
            @Param("userId") int userId
    );
}
