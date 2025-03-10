package com.syamsandi.whatsappclone.chat;

import com.syamsandi.whatsappclone.user.UserConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query(name = ChatConstants.FIND_CHAT_BY_CURRENT_USER_ID)
    List<Chat> findChatByCurrentUserId(@Param("currentUserId") String currentUserId);

    @Query(name = ChatConstants.FIND_CHAT_BY_CURRENT_USER_ID_AND_OTHER_USER_ID)
    Optional<Chat> findChatByCurrentUserIdAndOtherUserId(@Param("currentUserId") String currentUserId, @Param("otherUserId") String otherUserId);
}
