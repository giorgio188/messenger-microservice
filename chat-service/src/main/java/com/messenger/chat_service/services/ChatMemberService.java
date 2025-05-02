package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.ChatMemberDTO;
import com.messenger.chat_service.exceptions.ChatAccessDeniedException;
import com.messenger.chat_service.exceptions.ChatNotFoundException;
import com.messenger.chat_service.exceptions.ChatValidationException;
import com.messenger.chat_service.exceptions.UserNotFoundException;
import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.models.ChatMember;
import com.messenger.chat_service.models.enums.ChatRole;
import com.messenger.chat_service.models.enums.ChatType;
import com.messenger.chat_service.repositories.ChatMemberRepository;
import com.messenger.chat_service.repositories.ChatRepository;
import com.messenger.chat_service.utils.MapperDTO;
import com.messenger.chat_service.utils.UserInfoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void addMember(int chatId, int currentUserId, int userToAddId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        validateGroupChat(chat);

        if (!userInfoUtil.ifUserExists(userToAddId).block()) {
            throw new UserNotFoundException("User to add not found");
        }

        if (chat.getSettings().isOnlyAdminsCanAddMembers()) {
            validateAdminRights(chat, currentUserId);
        } else {
            boolean isMember = chat.getMembers().stream()
                    .anyMatch(member -> member.getUserId() == currentUserId);
            if (!isMember) {
                throw new ChatAccessDeniedException("User is not a member of this chat");
            }
        }

        boolean isAlreadyMember = chat.getMembers().stream()
                .anyMatch(member -> member.getUserId() == userToAddId);
        if (isAlreadyMember) {
            throw new ChatValidationException("User is already a member of this chat");
        }

        ChatMember newMember = mapperDTO.toChatMember(chat, userToAddId, ChatRole.MEMBER);
        chat.getMembers().add(newMember);
        chatRepository.save(chat);
    }

    @Transactional
    public void removeMember(int chatId, int currentUserId, int userToRemoveId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        validateGroupChat(chat);

        if (!userInfoUtil.ifUserExists(userToRemoveId).block()) {
            throw new UserNotFoundException("User to remove not found");
        }

        if (chat.getSettings().isOnlyAdminsCanRemoveMembers()) {
            validateAdminRights(chat, currentUserId);
        } else {
            if (currentUserId != userToRemoveId) {
                validateAdminRights(chat, currentUserId);
            }
        }

        ChatMember memberToRemove = chat.getMembers().stream()
                .filter(member -> member.getUserId() == userToRemoveId)
                .findFirst()
                .orElseThrow(() -> new ChatValidationException("User is not a member of this chat"));

        if (memberToRemove.getRole() == ChatRole.CREATOR) {
            throw new ChatValidationException("Cannot remove the creator of the chat");
        }

        chat.getMembers().remove(memberToRemove);
        chatMemberRepository.delete(memberToRemove);
        chatRepository.save(chat);
    }

    @Transactional
    public void leaveChat(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        if (!userInfoUtil.ifUserExists(userId).block()) {
            throw new UserNotFoundException("User not found");
        }

        ChatMember member = chat.getMembers().stream()
                .filter(m -> m.getUserId() == userId)
                .findFirst()
                .orElseThrow(() -> new ChatValidationException("User is not a member of this chat"));

        if (chat.getType() == ChatType.PRIVATE) {
            chatRepository.delete(chat);
            return;
        }

        if (member.getRole() == ChatRole.CREATOR) {
            throw new ChatValidationException("The creator cannot leave the chat");
        }

        chat.getMembers().remove(member);
        chatMemberRepository.delete(member);

        if (chat.getMembers().size() <= 1) {
            chatRepository.delete(chat);
        } else {
            chatRepository.save(chat);
        }
    }

    @Transactional
    public void setAdmin(int chatId, int currentUserId, int userToSetAdmin) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        validateGroupChat(chat);
        validateCreatorRights(chat, currentUserId);

        if (!userInfoUtil.ifUserExists(userToSetAdmin).block()) {
            throw new UserNotFoundException("User to promote not found");
        }

        ChatMember member = chat.getMembers().stream()
                .filter(m -> m.getUserId() == userToSetAdmin)
                .findFirst()
                .orElseThrow(() -> new ChatValidationException("User is not a member of this chat"));

        if (member.getRole() == ChatRole.ADMIN) {
            return;
        }

        if (member.getRole() == ChatRole.CREATOR) {
            throw new ChatValidationException("Cannot change the role of the creator");
        }

        member.setRole(ChatRole.ADMIN);
        chatMemberRepository.save(member);
    }

    @Transactional
    public void setMember(int chatId, int currentUserId, int userToSetMember) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        validateGroupChat(chat);
        validateCreatorRights(chat, currentUserId);

        if (!userInfoUtil.ifUserExists(userToSetMember).block()) {
            throw new UserNotFoundException("User to demote not found");
        }

        ChatMember member = chat.getMembers().stream()
                .filter(m -> m.getUserId() == userToSetMember)
                .findFirst()
                .orElseThrow(() -> new ChatValidationException("User is not a member of this chat"));

        if (member.getRole() != ChatRole.ADMIN) {
            return;
        }

        member.setRole(ChatRole.MEMBER);
        chatMemberRepository.save(member);
    }

    @Transactional
    public void muteMember(int chatId, int currentUserId, int userToMute) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        if (!userInfoUtil.ifUserExists(userToMute).block()) {
            throw new UserNotFoundException("User to mute not found");
        }

        if (currentUserId != userToMute) {
            validateAdminRights(chat, currentUserId);
        }

        ChatMember member = chat.getMembers().stream()
                .filter(m -> m.getUserId() == userToMute)
                .findFirst()
                .orElseThrow(() -> new ChatValidationException("User is not a member of this chat"));

        if (member.getRole() == ChatRole.CREATOR && currentUserId != userToMute) {
            throw new ChatValidationException("Cannot mute the creator of the chat");
        }

        member.setMuted(true);
        chatMemberRepository.save(member);
    }

    @Transactional
    public void unmuteMember(int chatId, int currentUserId, int userToUnmute) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        if (!userInfoUtil.ifUserExists(userToUnmute).block()) {
            throw new UserNotFoundException("User to unmute not found");
        }

        if (currentUserId != userToUnmute) {
            validateAdminRights(chat, currentUserId);
        }

        ChatMember member = chat.getMembers().stream()
                .filter(m -> m.getUserId() == userToUnmute)
                .findFirst()
                .orElseThrow(() -> new ChatValidationException("User is not a member of this chat"));

        member.setMuted(false);
        chatMemberRepository.save(member);
    }

    public List<Integer> getMutedMemberIds(int chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
        validateGroupChat(chat);
        List<ChatMember> mutedMembers = chatMemberRepository.findByChatIdAndIsMutedTrue(chatId);

        return mutedMembers.stream()
                .map(ChatMember::getUserId)
                .collect(Collectors.toList());
    }

    public List<Integer> getAdminMemberIds(int chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
        validateGroupChat(chat);
        List<ChatMember> adminMembers = chatMemberRepository.findByChatIdAndRoleIn(
                chatId,
                List.of(ChatRole.ADMIN, ChatRole.CREATOR)
        );

        return adminMembers.stream()
                .map(ChatMember::getUserId)
                .collect(Collectors.toList());
    }

    private void validateCreatorRights(Chat chat, int userId) {
        boolean isCreator = chat.getMembers().stream()
                .filter(member -> member.getUserId() == userId)
                .anyMatch(member -> member.getRole() == ChatRole.CREATOR);
        if (!isCreator) {
            throw new AccessDeniedException("Only creator can perform this operation");
        }
    }

    private void validateGroupChat(Chat chat) {
        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalStateException("This operation is only available for group chats");
        }
    }

    private void validateAdminRights(Chat chat, int userId) {
        boolean isAdmin = chat.getMembers().stream()
                .filter(member -> member.getUserId() == userId)
                .anyMatch(member -> member.getRole() == ChatRole.ADMIN || member.getRole() == ChatRole.CREATOR);
        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can perform this operation");
        }
    }




}
