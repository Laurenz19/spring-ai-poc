package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.Astreinte;
import com.intelcia.myITAssist.repository.AstreinteRepository;
import com.intelcia.myITAssist.spec.AstreinteSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AstreinteService {

    private final AstreinteRepository astreinteRepo;

    public List<Astreinte> findByWeek(LocalDate monday) {
        return astreinteRepo.findAll(AstreinteSpec.hasWeekDate(monday));
    }

    public List<Astreinte> findByCountryAndWeek(String country, LocalDate monday) {
        return astreinteRepo.findAll(AstreinteSpec.countryContains(country).and(AstreinteSpec.hasWeekDate(monday)));
    }
}
