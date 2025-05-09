package com.messenger.chat_service.controllers;

import com.messenger.chat_service.dto.ChatMemberDTO;
import com.messenger.chat_service.services.ChatMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/chat/{chatId}/members")
@RestController
@RequiredArgsConstructor
public class ChatMemberController {
    private final ChatMemberService chatMemberService;

    @GetMapping
    public ResponseEntity<List<ChatMemberDTO>> getChatMembers(@RequestHeader("X-User-Id") int userId,
                                                              @PathVariable int chatId) {
        List<ChatMemberDTO> chatMembers = chatMemberService.getChatMembers(userId, chatId);
        return ResponseEntity.ok().body(chatMembers);
    }

    @PostMapping
    public ResponseEntity<Void> addMember(@RequestHeader("X-User-Id") int userId,
                                          @PathVariable int chatId, @RequestParam int userToAddId) {
        chatMemberService.addMember(userId, chatId, userToAddId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@RequestHeader("X-User-Id") int userId,
                                             @PathVariable int memberId,
                                             @PathVariable int chatId) {
        chatMemberService.removeMember(userId, chatId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{memberId}/admin")
    public ResponseEntity<Void> setAdmin(@RequestHeader("X-User-Id") int userId,
                                         @PathVariable int chatId,
                                         @PathVariable int memberId) {
        chatMemberService.setAdmin(userId, chatId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{memberId}/admin")
    public ResponseEntity<Void> setMember(@RequestHeader("X-User-Id") int userId,
                                          @PathVariable int chatId,
                                          @PathVariable int memberId) {
        chatMemberService.setMember(userId, chatId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{memberId}/mute")
    public ResponseEntity<Void> muteMember(@RequestHeader("X-User-Id") int userId,
                                           @PathVariable int chatId,
                                           @PathVariable int memberId) {
        chatMemberService.muteMember(userId, chatId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{memberId}/mute")
    public ResponseEntity<Void> unmuteMember(@RequestHeader("X-User-Id") int userId,
                                             @PathVariable int chatId,
                                             @PathVariable int memberId){
        chatMemberService.unmuteMember(userId, chatId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveChat(@RequestHeader("X-User-Id") int userId,
                                          @PathVariable int chatId){
        chatMemberService.leaveChat(userId, chatId);
        return ResponseEntity.noContent().build();
    }

}
