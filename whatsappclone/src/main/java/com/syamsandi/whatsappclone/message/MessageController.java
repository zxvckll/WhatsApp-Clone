package com.syamsandi.whatsappclone.message;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveMessage(Authentication authentication, @RequestBody MessageRequest messageRequest) {
         messageService.saveMessage(authentication,messageRequest);
    }

    @PostMapping(value = "/upload-media",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadMedia(Authentication authentication,
            @RequestParam("chat-id") String chatId,
            //todo add parameter from swagger
            @RequestParam("file") MultipartFile file) {
        messageService.uploadMediaMessage(authentication,chatId,file);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setMessageToSeen(Authentication authentication,
                                 @RequestParam("chat-id") String chatId) {
        messageService.setMessageState(authentication,chatId);
    }

    @GetMapping("/chat/{chat-id}")
    public ResponseEntity<List<MessageResponse>> getChatMessage(Authentication authentication,
                                                                @PathVariable("chat-id") String chatId) {
         return ResponseEntity.ok(messageService.findChatMessages(authentication,chatId));
    }


}
