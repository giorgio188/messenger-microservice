package com.messenger.chat_service.repositories;


import com.messenger.chat_service.models.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    Page<File> findByChatIdAndIsDeletedFalseOrderByUploadedAtDesc(int chatId, Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.chat.id = :chatId AND f.isDeleted = false AND " +
            "(f.type = 'JPEG' OR f.type = 'PNG' OR f.type = 'GIF') ORDER BY f.uploadedAt DESC")
    Page<File> findImagesByChatId(@Param("chatId") int chatId, Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.chat.id = :chatId AND f.isDeleted = false AND " +
            "f.type NOT IN ('JPEG', 'PNG', 'GIF') ORDER BY f.uploadedAt DESC")
    Page<File> findDocumentsByChatId(@Param("chatId") int chatId, Pageable pageable);
}

