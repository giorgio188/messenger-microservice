package com.messenger.message_service.repositories;

import com.messenger.message_service.models.File;
import com.messenger.message_service.models.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    // Получить все файлы для чата
    Page<File> findByChatIdAndIsDeletedFalseOrderByUploadedAtDesc(int chatId, Pageable pageable);

    // Получить файлы по типу
    Page<File> findByChatIdAndTypeAndIsDeletedFalseOrderByUploadedAtDesc(int chatId, FileType type, Pageable pageable);

    // Получить файлы, связанные с сообщением
    List<File> findByMessageIdAndIsDeletedFalse(int messageId);

    // Проверить, является ли пользователь отправителем файла
    boolean existsByIdAndSenderId(int fileId, int senderId);

    // Найти все изображения в чате
    @Query("SELECT f FROM File f WHERE f.chatId = :chatId AND f.isDeleted = false AND " +
            "(f.type = 'JPEG' OR f.type = 'PNG' OR f.type = 'GIF') ORDER BY f.uploadedAt DESC")
    Page<File> findImagesByChatId(@Param("chatId") int chatId, Pageable pageable);

    // Найти все документы в чате (не изображения)
    @Query("SELECT f FROM File f WHERE f.chatId = :chatId AND f.isDeleted = false AND " +
            "f.type NOT IN ('JPEG', 'PNG', 'GIF') ORDER BY f.uploadedAt DESC")
    Page<File> findDocumentsByChatId(@Param("chatId") int chatId, Pageable pageable);
}

