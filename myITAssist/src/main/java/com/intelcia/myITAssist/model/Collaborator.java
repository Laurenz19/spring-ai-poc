package com.intelcia.myITAssist.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "collaborators")
@Data
public class Collaborator {

    @Id
    private String id;

    private String name;
    private String gsm;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonBackReference
    private Team team;
}
