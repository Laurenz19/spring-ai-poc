package com.intelcia.myITAssist.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    /**
     * Declares OpenAI (Groq) as the primary ChatModel so Spring AI's auto-configured
     * ChatClient.Builder doesn't fail when multiple ChatModel beans are present.
     * ChatService manages its own fallback chain and injects each model by concrete type.
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }
}
