package com.syamsandi.whatsappclone.message;

import com.syamsandi.whatsappclone.chat.Chat;
import com.syamsandi.whatsappclone.chat.ChatRepository;
import com.syamsandi.whatsappclone.file.FileService;
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
        // todo notification
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> findChatMessages(Authentication auth,String chatId) {
        validateAndGetChat(auth,chatId);
        return messageRepository.getMessageByChatId(chatId).stream().map(messageMapper::toMessageResponse).collect(Collectors.toList());
    }
    @Transactional
    public void setMessageState(Authentication auth, String chatId) {
        validateAndGetChat(auth,chatId);
        messageRepository.setMessagesSeenByChatId(chatId,MessageState.SEEN.toString());
        //todo nootification
    }
    @Transactional
    public void uploadMediaMessage(Authentication auth, String chatId, MultipartFile file) {
        Chat chat = validateAndGetChat(auth,chatId);

        final String senderId = getCurrentUserId(auth,chat);
        final String receiverId = getOtherUserId(auth,chat);
        final String filePath = fileService.saveFile(file,senderId);

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);
        messageRepository.save(message);

        //todo notification

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
