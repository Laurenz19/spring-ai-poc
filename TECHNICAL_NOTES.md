# Technical Notes

This document captures important architectural decisions, known bugs, and performance optimizations.

## Known Bugs & Limitations

### Ollama + Spring AI 1.0.0 Streaming Bug
- **Issue**: `OllamaChatModel.stream()` throws a `NullPointerException` on `evalDuration` when tool calling (Function Calling) is active.
- **Workaround**: 
    - The backend uses blocking `.call().content()` calls.
    - To maintain a reactive flow, these calls are wrapped in `Mono.fromCallable()` and executed on a `boundedElastic` scheduler in `ChatController.java`.
- **User Impact**: Token-by-token streaming is currently disabled. The frontend receives the full response as a single EventSource message once it is completely generated.

## Performance Optimizations

### ChatClient Caching
- **Implementation**: `ChatService.java` uses a `ConcurrentHashMap<Long, ChatClient>` to cache instances of `ChatClient`.
- **Rationale**: Building a `ChatClient` and its underlying `ChatModel` (via Spring AI providers) is an expensive operation involving reflection and connectivity checks. Caching improves response latency significantly.
- **Dynamic Context**: 
    - The system prompt is **not** cached inside the `ChatClient` builder.
    - Instead, the dynamic system prompt (containing current dates, weeks, etc.) is applied to each request using `.system(buildSystemPrompt())` during the `.prompt()` call.
    - This ensures that cached clients do not use stale temporal information.

## Tool Design Philosophy

### Parameter Optionality vs. Tool Specialization
- **Decision**: Prefer a single tool with multiple optional parameters over multiple specialized tools.
- **Example**: `getTeams(country, teamName)` instead of `getTeamsByCountry(country)` and `getTeamsByCountryAndName(country, name)`.
- **Rational**:
    - Reduces ambiguity for the AI model when deciding which tool to call.
    - Lowers token consumption by having fewer tool definitions in the system prompt.
    - Simplifies maintenance by centralizing the formatting and retrieval logic for a specific entity.

## Resilience & Error Handling

### Proactive Quota Management
- **Issue**: Previously, failing models were retried on every request, causing unnecessary latency.
- **Solution**: The system now checks for `429 (Rate Limit)` and `Quota Exceeded` errors. When encountered, the model is automatically marked as `tokenReached=true` in the database.

### Basic Circuit Breaker (Cooldown)
- **Mechanism**: A `cooldownCache` (`ConcurrentHashMap`) stores models that failed due to transient issues (e.g., `503 Service Unavailable`).
- **Behavior**: Such models are placed in a **5-minute cooldown**. They are skipped during the fallback loop until the cooldown expires, improving system responsiveness.

### Future Improvements
- **Resilience4j**: For production scale, consider full integration of Resilience4j for more advanced patterns (exponential backoff, sophisticated circuit breakers).
