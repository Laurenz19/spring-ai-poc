package com.intelcia.myITAssist.repository;

import com.intelcia.myITAssist.model.Astreinte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AstreinteRepository extends JpaRepository<Astreinte, Long>, JpaSpecificationExecutor<Astreinte> {
}
