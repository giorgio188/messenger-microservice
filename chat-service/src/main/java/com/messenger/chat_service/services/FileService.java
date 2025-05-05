package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.FileDTO;
import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.models.File;
import com.messenger.chat_service.models.Message;
import com.messenger.chat_service.models.enums.ChatRole;
import com.messenger.chat_service.models.enums.ChatType;
import com.messenger.chat_service.models.enums.FileType;
import com.messenger.chat_service.repositories.ChatRepository;
import com.messenger.chat_service.repositories.FileRepository;
import com.messenger.chat_service.utils.MapperDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final S3Service s3Service;
    private final MapperDTO mapperDTO;
    private final ChatService chatService;

    @Transactional
    public File uploadFileToMessage(int userId, int chatId, MultipartFile file, Message message) {
        try {
            String filePath = s3Service.uploadFile(file, chatService.getChat(chatId, userId).getType().toString() + "-files");
            File fileEntity = new File();
            fileEntity.setChat(message.getChat());
            fileEntity.setSenderId(userId);
            fileEntity.setFileName(file.getOriginalFilename());
            fileEntity.setFilePath(filePath);
            fileEntity.setSize((int) file.getSize());
            fileEntity.setType(FileType.fromContentType(file.getContentType()));
            fileEntity.setDeleted(false);
            fileEntity.setMessage(message);

            return fileRepository.save(fileEntity);
        }   catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<File> uploadFilesToMessage(int userId, int chatId, List<MultipartFile> files, Message message) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<File> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            File uploadedFile = uploadFileToMessage(userId, chatId, file, message);
            uploadedFiles.add(uploadedFile);
        }

        return uploadedFiles;
    }

    public Page<FileDTO> getFilesByChatId(int userId, int chatId, Pageable pageable) {
        chatService.checkChatAccess(chatId, userId);

        Page<File> files = fileRepository.findByChatIdAndIsDeletedFalseOrderByUploadedAtDesc(chatId, pageable);
        return files.map(mapperDTO::toFileDTO);
    }

    public Page<FileDTO> getImagesByChatId(int userId, int chatId, Pageable pageable) {
        chatService.checkChatAccess(chatId, userId);

        Page<File> images = fileRepository.findImagesByChatId(chatId, pageable);
        return images.map(mapperDTO::toFileDTO);
    }

    public Page<FileDTO> getDocumentsByChatId(int userId, int chatId, Pageable pageable) {
        chatService.checkChatAccess(chatId, userId);

        Page<File> documents = fileRepository.findDocumentsByChatId(chatId, pageable);
        return documents.map(mapperDTO::toFileDTO);
    }

    @Transactional
    public void deleteFile(int userId, int fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Chat chat = file.getChat();

        if (file.getSenderId() != userId && chat.getType() == ChatType.GROUP) {
            boolean isAdmin = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == userId)
                    .anyMatch(member -> member.getRole() == ChatRole.ADMIN ||
                            member.getRole() == ChatRole.CREATOR);
            if (!isAdmin) {
                throw new RuntimeException("You are not allowed to delete this file");
            }
        }

        if (file.getSenderId() == userId) {
            file.setDeleted(true);
            fileRepository.save(file);
        } else {
            throw new RuntimeException("You are not allowed to delete this file");
        }

        if (file.getMessage().isDeleted()) {
            s3Service.deleteFile(file.getFilePath());
        }
    }


}
