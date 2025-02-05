package com.messenger.private_chat_service.models;

import com.fasterxml.jackson.annotation.*;
import com.messenger.private_chat_service.models.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "private_chat_messages")
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChatMessage {

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    public PrivateChatMessage(PrivateChat privateChat, int senderId, int receiverId, String message, MessageStatus status) {
        this.privateChat = privateChat;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.status = status;
    }
}
