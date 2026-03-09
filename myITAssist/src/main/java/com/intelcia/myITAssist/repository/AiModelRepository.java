package com.intelcia.myITAssist.repository;

import com.intelcia.myITAssist.model.AiModel;
import com.intelcia.myITAssist.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiModelRepository extends JpaRepository<AiModel, Long> {

    /** Returns enabled, non-exhausted models in insertion order (priority). */
    List<AiModel> findByEnabledTrueAndTokenReachedFalseOrderByIdAsc();

    boolean existsByProviderAndName(Provider provider, String name);
}
