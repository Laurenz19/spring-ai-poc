package com.backend.spec;

import com.backend.model.Team;
import org.springframework.data.jpa.domain.Specification;

public class TeamSpec {

    private TeamSpec() {}

    public static Specification<Team> countryContains(String country) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%");
    }

    public static Specification<Team> nameContains(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
