package com.messenger.message_service.utils;

import com.messenger.message_service.dto.FileDTO;
import com.messenger.message_service.dto.MessageDTO;
import com.messenger.message_service.dto.UserProfileDTO;
import com.messenger.message_service.models.File;
import com.messenger.message_service.models.Message;
import com.messenger.message_service.services.EncryptionService;
import com.messenger.message_service.services.S3Service;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapperDTO {

    private final ModelMapper modelMapper;
    private final S3Service s3Service;
    private final UserInfoUtil userInfoUtil;
    private final EncryptionService encryptionService;

    public MessageDTO toMessageDTO(Message message) {
        MessageDTO dto = modelMapper.map(message, MessageDTO.class);

        if (message.getContent() != null && !message.getContent().isEmpty()) {
            dto.setContent(encryptionService.decrypt(message.getContent()));
        }

        try {
            UserProfileDTO sender = userInfoUtil.getUserProfile(message.getSenderId()).block();
            if (sender != null) {
                dto.setSenderUsername(sender.getUsername());
                dto.setSenderNickname(sender.getNickname());
                dto.setSenderAvatar(sender.getAvatar());
            }
        } catch (Exception e) {
            dto.setSenderUsername("Unknown user");
        }

        return dto;
    }

    public FileDTO toFileDTO(File file) {

        FileDTO dto = modelMapper.map(file, FileDTO.class);

        dto.setFileUrl(s3Service.getFileUrl(file.getFilePath()));

        try {
            UserProfileDTO sender = userInfoUtil.getUserProfile(file.getSenderId()).block();
            if (sender != null) {
                dto.setSenderUsername(sender.getUsername());
                dto.setSenderNickname(sender.getNickname());
            }
        } catch (Exception e) {
            dto.setSenderUsername("Unknown user");
        }
        if (file.getMessage() != null) {
            dto.setMessageId(file.getMessage().getId());
        }

        return dto;
    }
}
