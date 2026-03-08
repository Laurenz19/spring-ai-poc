package com.intelcia.myITAssist.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "teams")
@Data
public class Team {

    @Id
    private String id;

    private String name;
    private String country;
    private String site;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Collaborator> collaborators;
}
