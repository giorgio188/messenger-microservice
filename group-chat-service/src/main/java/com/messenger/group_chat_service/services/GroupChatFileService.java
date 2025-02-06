package com.messenger.group_chat_service.services;

import com.project.messenger.models.GroupChat;
import com.project.messenger.models.GroupChatFiles;
import com.project.messenger.models.UserProfile;
import com.project.messenger.models.enums.FileType;
import com.project.messenger.repositories.GroupChatFileRepository;
import com.project.messenger.repositories.GroupChatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupChatFileService {

    private final UserProfileService userProfileService;
    private final GroupChatService groupChatService;
    private final GroupChatRepository groupChatRepository;
    private final S3Service s3Service;
    private final String FILE_DIRECTORY = "group-files";
    private final GroupChatFileRepository groupChatFileRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public GroupChatFiles sendFile(int senderId, int groupChatId, MultipartFile file)
            throws IOException {
        UserProfile sender = userProfileService.getUserProfile(senderId);
        GroupChat groupChat = modelMapper.map(groupChatService.getGroupChat(groupChatId, senderId), GroupChat.class);
        String filePath = s3Service.uploadFile(file, FILE_DIRECTORY);
        FileType fileType = FileType.getByContentType(file.getContentType())
                .orElseThrow(() -> new RuntimeException("Неподдерживаемый формат файла: " + file.getContentType()));
        String fileName = file.getOriginalFilename();
        int size = (int) file.getSize();
        GroupChatFiles newFile = new GroupChatFiles(
                groupChat, sender, LocalDateTime.now(),
                fileName, filePath, fileType, size);
        GroupChatFiles savedFile = groupChatFileRepository.save(newFile);

        // Создаем уведомление о новом файле
        Map<String, Object> fileNotification = new HashMap<>();
        fileNotification.put("type", "FILE_UPLOADED");
        fileNotification.put("fileId", savedFile.getId());
        fileNotification.put("fileName", file.getOriginalFilename());
        fileNotification.put("fileType", fileType);
        fileNotification.put("senderId", senderId);
        fileNotification.put("timestamp", LocalDateTime.now());
        fileNotification.put("url", s3Service.getFileUrl(fileName));

        // Отправляем уведомление всем участникам группы
        messagingTemplate.convertAndSend(
                "/topic/group-file/" + groupChatId,
                fileNotification
        );

        return savedFile;
    }

    @Transactional
    public void deleteFile(int fileId){
        GroupChatFiles file = groupChatFileRepository.findById(fileId).orElse(null);
        if(file != null){
            int groupChatId = file.getGroupChat().getId();
            groupChatFileRepository.delete(file);
            s3Service.deleteFile(file.getFileName());
            // Создаем уведомление об удалении файла
            Map<String, Object> deleteNotification = new HashMap<>();
            deleteNotification.put("type", "FILE_DELETED");
            deleteNotification.put("fileId", fileId);
            deleteNotification.put("timestamp", LocalDateTime.now());

            // Отправляем уведомление всем участникам группы
            messagingTemplate.convertAndSend(
                    "/topic/group-file/" + groupChatId,
                    deleteNotification
            );
        }
    }

    public List<GroupChatFiles> getAllFiles(int groupChatId){
        GroupChat groupChat = groupChatRepository.findById(groupChatId).orElse(null);
        List<GroupChatFiles> files = groupChatFileRepository.getAllFilesByGroupChat(groupChat);
        return files;
    }

    public String getFileById(int fileId){
        GroupChatFiles file = groupChatFileRepository.findById(fileId).orElse(null);
        if(file != null){
            return s3Service.getFileUrl(file.getFileName());
        } else throw new EntityNotFoundException("No such file");
    }

}


