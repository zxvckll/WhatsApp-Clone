package com.syamsandi.whatsappclone.chat;

import com.syamsandi.whatsappclone.common.StringResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<StringResponse> createChat(@RequestParam("sender-id") String senderId,
                                                     @RequestParam("receiver-id") String receiverId) {
        final String chatId = chatService.createChat(senderId, receiverId);
        StringResponse response = StringResponse.builder()
                .response(chatId)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    private ResponseEntity<List<ChatResponse>> getChatsByReceiver(Authentication authentication) {
        return ResponseEntity.ok(chatService.getChatByCurrentUserId(authentication));
    }
}
