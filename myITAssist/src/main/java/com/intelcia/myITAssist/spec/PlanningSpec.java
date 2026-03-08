package com.intelcia.myITAssist.spec;

import com.intelcia.myITAssist.model.Planning;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

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

    public static Specification<Planning> hasTeamId(String teamId) {
        return (root, query, cb) ->
            cb.equal(root.get("teamId"), teamId);
    }

    public static Specification<Planning> hasDay(LocalDate day) {
        return (root, query, cb) ->
            cb.equal(root.get("day"), day);
    }
}
