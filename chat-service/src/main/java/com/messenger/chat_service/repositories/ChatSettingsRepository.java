package com.messenger.chat_service.repositories;

import com.messenger.chat_service.models.ChatSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSettingsRepository extends JpaRepository<ChatSettings, Integer> {
    Optional<ChatSettings> findByChatId(int chatId);

}
