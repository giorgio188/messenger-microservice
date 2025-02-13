package com.messenger.private_chat_service.models;

import com.fasterxml.jackson.annotation.*;
import com.messenger.private_chat_service.models.enums.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "private_chat_files")
public class PrivateChatFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "private_chat_id")
    @JsonProperty("privateChatId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private PrivateChat privateChat;

    @JoinColumn(name = "sender_id")
    private int senderId;

    @JoinColumn(name = "receiver_id")
    private int receiverId;

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

    public PrivateChatFiles(PrivateChat privateChat, int senderId, int receiverId, String fileName, String filePath, FileType type, int size) {
        this.privateChat = privateChat;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.type = type;
        this.size = size;
    }
}
