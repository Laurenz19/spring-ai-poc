package com.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "astreintes")
@Data
public class Astreinte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;
    private String site;
    private String agentName;
    private String gsm;
    private String flag;
    private boolean planned;
    private LocalDate weekDate;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
