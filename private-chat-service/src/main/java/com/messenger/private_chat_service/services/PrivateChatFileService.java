package com.messenger.private_chat_service.services;


import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.models.PrivateChatFiles;
import com.messenger.private_chat_service.models.enums.FileType;
import com.messenger.private_chat_service.repositories.PrivateChatFileRepository;
import com.messenger.private_chat_service.repositories.PrivateChatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateChatFileService {

    private final S3Service s3Service;
    private final PrivateChatFileRepository privateChatFileRepository;
    private final PrivateChatRepository privateChatRepository;
    private static final String FILE_DIRECTORY = "private-files";
    private final SimpMessagingTemplate messagingTemplate;



    public List<PrivateChatFiles> getPrivateChatFiles(int privateChatId) {
        PrivateChat privateChat = privateChatRepository.findById(privateChatId).orElse(null);
        List<PrivateChatFiles> privateChatFiles = privateChatFileRepository.findByPrivateChatOrderBySentAtDesc(privateChat);
        return privateChatFiles;
    }

    @Transactional
    public PrivateChatFiles sendPrivateChatFile(int senderId, int privateChatId, MultipartFile file) {
        PrivateChat privateChat = privateChatRepository.findById(privateChatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));
        int receiverId = privateChat.getSenderId() == senderId
                ? privateChat.getReceiverId()
                : privateChat.getSenderId();
        FileType fileType = FileType.getByContentType(file.getContentType())
                .orElseThrow(() -> new RuntimeException("Неподдерживаемый формат файла: " + file.getContentType()));
        String fileName = file.getOriginalFilename();
        int size = (int) file.getSize();
        String filePath = s3Service.uploadFile(file, FILE_DIRECTORY);
        PrivateChatFiles privateChatFiles = new PrivateChatFiles(privateChat, senderId, receiverId, fileName, filePath, fileType, size);
        FileType.getByContentType(file.getContentType());
        PrivateChatFiles savedFile = privateChatFileRepository.save(privateChatFiles);

        // Создаем уведомление о новом файле
        Map<String, Object> fileNotification = new HashMap<>();
        fileNotification.put("type", "FILE_UPLOADED");
        fileNotification.put("fileId", savedFile.getId());
        fileNotification.put("fileName", file.getOriginalFilename());
        fileNotification.put("fileType", fileType);
        fileNotification.put("senderId", senderId);
        fileNotification.put("timestamp", LocalDateTime.now());
        fileNotification.put("url", s3Service.getFileUrl(filePath));

        // Отправляем уведомление отправителю
        messagingTemplate.convertAndSendToUser(
                String.valueOf(senderId),
                "/queue/private-file/" + privateChatId,
                fileNotification
        );

        // Отправляем уведомление получателю
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/queue/private-file/" + privateChatId,
                fileNotification
        );

        return savedFile;
    }

    @Transactional
    public void deletePrivateChatFile(int fileId) {
        Optional<PrivateChatFiles> privateChatFile = privateChatFileRepository.findById(fileId);
        if (privateChatFile.isPresent()) {
            PrivateChatFiles file = privateChatFile.get();
            String fileName = privateChatFile.get().getFileName();
            s3Service.deleteFile(fileName);
            privateChatFileRepository.delete(privateChatFile.get());
            // Создаем уведомление об удалении файла
            Map<String, Object> deleteNotification = new HashMap<>();
            deleteNotification.put("type", "FILE_DELETED");
            deleteNotification.put("fileId", fileId);
            deleteNotification.put("timestamp", LocalDateTime.now());

            // Отправляем уведомление обоим участникам чата
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(file.getSenderId()),
                    "/queue/private-file/" + file.getPrivateChat().getId(),
                    deleteNotification
            );

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(file.getReceiverId()),
                    "/queue/private-file/" + file.getPrivateChat().getId(),
                    deleteNotification
            );

            privateChatFileRepository.delete(file);
        } else throw new EntityNotFoundException("File with id " + fileId + " not found");
    }

    public String getFileLinkById(int fileId){
        PrivateChatFiles file = privateChatFileRepository.findById(fileId).orElse(null);
        if(file != null){
            return s3Service.getFileUrl(file.getFileName());
        } else throw new EntityNotFoundException("No such file");
    }

    public PrivateChatFiles getFileById(int fileId){
        return privateChatFileRepository.findById(fileId).orElse(null);
    }

}
