package com.backend.spec;

import com.backend.model.Astreinte;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AstreinteSpec {

    private AstreinteSpec() {}

    public static Specification<Astreinte> countryContains(String country) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%");
    }

    public static Specification<Astreinte> hasWeekDate(LocalDate date) {
        return (root, query, cb) ->
            cb.equal(root.get("weekDate"), date);
    }
}
