package com.messenger.chat_service.repositories;

import com.messenger.chat_service.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT DISTINCT m FROM Message m LEFT JOIN FETCH m.files f WHERE m.chat.id = :chatId ORDER BY m.sentAt DESC")
    List<Message> findTop10ByChatIdWithFilesOrderBySentAtDesc(@Param("chatId") int chatId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'READ' WHERE m.chat.id = :chatId AND m.senderId != :userId AND m.status != 'read'")
    void markMessagesAsRead(@Param("chatId") int chatId, @Param("userId") int userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.senderId != :userId AND m.status != 'read'")
    int countUnreadMessages(@Param("chatId") int chatId, @Param("userId") int userId);

    @Query("SELECT DISTINCT m FROM Message m LEFT JOIN FETCH m.files WHERE m.chat.id = :chatId ORDER BY m.sentAt DESC")
    Page<Message> findByChatIdWithFilesOrderBySentAtDesc(@Param("chatId") int chatId, Pageable pageable);
}


