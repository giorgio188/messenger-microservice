package com.messenger.private_chat_service.models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "private_chat")
@EqualsAndHashCode
public class PrivateChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JoinColumn(name = "sender_id")
    private int senderId;

    @JoinColumn(name = "receiver_id")
    private int receiverId;

    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "privateChat", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<PrivateChatFiles> privateChatFiles;

    @OneToMany(mappedBy = "privateChat", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<PrivateChatMessage> privateChatMessages;

    public PrivateChat(int senderId, int receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
}
