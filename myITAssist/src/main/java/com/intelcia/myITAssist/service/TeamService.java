package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.Team;
import com.intelcia.myITAssist.repository.TeamRepository;
import com.intelcia.myITAssist.spec.TeamSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepo;

    public List<Team> findAll() {
        return teamRepo.findAll();
    }

    public Optional<Team> findById(String id) {
        return teamRepo.findById(id);
    }

    public List<Team> findByCountry(String country) {
        return teamRepo.findAll(TeamSpec.countryContains(country));
    }

    public List<Team> findByName(String name) {
        return teamRepo.findAll(TeamSpec.nameContains(name));
    }

    public List<Team> findByCountryAndName(String country, String name) {
        return teamRepo.findAll(TeamSpec.countryContains(country).and(TeamSpec.nameContains(name)));
    }

    public Map<String, String> buildIdToNameMap() {
        return findAll().stream().collect(Collectors.toMap(Team::getId, Team::getName));
    }
}
