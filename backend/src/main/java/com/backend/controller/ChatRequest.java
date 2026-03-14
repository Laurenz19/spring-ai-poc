package com.backend.controller;

/**
 * @param message the user's message
 * @param modelId optional — if set, use that model directly; if null, use the priority fallback chain
 */
public record ChatRequest(String message, Long modelId) {}

