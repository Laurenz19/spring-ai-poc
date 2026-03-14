package com.backend.service.provider;

import com.backend.model.AiModel;
import com.backend.model.Provider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * Handles GROQ, GEMINI and OPENAI — all expose an OpenAI-compatible REST API.
 * For OPENAI the baseUrl is null/blank, so the default (api.openai.com) is used.
 */
@Component
public class OpenAiCompatibleProvider implements AiProvider {

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.GROQ || provider == Provider.GEMINI || provider == Provider.OPENAI;
    }

    @Override
    public ChatModel build(AiModel model) {
        var apiBuilder = OpenAiApi.builder().apiKey(model.getApiKey());
        if (model.getBaseUrl() != null && !model.getBaseUrl().isBlank()) {
            apiBuilder.baseUrl(model.getBaseUrl());
        }
        var api = apiBuilder.build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(model.getName()).build())
                .build();
    }
}
