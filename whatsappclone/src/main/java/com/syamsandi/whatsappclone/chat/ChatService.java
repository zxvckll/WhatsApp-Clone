package com.syamsandi.whatsappclone.chat;

import com.syamsandi.whatsappclone.user.User;
import com.syamsandi.whatsappclone.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.CharsetMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatByCurrentUserId(Authentication currentUser){
        final String currentUserId = currentUser.getName();
        return chatRepository.findChatByCurrentUserId(currentUserId)
                .stream()
                .map(c -> chatMapper.toChatResponse(c,currentUserId))
                .toList();
    }

    public String createChat(String currentUserId, String otherUserId){
        Optional<Chat> existingChat = chatRepository.findChatByCurrentUserIdAndOtherUserId(currentUserId, otherUserId);
        if(existingChat.isPresent()){
            return existingChat.get().getId();
        }
        User currentUser = userRepository.findUserByPublicId(currentUserId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + currentUserId + " not found")
        );

        User otherUser = userRepository.findUserByPublicId(otherUserId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + currentUserId + " not found")
        );
        Chat chat = new Chat();

        chat.setSender(currentUser);
        chat.setReceiver(otherUser);
        Chat savedChat = chatRepository.save(chat);
        return savedChat.getId();

    }
}
