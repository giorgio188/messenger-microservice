package com.messenger.private_chat_service.models;

import com.fasterxml.jackson.annotation.*;
import com.project.messenger.models.enums.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @JsonProperty("senderId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private UserProfile sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    @JsonProperty("receiverId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private UserProfile receiver;

    @Column(name = "sent_at")
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

    public PrivateChatFiles(PrivateChat privateChat, UserProfile sender, UserProfile receiver,
                            LocalDateTime sentAt, String fileName, String filePath,
                            FileType type, int size) {
        this.privateChat = privateChat;
        this.sender = sender;
        this.receiver = receiver;
        this.sentAt = sentAt;
        this.fileName = fileName;
        this.filePath = filePath;
        this.type = type;
        this.size = size;
    }
}
