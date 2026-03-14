package com.backend.repository;

import com.backend.model.Astreinte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AstreinteRepository extends JpaRepository<Astreinte, Long>, JpaSpecificationExecutor<Astreinte> {
}
