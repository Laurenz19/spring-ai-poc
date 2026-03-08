package com.intelcia.myITAssist.repository;

import com.intelcia.myITAssist.model.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlanningRepository extends JpaRepository<Planning, Long>, JpaSpecificationExecutor<Planning> {
}
