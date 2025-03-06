package com.syamsandi.whatsappclone.chat;

import com.syamsandi.whatsappclone.common.BaseAuditingEntity;
import com.syamsandi.whatsappclone.message.Message;
import com.syamsandi.whatsappclone.message.MessageState;
import com.syamsandi.whatsappclone.message.MessageType;
import com.syamsandi.whatsappclone.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.GenerationType.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
public class Chat extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    @OrderBy("createdDate DESC")
    private List<Message> messages;

    @Transient
    public String getChatName(final String currUserId){
        if(receiver.getId().equals(currUserId)){
            return sender.getFirstName() + " " + sender.getLastName();
        }
        return receiver.getFirstName() + " " + receiver.getLastName();
    }

    @Transient
    public Long getUnreadMessages(final String currUserId){
        return messages
                .stream()
                .filter(m -> m.getReceiverId().equals(currUserId))
                .filter(m -> m.getState().equals(MessageState.SENT))
                .count();
    }
    @Transient
    public String getLastMessage() {
        if(messages!=null && !messages.isEmpty()){
             if(!messages.get(0).getType().equals(MessageType.TEXT)){
                 return "Attachment";
             }
             return messages.get(0).getContent();
        }
        return null;
    }
    @Transient
    public LocalDateTime getLastMessageTime(){
        if(messages!=null && !messages.isEmpty()){
            return messages.get(0).getCreatedDate();
        }
        return null;
    }
}
