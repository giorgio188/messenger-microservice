package com.messenger.message_service.services;

import com.messenger.message_service.dto.MessageDTO;
import com.messenger.message_service.models.Message;
import com.messenger.message_service.repositories.FileRepository;
import com.messenger.message_service.repositories.MessageRepository;
import com.messenger.message_service.utils.ChatAccessUtil;
import com.messenger.message_service.utils.MapperDTO;
import com.messenger.message_service.utils.UserInfoUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatAccessUtil chatAccessUtil;
    private final MessageRepository messageRepository;
    private final MapperDTO mapperDTO;

    public Page<MessageDTO> getChatMessages(int chatId, int userId, Pageable pageable) {
        chatAccessUtil.hasUserAccessToChat(chatId, userId);
        Page<Message> messages = messageRepository.findByChatIdWithFilesOrderBySentAtDesc(chatId, pageable);
        return messages.map(mapperDTO::toMessageDTO);
    }


    @Transactional
    public MessageDTO sendTextMessage(int chatId, int userId, String content){
        chatAccessUtil.hasUserAccessToChat(chatId, userId);

        Chat chat
    }

}
