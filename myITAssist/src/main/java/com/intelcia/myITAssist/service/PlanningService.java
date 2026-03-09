package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.Planning;
import com.intelcia.myITAssist.repository.PlanningRepository;
import com.intelcia.myITAssist.spec.PlanningSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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

    public List<Planning> findOffAgentsByDay(LocalDate day) {
        return planningRepo.findAll(
            PlanningSpec.hasShiftType("off").and(PlanningSpec.hasDay(day))
        );
    }

    /**
     * Flexible query combining any combination of filters.
     *
     * @param from             start date (inclusive)
     * @param to               end date (inclusive)
     * @param teamIds          list of team IDs to include (null = all)
     * @param collaboratorName partial name match (null = all)
     * @param shiftType        exact type: "off" | "work" | "astreinte" (null = all)
     * @param shiftKeyword     substring to search in shiftLabel, e.g. "19:00" (null = ignored)
     */
    public List<Planning> findByFilters(LocalDate from, LocalDate to,
                                        List<String> teamIds,
                                        String collaboratorName,
                                        String shiftType,
                                        String shiftKeyword) {
        Specification<Planning> spec = PlanningSpec.dayBetween(from, to);

        if (teamIds != null && !teamIds.isEmpty()) {
            spec = spec.and(PlanningSpec.hasTeamIdIn(teamIds));
        }
        if (collaboratorName != null && !collaboratorName.isBlank()) {
            spec = spec.and(PlanningSpec.hasCollaboratorNameLike(collaboratorName));
        }
        if (shiftType != null && !shiftType.isBlank()) {
            spec = spec.and(PlanningSpec.hasShiftType(shiftType));
        }
        if (shiftKeyword != null && !shiftKeyword.isBlank()) {
            spec = spec.and(PlanningSpec.shiftLabelContains(shiftKeyword));
        }

        return planningRepo.findAll(spec);
    }
}
