package com.backend.service;

import com.backend.model.AiModel;
import com.backend.repository.AiModelRepository;
import com.backend.service.provider.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static String buildSystemPrompt() {
        LocalDate today      = LocalDate.now();
        LocalDate monday     = today.with(DayOfWeek.MONDAY);
        LocalDate sunday     = monday.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = today.withDayOfMonth(today.lengthOfMonth());

        LocalDate nextMonday = monday.plusWeeks(1);
        LocalDate nextSunday = nextMonday.plusDays(6);

        return ("Tu es l'assistant IA. Tu as accès à des outils pour répondre.\n\n"

            + "=== RÈGLE ABSOLUE ===\n"
            + "Tu NE DOIS JAMAIS répondre directement sans appeler un outil d'abord.\n"
            + "Pour toute question sur le planning, les absences (OFF), les astreintes, les équipes ou les contacts :\n"
            + "  -> APPELLE L'OUTIL CORRESPONDANT IMMÉDIATEMENT.\n"
            + "Ne donne pas d'explication, ne pose pas de question, appelle l'outil.\n\n"

            + "=== DATE DU JOUR ===\n"
            + "Aujourd'hui    : " + today      + "\n"
            + "Cette semaine  : " + monday     + " (lundi) au " + sunday     + " (dimanche)\n"
            + "Semaine proch. : " + nextMonday + " au " + nextSunday + "\n"
            + "Ce mois        : " + monthStart + " au " + monthEnd  + "\n"
            + "Année en cours : " + today.getYear() + "\n\n"

            + "=== EXEMPLES D'APPELS D'OUTILS ===\n"
            + "- \"Qui est OFF cette semaine ?\"        -> getPlanning(from=" + monday + ", to=" + sunday + ", shiftType=off)\n"
            + "- \"Planning MADA2 cette semaine\"       -> getPlanning(from=" + monday + ", to=" + sunday + ", teamName=MADA2)\n"
            + "- \"Qui est en astreinte à Madagascar ?\"-> getAstreinte(country=Madagascar)\n"
            + "- \"Contact Jacky\"                      -> getContact(name=Jacky)\n"
            + "- \"Planning Jacky cette semaine\"       -> getPlanning(from=" + monday + ", to=" + sunday + ", collaboratorName=Jacky)\n"
            + "- \"Qui travaille aujourd'hui ?\"        -> getPlanning(from=" + today  + ", to=" + today  + ", shiftType=work)\n\n"

            + "=== AUTRES RÈGLES ===\n"
            + "- Dates au format ISO : YYYY-MM-DD. Toujours passer from ET to.\n"
            + "- Si un outil retourne vide : dis-le, n'invente rien.\n"
            + "- Réponds en français, de façon concise.\n"
        );
    }

    private final AiModelRepository aiModelRepository;
    private final List<AiProvider> aiProviders;
    private final PlanningTools tools;

    public String respond(String message, Long modelId) {
        // ── Specific model requested ──────────────────────────────────────────
        if (modelId != null) {
            AiModel model = aiModelRepository.findById(modelId)
                    .orElseThrow(() -> new IllegalArgumentException("Modèle introuvable : " + modelId));
            String label = model.getProvider() + " (" + model.getName() + ")";
            log.debug("[AI] Using selected model: {}", label);
            try {
                String result = buildClient(model).prompt().user(message).call().content();
                if (result == null || result.isBlank()) {
                    throw new RuntimeException("Le modèle n'a retourné aucune réponse.");
                }
                return result;
            } catch (Exception e) {
                log.error("[AI] Error with selected model {}: {}", label, summarize(e));
                throw new RuntimeException(
                    "Le modèle " + label + " a rencontré une erreur : " + friendlyError(e), e);
            }
        }

        // ── Auto fallback chain ───────────────────────────────────────────────
        List<AiModel> models = aiModelRepository.findByEnabledTrueAndTokenReachedFalseOrderByIdAsc();

        if (models.isEmpty()) {
            throw new RuntimeException("Aucun fournisseur IA n'est activé. Veuillez en activer un depuis les paramètres.");
        }

        List<String> tried = new ArrayList<>();
        Exception lastError = null;

        for (AiModel model : models) {
            String label = model.getProvider() + " (" + model.getName() + ")";
            try {
                log.debug("[AI] Trying provider: {}", label);
                String result = buildClient(model).prompt()
                        .user(message)
                        .call()
                        .content();
                if (result == null || result.isBlank()) {
                    tried.add(label);
                    log.warn("[AI] Provider '{}' returned empty response — switching to next...", label);
                    continue;
                }
                if (!tried.isEmpty()) {
                    log.info("[AI] Fallback successful — responded via {}", label);
                }
                return result;
            } catch (Exception e) {
                tried.add(label);
                lastError = e;
                log.warn("[AI] Provider '{}' failed: {} — switching to next...", label, summarize(e));
            }
        }

        log.error("[AI] All providers exhausted. Tried: {}", tried);
        throw new RuntimeException(
                "Tous les fournisseurs IA sont temporairement indisponibles. Réessayez dans un moment.",
                lastError);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ChatClient buildClient(AiModel model) {
        return ChatClient.builder(buildChatModel(model))
                .defaultSystem(buildSystemPrompt())
                .defaultTools(tools)
                .build();
    }

    private ChatModel buildChatModel(AiModel model) {
        return aiProviders.stream()
                .filter(p -> p.supports(model.getProvider()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aucun fournisseur pour : " + model.getProvider()))
                .build(model);
    }

    private String friendlyError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("429") || msg.contains("rate_limit") || msg.contains("rate limit"))
            return "limite de requêtes atteinte (rate limit).";
        if (msg.contains("quota_exceeded") || msg.contains("quota exceeded"))
            return "quota épuisé.";
        if (msg.contains("503") || msg.contains("service unavailable") || msg.contains("overloaded"))
            return "service temporairement indisponible.";
        if (msg.contains("401") || msg.contains("unauthorized") || msg.contains("invalid api key"))
            return "clé API invalide ou expirée.";
        if (msg.contains("context_length") || msg.contains("too long") || msg.contains("max_tokens"))
            return "message trop long pour ce modèle.";
        if (msg.contains("tool") || msg.contains("function") || msg.contains("400"))
            return "erreur lors de l'appel aux outils.";
        if (msg.contains("connection") || msg.contains("timeout"))
            return "impossible de joindre le service.";
        return "erreur inattendue.";
    }

    private String summarize(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "unknown";
        return msg.length() > 120 ? msg.substring(0, 120) + "…" : msg;
    }
}
