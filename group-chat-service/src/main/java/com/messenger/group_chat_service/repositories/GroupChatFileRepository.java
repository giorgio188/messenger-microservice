package com.messenger.group_chat_service.repositories;

import com.project.messenger.models.GroupChat;
import com.project.messenger.models.GroupChatFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatFileRepository extends JpaRepository<GroupChatFiles, Integer> {

   List<GroupChatFiles> getAllFilesByGroupChat(GroupChat groupChat);
}
