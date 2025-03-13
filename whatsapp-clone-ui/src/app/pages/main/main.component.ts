import {Component, OnInit} from '@angular/core';
import {ChatListComponent} from '../../components/chat-list/chat-list.component';
import {ChatResponse} from '../../services/models/chat-response';
import {ChatService} from '../../services/services/chat.service';
import {KeycloakService} from '../../utils/keycloak/keycloak.service';
import {MessageService} from '../../services/services/message.service';
import {MessageResponse} from '../../services/models/message-response';
import {DatePipe, NgOptimizedImage} from '@angular/common';
import {uploadMedia} from '../../services/fn/message/upload-media';

@Component({
  selector: 'app-main',
  imports: [
    ChatListComponent,
    DatePipe
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit {

  chats: Array<ChatResponse> = [];
  selectedChat: ChatResponse = {};
  chatMessages: Array<MessageResponse> = [];


  constructor(
    private chatService:ChatService,
    private keycloakService: KeycloakService,
    private messageService: MessageService
  ){}

  ngOnInit(): void {
    this.getAllChats();
  }

  private getAllChats(){
    this.chatService.getChatsByReceiver().subscribe({
      next: (res) => {
        this.chats = res}
      }
    );
  }

  logout() {
    this.keycloakService.logout();
  }

  userProfile() {
    this.keycloakService.accountManagement();
  }

  chatSelected(chatResponse: ChatResponse) {
    this.selectedChat = chatResponse;
    this.getAllChatsMessages(chatResponse.id as string);
    this.setMessagesToSeen(chatResponse.id as string);
    this.selectedChat.unreadCount = 0;
  }

  private getAllChatsMessages(chatId: string) {
    this.messageService.getChatMessage({
      'chat-id': chatId
    }).subscribe({
      next: (messages) => {
        this.chatMessages = messages;
      }
    })
  }

  private setMessagesToSeen(chatId:string) {
    this.messageService.setMessageToSeen({'chat-id': chatId});
  }

  isSelfMessage(message:MessageResponse) {
    return message.senderId === this.keycloakService.userId;
  }


  uploadMedia(target: EventTarget | null) {

  }
}
