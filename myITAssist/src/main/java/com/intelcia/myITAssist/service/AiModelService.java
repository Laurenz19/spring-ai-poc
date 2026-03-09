package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.AiModel;
import com.intelcia.myITAssist.repository.AiModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiModelService {

    private final AiModelRepository repo;

    public List<AiModel> findAll() {
        return repo.findAll();
    }

    public AiModel update(Long id, AiModel patch) {
        AiModel model = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AI model not found: " + id));
        model.setName(patch.getName());
        model.setApiKey(patch.getApiKey());
        model.setBaseUrl(patch.getBaseUrl());
        model.setEnabled(patch.isEnabled());
        model.setTokenReached(patch.isTokenReached());
        return repo.save(model);
    }

    public AiModel toggleEnabled(Long id) {
        AiModel model = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AI model not found: " + id));
        model.setEnabled(!model.isEnabled());
        return repo.save(model);
    }
}
