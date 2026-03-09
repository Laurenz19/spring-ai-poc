package com.intelcia.myITAssist.spec;

import com.intelcia.myITAssist.model.Planning;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class PlanningSpec {

    private PlanningSpec() {}

    public static Specification<Planning> hasShiftType(String type) {
        return (root, query, cb) ->
            cb.equal(cb.lower(root.get("shiftType")), type.toLowerCase());
    }

    public static Specification<Planning> dayBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) ->
            cb.between(root.get("day"), from, to);
    }

    public static Specification<Planning> hasDay(LocalDate day) {
        return (root, query, cb) ->
            cb.equal(root.get("day"), day);
    }

    public static Specification<Planning> hasTeamId(String teamId) {
        return (root, query, cb) ->
            cb.equal(root.get("teamId"), teamId);
    }

    public static Specification<Planning> hasTeamIdIn(List<String> teamIds) {
        return (root, query, cb) -> root.get("teamId").in(teamIds);
    }

    public static Specification<Planning> hasCollaboratorNameLike(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("collaboratorName")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Planning> shiftLabelContains(String keyword) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("shiftLabel")), "%" + keyword.toLowerCase() + "%");
    }
}
