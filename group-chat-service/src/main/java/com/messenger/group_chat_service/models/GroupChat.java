package com.messenger.group_chat_service.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "group_chat")
@RequiredArgsConstructor
public class GroupChat{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "name")
    @Size(min = 3, max = 64)
    private String name;

    @NotNull
    @Column(name = "description")
    @Size(max = 256)
    private String description;

    @Column(name = "created_at")
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


    @Column(name = "avatar")
    private String avatar;

    @OneToMany(mappedBy = "groupChat", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<GroupChatMessage> groupChatMessages;

    @OneToMany(mappedBy = "groupChat", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<GroupChatFiles> groupChatFiles;

    @OneToMany(mappedBy = "groupChat")
    @JsonBackReference
    private List<GroupChatMembers> groupChatMembers;
}
