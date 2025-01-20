package com.messenger.user_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "friend_list")
@NoArgsConstructor
public class FriendList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserProfile userId;

    @ManyToOne
    @JoinColumn(name = "friend_id", referencedColumnName = "id")
    private UserProfile friendId;

    @NotNull
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    public FriendList(UserProfile userId, UserProfile friendId, LocalDateTime addedAt) {
        this.userId = userId;
        this.friendId = friendId;
        this.addedAt = addedAt;
    }
}
