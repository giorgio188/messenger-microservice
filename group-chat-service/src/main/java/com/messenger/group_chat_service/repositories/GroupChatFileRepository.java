package com.messenger.group_chat_service.repositories;


import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatFileRepository extends JpaRepository<GroupChatFiles, Integer> {

   List<GroupChatFiles> getAllFilesByGroupChat(GroupChat groupChat);
}
