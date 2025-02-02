package com.messenger.private_chat_service.models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private UserProfile sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private UserProfile receiver;

    @Column(name = "created_at")
    @JsonBackReference
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "privateChat", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<PrivateChatFiles> privateChatFiles;

    @OneToMany(mappedBy = "privateChat", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<PrivateChatMessage> privateChatMessages;

    public PrivateChat(UserProfile sender, UserProfile receiver, LocalDateTime createdAt) {
        this.sender = sender;
        this.receiver = receiver;
        this.createdAt = createdAt;
    }
}
