package com.syamsandi.whatsappclone.message;

import com.syamsandi.whatsappclone.chat.Chat;
import com.syamsandi.whatsappclone.chat.ChatRepository;
import com.syamsandi.whatsappclone.file.FileService;
import com.syamsandi.whatsappclone.file.FileUtils;
import com.syamsandi.whatsappclone.notification.Notification;
import com.syamsandi.whatsappclone.notification.NotificationService;
import com.syamsandi.whatsappclone.notification.NotificationType;
import com.syamsandi.whatsappclone.user.User;
import com.syamsandi.whatsappclone.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final FileService fileService;
    private final NotificationService notificationService;

    @Transactional
    public void saveMessage(Authentication auth, MessageRequest messageRequest) {

        Chat chat = validateAndGetChat(auth,messageRequest.getChatId());
        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setContent(messageRequest.getContent());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);
        messageRepository.save(message);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(messageRequest.getSenderId())
                .receiverId(messageRequest.getReceiverId())
                .content(messageRequest.getContent())
                .messageType(messageRequest.getType())
                .type(NotificationType.MESSAGE)
                .chatName(chat.getChatName(message.getSenderId()))
                .build();

        notificationService.sendNotification(message.getReceiverId(),notification);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> findChatMessages(Authentication auth,String chatId) {
        validateAndGetChat(auth,chatId);
        return messageRepository.getMessageByChatId(chatId).stream().map(messageMapper::toMessageResponse).collect(Collectors.toList());
    }
    @Transactional
    public void setMessageState(Authentication auth, String chatId) {
        Chat chat = validateAndGetChat(auth, chatId);
        final String otherUserId = getOtherUserId(auth,chat);
        final String currentUserId = getCurrentUserId(auth,chat);
        messageRepository.setMessagesSeenByChatId(chatId,MessageState.SEEN,currentUserId);
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(currentUserId)
                .receiverId(otherUserId)
                .type(NotificationType.SEEN)
                .build();

        notificationService.sendNotification(otherUserId,notification);
    }
    @Transactional
    public void uploadMediaMessage(Authentication auth, String chatId, MultipartFile file) {
        Chat chat = validateAndGetChat(auth,chatId);

        final String currentUserId = getCurrentUserId(auth,chat);
        final String otherUserId = getOtherUserId(auth,chat);
        final String filePath = fileService.saveFile(file,currentUserId);

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(currentUserId);
        message.setReceiverId(otherUserId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);
        messageRepository.save(message);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(currentUserId)
                .receiverId(otherUserId)
                .messageType(MessageType.IMAGE)
                .type(NotificationType.IMAGE)
                .media(FileUtils.readFileFromLocation(filePath))
                .build();

        notificationService.sendNotification(otherUserId,notification);

    }





    private String getOtherUserId(Authentication auth,Chat chat) {
        if(chat.getSender().getId().equals(auth.getName())) {
            return chat.getReceiver().getId();
        }
        return chat.getSender().getId();
    }
    private String getCurrentUserId(Authentication auth,Chat chat) {
        if(chat.getSender().getId().equals(auth.getName())) {
            return chat.getSender().getId();
        }
        return chat.getReceiver().getId();
    }
    private Chat validateAndGetChat(Authentication auth, String chatId) {
        String currentUserId = auth.getName();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );
        Chat chat = chatRepository.findById(chatId).orElseThrow(
                () -> new EntityNotFoundException("Chat not found")
        );
        if(!chat.getReceiver().equals(currentUser) && !chat.getSender().equals(currentUser)){
            throw new SecurityException("You do not have permission to access this message");
        }
        return chat;
    }
}
