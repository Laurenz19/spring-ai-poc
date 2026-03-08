package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.Collaborator;
import com.intelcia.myITAssist.repository.CollaboratorRepository;
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
