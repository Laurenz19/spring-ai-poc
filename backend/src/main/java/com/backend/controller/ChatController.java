package com.backend.controller;

import com.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public String chat(@RequestBody ChatRequest req) {
        return chatService.respond(req.message(), req.modelId());
    }

    // Spring AI 1.0.0 bug: OllamaChatModel.stream() NPEs on evalDuration when tool calling is active.
    // Workaround: run the blocking call on a separate thread and emit the result as a single SSE event.
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<String> streamChat(@RequestBody ChatRequest req) {
        return Mono.fromCallable(() -> chatService.respond(req.message(), req.modelId()))
                   .subscribeOn(Schedulers.boundedElastic());
    }
}
