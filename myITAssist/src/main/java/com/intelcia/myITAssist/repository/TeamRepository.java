package com.intelcia.myITAssist.repository;

import com.intelcia.myITAssist.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TeamRepository extends JpaRepository<Team, String>, JpaSpecificationExecutor<Team> {
}
