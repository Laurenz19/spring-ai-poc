export type Provider = 'GROQ' | 'GEMINI' | 'OLLAMA' | 'ANTHROPIC' | 'OPENAI';

export interface AiModel {
  id: number;
  provider: Provider;
  name: string;
  apiKey: string;
  baseUrl: string;
  enabled: boolean;
  tokenReached: boolean;
}
