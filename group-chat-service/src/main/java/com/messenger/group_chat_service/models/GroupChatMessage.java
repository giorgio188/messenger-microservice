package com.messenger.group_chat_service.models;


import com.fasterxml.jackson.annotation.*;
import com.messenger.group_chat_service.models.enums.MessageStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "group_chat_messages")
@NoArgsConstructor
public class GroupChatMessage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_chat_id", referencedColumnName = "id")
    @JsonProperty("groupChatId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private GroupChat groupChat;

    @Column(name = "sender_id")
    private int senderId;

    @NotNull
    @Column(name = "sent_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @NotNull
    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    public GroupChatMessage(GroupChat groupChat, int senderId, String message, MessageStatus status) {
        this.groupChat = groupChat;
        this.senderId = senderId;
        this.message = message;
        this.status = status;
    }
}
