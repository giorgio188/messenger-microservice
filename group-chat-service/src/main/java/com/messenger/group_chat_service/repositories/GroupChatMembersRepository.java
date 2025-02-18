package com.messenger.group_chat_service.repositories;


import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatMembersRepository extends JpaRepository<GroupChatMembers, Integer> {
    List<GroupChatMembers> findByMemberId(int memberId);
    void deleteAllByGroupChat(GroupChat groupChat);
    void deleteByGroupChatAndMemberId(GroupChat groupChat, int memberId);
    List<GroupChatMembers> findAllByGroupChat(GroupChat groupChat);

    GroupChatMembers findByGroupChatAndMemberId(GroupChat groupChat, int memberId);

    boolean existsByIdAndMemberId(int groupChatId, int currentUserId);
}
