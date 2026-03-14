package com.backend.repository;

import com.backend.model.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlanningRepository extends JpaRepository<Planning, Long>, JpaSpecificationExecutor<Planning> {
}
