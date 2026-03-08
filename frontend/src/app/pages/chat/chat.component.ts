import { Component, ElementRef, ViewChild, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { ChatService } from '../../services/chat.service';
import { Message } from '../../models/message.model';
import { MarkdownPipe } from '../../pipes/markdown.pipe';

@Component({
  selector: 'app-chat',
  imports: [FormsModule, NgClass, MarkdownPipe],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent {
  @ViewChild('messagesEl') messagesEl!: ElementRef<HTMLDivElement>;

  messages = signal<Message[]>([
    {
      role: 'bot',
      content: `Bonjour ! Je suis votre assistant **MyPlanning**. Je peux vous aider à :

- Savoir qui est en **astreinte** par pays/équipe
- Consulter les jours **OFF** de la semaine
- Vérifier la **disponibilité** d'un collaborateur
- Afficher le **planning** d'une équipe
- Trouver le **contact GSM** d'un agent

Que puis-je faire pour vous ?`,
      timestamp: new Date()
    }
  ]);

  inputText = '';
  loading = signal(false);
  isStreaming = signal(false);

  readonly suggestions = [
    { icon: 'support_agent', label: 'Astreinte Madagascar',  text: 'Qui est en astreinte à Madagascar ?' },
    { icon: 'event_busy',    label: 'OFF en cours',          text: 'Qui est OFF cette semaine ?' },
    { icon: 'calendar_month',label: 'Planning MADA2',        text: 'Planning Infra MADA2' },
    { icon: 'phone',         label: 'Contact Jacky',         text: 'Contact Jacky' },
    { icon: 'warning',       label: 'Escalade Jacky',        text: 'Jacky ne répond pas, escalade ?' },
  ];

  constructor(private chatService: ChatService) {}

  send(text?: string) {
    const msg = (text ?? this.inputText).trim();
    if (!msg || this.loading()) return;

    this.pushMessage('user', msg);
    this.inputText = '';
    this.loading.set(true);
    this.isStreaming.set(false);

    this.chatService.sendStream(msg).subscribe({
      next: (chunk) => {
        if (!this.isStreaming()) {
          this.isStreaming.set(true);
          this.pushMessage('bot', chunk);
        } else {
          this.messages.update(msgs => {
            const updated = [...msgs];
            const last = updated[updated.length - 1];
            updated[updated.length - 1] = { ...last, content: last.content + chunk };
            return updated;
          });
        }
        this.scrollBottom();
      },
      error: () => {
        this.pushMessage('bot', 'Une erreur est survenue. Veuillez réessayer.');
        this.loading.set(false);
        this.isStreaming.set(false);
      },
      complete: () => {
        this.loading.set(false);
        this.isStreaming.set(false);
        this.scrollBottom();
      }
    });

    this.scrollBottom();
  }

  clearChat() {
    this.messages.set([]);
    this.loading.set(false);
  }

  private pushMessage(role: 'bot' | 'user', content: string) {
    this.messages.update(msgs => [...msgs, { role, content, timestamp: new Date() }]);
  }

  private scrollBottom() {
    setTimeout(() => {
      const el = this.messagesEl?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 50);
  }
}
