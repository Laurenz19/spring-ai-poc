package com.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "planning")
@Data
public class Planning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String collaboratorId;
    private String collaboratorName;
    private String teamId;
    private LocalDate day;
    private String shiftType;   // "morning", "evening", "night", "off"
    private String shiftLabel;  // "08h-17h", "OFF", etc.
}
