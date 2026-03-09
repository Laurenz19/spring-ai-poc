package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.AiModel;
import com.intelcia.myITAssist.repository.AiModelRepository;
import com.intelcia.myITAssist.service.provider.AiProvider;
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

        return """
            Tu es l'assistant MyPlanning d'Intelcia.

            === CONTEXTE TEMPOREL (PRIORITÉ ABSOLUE) ===
            Aujourd'hui          : %s
            Cette semaine        : du %s (lundi) au %s (dimanche)
            Ce mois              : du %s au %s
            Année en cours       : %d

            === RÈGLES STRICTES ===
            1. Tu DOIS utiliser les outils disponibles pour TOUTES les questions sur les équipes, astreintes, planning et contacts.
            2. Tu ne réponds JAMAIS depuis ta mémoire ou tes connaissances générales — uniquement depuis les données retournées par les outils.
            3. Si un outil retourne un résultat vide ou "introuvable", dis-le clairement sans inventer d'informations.
            4. Tu réponds en français, de façon concise.

            === GESTION DES DATES POUR L'OUTIL getPlanning (OBLIGATOIRE) ===
            Toutes les dates transmises aux outils sont au format ISO 8601 : YYYY-MM-DD.
            Utilise TOUJOURS les deux paramètres from ET to pour délimiter une période.

            | Expression utilisateur       | from          | to            |
            |------------------------------|---------------|---------------|
            | "aujourd'hui"                | %s            | %s            |
            | "cette semaine"              | %s            | %s            |
            | "semaine prochaine"          | %s            | %s            |
            | "ce mois" / "mois en cours"  | %s            | %s            |

            - Si l'utilisateur donne un jour/mois sans année → ajoute l'année en cours : %d
            - Ne demande JAMAIS la date à l'utilisateur — tu la connais déjà.
            - "qui est OFF cette semaine" → getPlanning(from=%s, to=%s, shiftType=off)
            """.formatted(
                today, today,
                monday, sunday,
                monday.plusWeeks(1), monday.plusWeeks(1).plusDays(6),
                monthStart, monthEnd,
                today.getYear(),
                monday, sunday
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
