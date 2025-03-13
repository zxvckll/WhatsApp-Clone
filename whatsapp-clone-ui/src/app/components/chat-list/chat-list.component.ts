import {Component, input, InputSignal, output} from '@angular/core';
import {ChatResponse} from '../../services/models/chat-response';
import {DatePipe} from '@angular/common';
import {UserResponse} from '../../services/models/user-response';
import { UserService } from '../../services/services/user.service';
import {ChatService} from '../../services/services/chat.service';
import {KeycloakService} from '../../utils/keycloak/keycloak.service';

@Component({
  selector: 'app-chat-list',
  imports: [
    DatePipe
  ],
  templateUrl: './chat-list.component.html',
  styleUrl: './chat-list.component.scss'
})
export class ChatListComponent {
  chats: InputSignal<ChatResponse[]> = input<ChatResponse[]>([]);
  searchNewContact:boolean = false;
  contacts: Array<UserResponse> = [];
  chatSelected = output<ChatResponse>()


  constructor(
    private userService: UserService,
    private chatService: ChatService,
    private keycloakService: KeycloakService
  ) {
  }

  searchContact() {
    this.userService.getAllUsers()
      .subscribe(
        {next: (users) => {
          this.contacts = users;
          this.searchNewContact = true;
        }}
      )
  }

  chatClicked(chat: ChatResponse) {
    this.chatSelected.emit(chat);
  }

  wrapMessage(lastMessage: string | undefined) {
    if(lastMessage && lastMessage.length <= 20){
      return lastMessage;
    }
    return lastMessage?.substring(0, 17) + "...";
  }

  selectContact(contact: UserResponse) {

    this.chatService.createChat({
      'sender-id': this.keycloakService.userId as string,
      'receiver-id': contact.id as string,
    }).subscribe({
      next: (result) => {
        const chatResponse: ChatResponse = {
          id:result.response,
          name: contact.firstName + ' ' + contact.lastName,
          otherUserOnline: contact.online,
          lastMessageTime: contact.lastSeen,
          senderId: this.keycloakService.userId as string,
          receiverId:contact.id
        };
        this.chats().unshift(chatResponse);
        this.searchNewContact = false;
        this.chatSelected.emit(chatResponse);
      }
    });
  }
}
