package com.syamsandi.whatsappclone.chat;

import org.springframework.stereotype.Service;

@Service
public class ChatMapper {

    public ChatResponse toChatResponse(Chat chat, String currentUserId) {
        boolean isOtherUserOnline = false;

        if(chat.getSender().getId().equals(currentUserId)){
            isOtherUserOnline = chat.getReceiver().isUserOnline();
        }
        else {
            isOtherUserOnline = chat.getSender().isUserOnline();
        }

        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getChatName(currentUserId))
                .unreadCount(chat.getUnreadMessages(currentUserId))
                .lastMessage(chat.getLastMessage())
                .lastMessageTime(chat.getLastMessageTime())
                .isOtherUserOnline(isOtherUserOnline)
                .senderId(chat.getSender().getId())
                .receiverId(chat.getReceiver().getId())
                .build();
    }
}
