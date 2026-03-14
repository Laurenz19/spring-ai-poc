package com.backend.service.provider;

import com.backend.model.AiModel;
import com.backend.model.Provider;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class AnthropicProvider implements AiProvider {

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.ANTHROPIC;
    }

    @Override
    public ChatModel build(AiModel model) {
        var api = AnthropicApi.builder().apiKey(model.getApiKey()).build();
        return AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(AnthropicChatOptions.builder().model(model.getName()).build())
                .build();
    }
}
