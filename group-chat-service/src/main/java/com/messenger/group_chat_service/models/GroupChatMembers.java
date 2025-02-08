package com.messenger.group_chat_service.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.messenger.group_chat_service.models.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "group_chat_members")
@RequiredArgsConstructor
public class GroupChatMembers {

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

    @JoinColumn(name = "member_id")
    private int memberId;

    @NotNull
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Roles role;

    public GroupChatMembers(GroupChat groupChat, int memberId, Roles role) {
        this.groupChat = groupChat;
        this.memberId = memberId;
        this.role = role;
    }
}
