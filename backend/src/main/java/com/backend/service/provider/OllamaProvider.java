package com.backend.service.provider;

import com.backend.model.AiModel;
import com.backend.model.Provider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Component;

@Component
public class OllamaProvider implements AiProvider {

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.OLLAMA;
    }

    @Override
    public ChatModel build(AiModel model) {
        var api = OllamaApi.builder().baseUrl(model.getBaseUrl()).build();
        return OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(OllamaOptions.builder().model(model.getName()).build())
                .build();
    }
}
