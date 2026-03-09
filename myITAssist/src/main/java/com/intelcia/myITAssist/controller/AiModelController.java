package com.intelcia.myITAssist.controller;

import com.intelcia.myITAssist.model.AiModel;
import com.intelcia.myITAssist.service.AiModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-models")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService service;

    @GetMapping
    public List<AiModel> findAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public AiModel update(@PathVariable Long id, @RequestBody AiModel patch) {
        return service.update(id, patch);
    }

    @PatchMapping("/{id}/toggle")
    public AiModel toggle(@PathVariable Long id) {
        return service.toggleEnabled(id);
    }
}
