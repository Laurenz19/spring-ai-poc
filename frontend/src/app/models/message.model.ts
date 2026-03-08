export type MessageRole = 'bot' | 'user';

export interface Message {
  role: MessageRole;
  content: string;
  timestamp: Date;
}
