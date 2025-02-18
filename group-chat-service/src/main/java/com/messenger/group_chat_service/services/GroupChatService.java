package com.messenger.group_chat_service.services;


import com.messenger.group_chat_service.dto.GroupChatDTO;
import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatMembers;
import com.messenger.group_chat_service.models.enums.Roles;
import com.messenger.group_chat_service.repositories.GroupChatMembersRepository;
import com.messenger.group_chat_service.repositories.GroupChatRepository;
import com.messenger.group_chat_service.utils.MapperForDTO;
import com.messenger.group_chat_service.utils.UserInfoUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupChatMembersRepository groupChatMembersRepository;
    private final MapperForDTO mapperForDTO;
    private final String AVATAR_DIRECTORY = "groupchat-avatar";
    private final S3Service s3Service;
    private final UserInfoUtil userInfoUtil;

    @Transactional
    public void createGroupChat(String groupName, String description, int creatorId) {
        GroupChat groupChat = new GroupChat();
        groupChat.setName(groupName);
        groupChat.setDescription(description);
        groupChatRepository.save(groupChat);

        GroupChatMembers creator = new GroupChatMembers(groupChat, creatorId, Roles.valueOf(Roles.CREATOR.toString()));

    }

    public void addUser(int groupChatId, int currentUserId, int userToAddId, Roles role) {
        if (groupChatMembersRepository.existsByIdAndMemberId(groupChatId, currentUserId)
                && userInfoUtil.ifUserExists(userToAddId).block()) {
            GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
            GroupChatMembers member = new GroupChatMembers();
            member.setMemberId(userToAddId);
            member.setGroupChat(groupChat);
            member.setRole(role);
            groupChatMembersRepository.save(member);
        } else {
            throw new EntityNotFoundException("User with id " + currentUserId + " not a member of group chat with id " + groupChatId);
        }


    }

    public GroupChatDTO getGroupChat(int groupChatId, int memberId) throws AccessDeniedException {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new EntityNotFoundException("GroupChat not found"));
        boolean isMember = groupChat.getGroupChatMembers().stream().anyMatch(member -> member.getMemberId() == memberId);
        if (isMember) {
            return mapperForDTO.convertGroupChatToDTO(groupChat);
        } else {
            throw new AccessDeniedException("User is not a participant of this chat");
        }
    }

    @Transactional
    public void editDescription(int groupChatId, String description, int currentUserId) {
        if (isAdmin(groupChatId, currentUserId)) {
            GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
            groupChat.setDescription(description);
            groupChatRepository.save(groupChat);
        }
    }

    @Transactional
    public void editName(int groupChatId, String name, int currentUserId) {
        if (isAdmin(groupChatId, currentUserId)) {
            GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
            groupChat.setName(name);
            groupChatRepository.save(groupChat);
        }
        throw new AccessDeniedException("User is not an admin of this chat");
    }

    @Transactional
    public void deleteUser(int groupChatId, int userToDeleteId, int currentUserId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new EntityNotFoundException("GroupChat not found"));
        boolean isMember = groupChat.getGroupChatMembers().stream().anyMatch(member -> member.getMemberId() == userToDeleteId);
        if (isAdmin(groupChatId, currentUserId) && isMember) {
            GroupChatMembers member = groupChatMembersRepository.findByGroupChatAndMemberId(groupChat, currentUserId);
            groupChatMembersRepository.delete(member);
        } else {
            throw new AccessDeniedException("User is not an admin of this chat");
        }
    }

    public List<GroupChatDTO> getAllGroupChatsByUser(int userId) {
        List<GroupChatMembers> members = groupChatMembersRepository.findByMemberId(userId);
        List<GroupChatDTO> groupChatDTOs = new ArrayList<>();
        for (GroupChatMembers groupChatMember : members) {
            GroupChat groupChat = groupChatMember.getGroupChat();
            GroupChatDTO groupChatDTO = mapperForDTO.convertGroupChatToDTO(groupChat);
            groupChatDTOs.add(groupChatDTO);
        }
        return groupChatDTOs;
    }

    @Transactional
    public void deleteGroupChat(int groupChatId, int userId) {
        if (isCreator(groupChatId, userId)) {
             groupChatRepository.deleteById(groupChatId);
        } else throw new AccessDeniedException("User is not an admin of this chat");
    }

    public List<GroupChatMembers> getAllGroupChatMembersByGroupChat(int groupChatId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
        List<GroupChatMembers> members = groupChatMembersRepository.findAllByGroupChat(groupChat);
        return members;
    }

    @Transactional
    public void leaveGroupChat(int groupChatId, int currentUserId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
        groupChatMembersRepository.deleteByGroupChatAndMemberId(groupChat, currentUserId);
    }

    @Transactional
    public void setRoleToMember(int groupChatId, int memberId, Roles role, int adminId) {
        if (isAdmin(groupChatId, adminId)) {
            GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
            GroupChatMembers memberToChange = groupChatMembersRepository.findByGroupChatAndMemberId(groupChat, memberId);
            memberToChange.setRole(role);
            groupChatMembersRepository.save(memberToChange);
        } else throw new AccessDeniedException("User is not an admin of this chat");
    }

    @Transactional
    public void setAvatar(int userId, int groupChatId, MultipartFile file) {
        if (isAdmin(userId, groupChatId)) {
            GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
            String avatar = s3Service.uploadFile(file, AVATAR_DIRECTORY);
            groupChat.setAvatar(avatar);
            groupChatRepository.save(groupChat);
        } else throw new AccessDeniedException("User is not an admin of this chat");
    }

    @Transactional
    public void deleteAvatar(int userId, int groupChatId) {
        if (isAdmin(groupChatId, userId)) {
            GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
            groupChat.setAvatar(null);
            groupChatRepository.save(groupChat);
        } else throw new AccessDeniedException("User is not an admin of this chat");
    }

    public String getGroupChatAvatar(int groupChatId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
        String avatarName = groupChat.getAvatar();
        return s3Service.getFileUrl(avatarName);
    }



    private boolean isAdmin(int groupChatId, int userId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
        GroupChatMembers admin = groupChatMembersRepository.findByGroupChatAndMemberId(groupChat, userId);
        if (admin.getRole() == Roles.ADMIN || admin.getRole() == Roles.CREATOR) {
            return true;
        } else return false;
    }

    private boolean isCreator(int groupChatId, int userId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId).get();
        GroupChatMembers creator = groupChatMembersRepository.findByGroupChatAndMemberId(groupChat, userId);
        if (creator.getRole() == Roles.CREATOR) {
            return true;
        } else return false;
    }
}

