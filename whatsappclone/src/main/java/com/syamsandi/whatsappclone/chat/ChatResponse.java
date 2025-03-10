package com.syamsandi.whatsappclone.chat;

import lombok.*;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String id;
    private String name;
    private long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean isOtherUserOnline;
    private String senderId;
    private String receiverId;
}
