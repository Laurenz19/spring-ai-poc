package com.intelcia.myITAssist.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
        Tu es l'assistant MyPlanning d'Intelcia. Règles STRICTES :
        1. Tu DOIS utiliser les outils disponibles pour TOUTES les questions sur les équipes, astreintes, planning et contacts.
        2. Tu ne réponds JAMAIS depuis ta mémoire ou tes connaissances générales — uniquement depuis les données retournées par les outils.
        3. Si un outil retourne un résultat vide ou "introuvable", dis-le clairement sans inventer d'informations.
        4. Tu réponds en français, de façon concise.
        """;

    private final List<NamedClient> providers;

    public ChatService(OpenAiChatModel groqModel,
                       OllamaChatModel ollamaModel,
                       PlanningTools tools) {
        this.providers = List.of(
            build("Ollama (qwen3.5:4b)",       ollamaModel, tools),
            build("Groq (llama-3.1-8b-instant)", groqModel, tools)
        );
    }

    public String respond(String message) {
        List<String> tried = new ArrayList<>();
        Exception lastError = null;

        for (NamedClient provider : providers) {
            try {
                log.debug("[AI] Trying provider: {}", provider.name());
                String result = provider.client().prompt()
                    .user(message)
                    .call()
                    .content();
                if (!tried.isEmpty()) {
                    log.info("[AI] Fallback successful — responded via {}", provider.name());
                }
                return result;
            } catch (Exception e) {
                if (isRateLimitOrUnavailable(e)) {
                    tried.add(provider.name());
                    lastError = e;
                    log.warn("[AI] Provider '{}' unavailable: {} — switching to next...",
                        provider.name(), summarize(e));
                } else {
                    throw e;
                }
            }
        }

        log.error("[AI] All providers exhausted. Tried: {}", tried);
        throw new RuntimeException(
            "Tous les fournisseurs IA sont temporairement indisponibles. Réessayez dans un moment.",
            lastError
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private NamedClient build(String name, ChatModel model, PlanningTools tools) {
        ChatClient client = ChatClient.builder(model)
            .defaultSystem(SYSTEM_PROMPT)
            .defaultTools(tools)
            .build();
        return new NamedClient(name, client);
    }

    /**
     * true  → rate limit / provider down → try next
     * false → hard error (bad key, bad request) → rethrow immediately
     */
    private boolean isRateLimitOrUnavailable(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("429")
            || msg.contains("rate_limit")
            || msg.contains("rate limit")
            || msg.contains("quota_exceeded")
            || msg.contains("quota exceeded")
            || msg.contains("overloaded")
            || msg.contains("capacity")
            || msg.contains("service unavailable")
            || msg.contains("503")
            || msg.contains("connection refused")
            || msg.contains("tool_use_failed")
            || msg.contains("failed to call a function")
            || msg.contains("model_decommissioned");
    }

    private String summarize(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "unknown";
        return msg.length() > 120 ? msg.substring(0, 120) + "…" : msg;
    }

    record NamedClient(String name, ChatClient client) {}
}
