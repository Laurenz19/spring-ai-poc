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
