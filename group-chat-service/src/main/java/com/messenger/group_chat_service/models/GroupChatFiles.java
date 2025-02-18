package com.messenger.group_chat_service.models;

import com.fasterxml.jackson.annotation.*;
import com.messenger.group_chat_service.models.enums.FileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "group_chat_files")
public class GroupChatFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_chat_id", referencedColumnName = "id")
    @JsonProperty("groupChatId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private GroupChat groupChat;


    @Column(name = "sender_id")
    private int senderId;

    @Column(name = "sent_at")
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10)
    private FileType type;

    @Column(name = "size")
    private int size;

    public GroupChatFiles(GroupChat groupChat, int senderId, String fileName, String filePath, FileType type, int size) {
        this.groupChat = groupChat;
        this.senderId = senderId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.type = type;
        this.size = size;
    }
}
