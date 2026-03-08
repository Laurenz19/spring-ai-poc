package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.Planning;
import com.intelcia.myITAssist.repository.PlanningRepository;
import com.intelcia.myITAssist.spec.PlanningSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningRepository planningRepo;

    public List<Planning> findByWeek(LocalDate monday) {
        return planningRepo.findAll(PlanningSpec.dayBetween(monday, monday.plusDays(6)));
    }

    public List<Planning> findByTeamAndDay(String teamId, LocalDate day) {
        return planningRepo.findAll(PlanningSpec.hasTeamId(teamId).and(PlanningSpec.hasDay(day)));
    }

    public List<Planning> findOffAgentsByWeek(LocalDate monday) {
        return planningRepo.findAll(
            PlanningSpec.hasShiftType("off").and(PlanningSpec.dayBetween(monday, monday.plusDays(4)))
        );
    }
}
