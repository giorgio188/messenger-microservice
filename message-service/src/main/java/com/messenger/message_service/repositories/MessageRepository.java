package com.messenger.message_service.repositories;

import com.messenger.message_service.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Получить сообщения для чата
    Page<Message> findByChatIdOrderBySentAtDesc(int chatId, Pageable pageable);

    // Получить последние сообщения для чата
    List<Message> findTop20ByChatIdOrderBySentAtDesc(int chatId);

    // Получить сообщения после определенной даты
    List<Message> findByChatIdAndSentAtAfterOrderBySentAt(int chatId, LocalDateTime sentAt);

    // Пометить сообщения как прочитанные
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'READ' WHERE m.chatId = :chatId AND m.senderId != :userId AND m.status != 'READ'")
    void markMessagesAsRead(@Param("chatId") int chatId, @Param("userId") int userId);

    // Получить количество непрочитанных сообщений
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatId = :chatId AND m.senderId != :userId AND m.status != 'READ'")
    int countUnreadMessages(@Param("chatId") int chatId, @Param("userId") int userId);

    // Получить сообщения для чата (с загрузкой файлов)
    @Query("SELECT DISTINCT m FROM Message m LEFT JOIN FETCH m.files WHERE m.chatId = :chatId ORDER BY m.sentAt DESC")
    Page<Message> findByChatIdWithFilesOrderBySentAtDesc(@Param("chatId") int chatId, Pageable pageable);
}


