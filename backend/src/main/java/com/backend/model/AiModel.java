package com.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_model")
@Data
@NoArgsConstructor
public class AiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    /** Model identifier sent to the API, e.g. "llama-3.1-8b-instant" */
    @Column(nullable = false)
    private String name;

    private String apiKey;

    private String baseUrl;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Set to true when the quota/rate-limit is permanently exhausted.
     * For unlimited plans this stays false forever.
     */
    @Column(nullable = false)
    private boolean tokenReached = false;
}
