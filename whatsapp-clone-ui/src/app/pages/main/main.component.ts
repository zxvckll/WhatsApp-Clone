import {Component, OnInit} from '@angular/core';
import {ChatListComponent} from '../../components/chat-list/chat-list.component';
import {ChatResponse} from '../../services/models/chat-response';
import {ChatService} from '../../services/services/chat.service';
import {KeycloakService} from '../../utils/keycloak/keycloak.service';
import {MessageService} from '../../services/services/message.service';
import {MessageResponse} from '../../services/models/message-response';
import {DatePipe} from '@angular/common';
import {PickerComponent} from '@ctrl/ngx-emoji-mart';
import {FormsModule} from '@angular/forms';
import {EmojiData} from '@ctrl/ngx-emoji-mart/ngx-emoji';
import {MessageRequest} from '../../services/models/message-request';

@Component({
  selector: 'app-main',
  imports: [
    ChatListComponent,
    DatePipe,
    PickerComponent,
    FormsModule
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit {

  chats: Array<ChatResponse> = [];
  selectedChat: ChatResponse = {};
  chatMessages: Array<MessageResponse> = [];
  showEmojis: boolean  = false;
  messageContent: string = '';


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
    this.messageService.setMessageToSeen({'chat-id': chatId}).subscribe({
      next: () => {
        this.getAllChatsMessages(chatId);
      }
    });
  }

  isSelfMessage(message:MessageResponse) {
    return message.senderId === this.keycloakService.userId;
  }


  uploadMedia(target: EventTarget | null) {

  }

  onSelectEmoji(emojiSelected: any) {
    const emoji:EmojiData = emojiSelected.emoji;
    this.messageContent += emoji?.native || '';
  }

  keyDown(event: KeyboardEvent) {
    if(event.key === 'Enter') {
      this.sendMessage();
    }
  }

  onClick(chatId:string) {
    this.setMessagesToSeen(chatId);
  }

  sendMessage() {
    if (this.messageContent) {
      const messageRequest: MessageRequest = {
        chatId:this.selectedChat.id,
        senderId:this.getSenderId(),
        receiverId:this.getReceiverId(),
        content:this.messageContent,
        type: 'TEXT'
      }
      this.messageService.saveMessage({
        body:messageRequest,
      }).subscribe({
        next: () => {
          const message:MessageResponse = {
            senderId:this.getSenderId(),
            receiverId:this.getReceiverId(),
            content:this.messageContent,
            type: 'TEXT',
            state:'SENT',
            createdAt:new Date().toString(),
          };
          this.selectedChat.lastMessage = this.messageContent;
          this.chatMessages.push(message);
          this.messageContent = '';
          this.showEmojis = false;
        }
      })
    }
  }

  private getSenderId():string {
    if (this.keycloakService.userId === this.selectedChat.senderId) {
      return this.selectedChat.senderId as string;
    }
    return this.selectedChat.receiverId as string;
  }

  private getReceiverId():string {
    if (this.keycloakService.userId === this.selectedChat.senderId) {
      return this.selectedChat.receiverId as string;
    }
    return this.selectedChat.senderId as string;
  }
}
