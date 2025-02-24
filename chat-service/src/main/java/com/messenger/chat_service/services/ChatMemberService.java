package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.ChatMemberDTO;
import com.messenger.chat_service.exceptions.ChatAccessDeniedException;
import com.messenger.chat_service.exceptions.ChatNotFoundException;
import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.repositories.ChatMemberRepository;
import com.messenger.chat_service.repositories.ChatRepository;
import com.messenger.chat_service.utils.MapperDTO;
import com.messenger.chat_service.utils.UserInfoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;
    private final UserInfoUtil userInfoUtil;
    private final ChatRepository chatRepository;
    private final MapperDTO mapperDTO;

    public List<ChatMemberDTO> getChatMembers(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        boolean isMember = chat.getMembers().stream()
                .anyMatch(member -> member.getUserId() == userId);
        if (!isMember) {
            throw new ChatAccessDeniedException("Access denied. User is not a member of chat");

        }

        return chat.getMembers().stream()
                .map(mapperDTO::toChatMemberDTO)
                .collect(Collectors.toList());
    }

    public void addMember(int chatId, int currentUserId, int userToAddId) {

    }

    public void removeMember(int chatId, int currentUserId, int userToRemoveId) {

    }

    public void leaveChat(int chatId, int userId) {

    }


    public void setAdmin(int chatId, int currentUserId, int userToSetAdmin) {

    }

    public void setMember(int chatId, int currentUserId, int userToSetMember) {

    }

    public void muteMember(int chatId, int currentUserId, int userToMute) {

    }

    public void unmuteMember(int chatId, int currentUserId, int userToUnmute) {

    }


}
