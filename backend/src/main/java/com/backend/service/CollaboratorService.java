package com.backend.service;

import com.backend.model.Collaborator;
import com.backend.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollaboratorService {

    private final CollaboratorRepository collaboratorRepo;

    public Optional<Collaborator> findByName(String name) {
        return collaboratorRepo.findByNameContainingIgnoreCase(name);
    }
}
