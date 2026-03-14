package com.backend.repository;

import com.backend.model.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CollaboratorRepository extends JpaRepository<Collaborator, String>, JpaSpecificationExecutor<Collaborator> {
    Optional<Collaborator> findByNameContainingIgnoreCase(String name);
}
