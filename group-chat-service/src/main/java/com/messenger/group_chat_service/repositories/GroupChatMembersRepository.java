package com.messenger.group_chat_service.repositories;

import com.project.messenger.models.GroupChat;
import com.project.messenger.models.GroupChatMembers;
import com.project.messenger.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatMembersRepository extends JpaRepository<GroupChatMembers, Integer> {
    GroupChatMembers findByGroupChatAndMember(GroupChat groupChatId, UserProfile member);
    List<GroupChatMembers> findByMember(UserProfile member);
    void deleteAllByGroupChat(GroupChat groupChat);
    void deleteByGroupChatAndMember(GroupChat groupChat, UserProfile member);
    List<GroupChatMembers> findAllByGroupChat(GroupChat groupChat);
}
