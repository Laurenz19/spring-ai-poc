package com.backend.config;

import com.backend.model.*;
import com.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepo;
    private final CollaboratorRepository collaboratorRepo;
    private final AstreinteRepository astreinteRepo;
    private final PlanningRepository planningRepo;
    private final AiModelRepository aiModelRepo;

    @Value("${GROQ_API_KEY:}")      private String groqApiKey;
    @Value("${GEMINI_API_KEY:}")    private String geminiApiKey;
    @Value("${ANTHROPIC_API_KEY:}") private String anthropicApiKey;
    @Value("${OPENAI_API_KEY:}")    private String openaiApiKey;

    @Override
    public void run(String... args) {
        seedAiModels();
        if (teamRepo.count() > 0) {
            log.info("Data already initialized, skipping seed.");
            return;
        }
        log.info("Seeding database: Jan–Mar 2026...");
        seedTeams();
        seedAstreintes();
        seedPlanning();
        log.info("Database seeded successfully.");
    }

    // ─── AI MODELS ────────────────────────────────────────────────────────────

    private void seedAiModels() {
        seedIfAbsent(Provider.GROQ,      "llama-3.1-8b-instant",       groqApiKey,      "https://api.groq.com/openai",                              true);
        seedIfAbsent(Provider.GEMINI,    "gemini-2.0-flash",           geminiApiKey,    "https://generativelanguage.googleapis.com/v1beta/openai/", true);
        seedIfAbsent(Provider.OLLAMA,    "qwen3.5:4b",                 "",              "http://localhost:11434",                                    true);
        seedIfAbsent(Provider.ANTHROPIC, "claude-haiku-4-5-20251001",  anthropicApiKey, null,                                                       false);
        seedIfAbsent(Provider.OPENAI,    "gpt-4o-mini",                openaiApiKey,    null,                                                       false);
    }

    private void seedIfAbsent(Provider provider, String name, String apiKey, String baseUrl, boolean enabled) {
        if (!aiModelRepo.existsByProviderAndName(provider, name)) {
            aiModelRepo.save(aiModel(provider, name, apiKey, baseUrl, enabled));
            log.info("AI model added: {} / {}", provider, name);
        }
    }

    private AiModel aiModel(Provider provider, String name, String apiKey, String baseUrl, boolean enabled) {
        AiModel m = new AiModel();
        m.setProvider(provider);
        m.setName(name);
        m.setApiKey(apiKey);
        m.setBaseUrl(baseUrl);
        m.setEnabled(enabled);
        m.setTokenReached(false);
        return m;
    }

    // ─── TEAMS & COLLABORATORS ────────────────────────────────────────────────

    private void seedTeams() {
        Team mada2 = team("team-mada2", "Infra MADA2", "Madagascar", "MADA2 Crystal");
        collab(mada2, "T00157", "Jacky",   "261341464994");
        collab(mada2, "T06442", "David",   "261341465029");
        collab(mada2, "T08453", "Romy",    "261341465028");
        collab(mada2, "T09235", "Nanja",   "261341465047");
        collab(mada2, "T08401", "Nilaina", "261383458679");

        Team mada1 = team("team-mada1", "Infra MADA1", "Madagascar", "MADA1 Futura");
        collab(mada1, "T00851", "Eddy",       "261341464990");
        collab(mada1, "T07419", "Nantenaina", "261341465003");
        collab(mada1, "T07872", "Mathieu",    "261341464989");
        collab(mada1, "T07176", "Erica",      "261341464989");
        collab(mada1, "T08504", "Tojo",       "261341465004");

        Team ebene = team("team-ebene", "Infra Ebene", "Maurice", "Ebene");
        collab(ebene, "R00008", "Mitch Green",     "23058402192");
        collab(ebene, "R01676", "Rodney Duchesne", "23059876917");

        Team abidjan = team("team-abidjan", "Infra Abidjan", "Abidjan", "Abidjan IT");
        collab(abidjan, "A00062", "Tia Curiae",      "0787010180");
        collab(abidjan, "A00278", "Sorho Dognon",    "0787010123");
        collab(abidjan, "A01734", "YOMANN Emmanuel", "0787010124");

        Team thies = team("team-thies", "Infra Thies", "Sénégal", "Thies");
        collab(thies, "SN001", "Papa El Hadj Moussa Thiam", "0022177537730");
        collab(thies, "SN002", "El Hadj Malick Diop",       "0022178462180");

        Team pointe = team("team-pointe", "Infra Point E", "Sénégal", "SN Point E");
        collab(pointe, "SN003", "Cheikh Samb (D00470)",  "0022177537742");
        collab(pointe, "SN004", "Ibrahim Faye (D04102)", "0022178588030");

        Team paris = team("team-paris", "Infra Paris", "France", "Paris");
        collab(paris, "FR001", "Sébastien Katz",   "+33 6 32 61 36 78");
        collab(paris, "FR002", "Frédéric Dalcros", "+33 6 22 66 44 03");
        collab(paris, "FR003", "Belall Bouhine",   "");

        Team dreux = team("team-dreux", "Infra Dreux", "France", "Dreux");
        collab(dreux, "FR004", "Guillaume Bédard", "+33 1 28 15 64");
        collab(dreux, "FR005", "Raphaël Raut",     "+33 11 08 13");

        Team lyon = team("team-lyon", "Infra Lyon", "France", "Lyon");
        collab(lyon, "FR006", "Kévin Laforge", "+33 6 09 53 00 21");

        Team marseille = team("team-marseille", "Infra Marseille", "France", "Marseille");
        collab(marseille, "FR007", "Nerhad Dilki", "+33 6 28 090 306");

        Team chartres = team("team-chartres", "Infra Chartres", "France", "Chartres");
        collab(chartres, "FR008", "Noran Liguillant", "+33 6 34 55 69 18");

        Team amiens = team("team-amiens", "Infra Amiens", "France", "Amiens");
        collab(amiens, "FR009", "Enzo De Souza Jeronimo", "");
        collab(amiens, "FR010", "Gerniose Didier",        "");
    }

    // ─── ASTREINTES: S01 (Jan 5) → S13 (Mar 30) — weekly rotation ────────────

    private void seedAstreintes() {
        // {agentName, gsm, planned}
        String[][] mada2   = {
            {"Jacky",   "261341464994", "true"},
            {"David",   "261341465029", "true"},
            {"Romy",    "261341465028", "true"},
            {"Nanja",   "261341465047", "true"},
            {"Nilaina", "261383458679", "true"}
        };
        String[][] mada1   = {
            {"Eddy",       "261341464990", "true"},
            {"Nantenaina", "261341465003", "true"},
            {"Mathieu",    "261341464989", "true"},
            {"Erica",      "261341464989", "true"},
            {"Tojo",       "261341465004", "true"}
        };
        String[][] ebene   = {
            {"Mitch Green",     "23058402192", "true"},
            {"Rodney Duchesne", "23059876917", "true"}
        };
        String[][] thies   = {
            {"Papa El Hadj Moussa Thiam", "0022177537730", "true"},
            {"El Hadj Malick Diop",       "0022178462180", "true"}
        };
        String[][] pointe  = {
            {"Cheikh Samb (D00470)",  "0022177537742", "true"},
            {"Ibrahim Faye (D04102)", "0022178588030", "false"}
        };
        String[][] paris   = {
            {"Sébastien Katz",   "+33 6 32 61 36 78", "true"},
            {"Frédéric Dalcros", "+33 6 22 66 44 03", "true"},
            {"Belall Bouhine",   "",                  "false"}
        };
        String[][] abidjan = {
            {"Tia Curiae",      "0787010180", "true"},
            {"Sorho Dognon",    "0787010123", "true"},
            {"YOMANN Emmanuel", "0787010124", "true"}
        };

        LocalDate week = LocalDate.of(2026, 1, 5);   // S01 — first Monday of 2026
        LocalDate last = LocalDate.of(2026, 3, 30);  // S13 — last Monday in March
        int i = 0;

        while (!week.isAfter(last)) {
            ast("team-mada2",   "Madagascar", "🇲🇬", "MADA2 Crystal", mada2[i   % mada2.length],   week);
            ast("team-mada1",   "Madagascar", "🇲🇬", "MADA1 Futura",  mada1[i   % mada1.length],   week);
            ast("team-ebene",   "Maurice",    "🇲🇺", "Ebene",          ebene[i   % ebene.length],   week);
            ast("team-thies",   "Sénégal",    "🇸🇳", "Thies",          thies[i   % thies.length],   week);
            ast("team-pointe",  "Sénégal",    "🇸🇳", "SN Point E",     pointe[i  % pointe.length],  week);
            ast("team-paris",   "France",     "🇫🇷", "Paris",          paris[i   % paris.length],   week);
            ast("team-abidjan", "Abidjan",    "🇨🇮", "Abidjan IT",     abidjan[i % abidjan.length], week);
            week = week.plusWeeks(1);
            i++;
        }
    }

    private void ast(String teamId, String country, String flag, String site, String[] a, LocalDate w) {
        astreinte(teamId, country, flag, site, a[0], a[1], "true".equals(a[2]), w);
    }

    // ─── PLANNING: Jan 5 → Mar 31 — fixed weekly patterns, repeated ──────────

    private void seedPlanning() {
        // Fixed shift patterns per collaborator: Mon–Sun (index 0=Mon … 6=Sun)

        // team-mada2
        String[] jacky   = {"10:00–19:00", "10:00–19:00", "10:00–19:00", "OFF",         "10:00–19:00", "AS 19:00–04:00", "OFF"        };
        String[] david   = {"07:00–16:00", "OFF",          "07:00–16:00", "07:00–16:00", "OFF",         "09:00–18:00",    "16:00–01:00"};
        String[] romy    = {"OFF",          "07:00–16:00", "10:00–19:00", "10:00–19:00", "07:00–16:00", "OFF",            "09:00–18:00"};
        String[] nanja   = {"19:00–04:00", "19:00–04:00",  "19:00–04:00", "19:00–04:00", "19:00–04:00", "OFF",            "OFF"        };
        String[] nilaina = {"08:00–17:00", "08:00–17:00",  "08:00–17:00", "08:00–17:00", "08:00–17:00", "OFF",            "OFF"        };

        // team-mada1
        String[] eddy       = {"10:00–19:00", "10:00–19:00", "10:00–19:00", "07:00–16:00", "10:00–19:00", "OFF",         "OFF"};
        String[] nantenaina = {"07:00–16:00", "07:00–16:00", "07:00–16:00", "OFF",          "07:00–16:00", "12:00–21:00", "OFF"};
        String[] mathieu    = {"15:00–00:00", "15:00–00:00", "15:00–00:00", "15:00–00:00",  "15:00–00:00", "OFF",         "OFF"};
        String[] erica      = {"12:00–21:00", "12:00–21:00", "OFF",          "10:00–19:00", "12:00–21:00", "09:00–18:00", "OFF"};
        String[] tojo       = {"08:00–17:00", "08:00–17:00", "08:00–17:00", "08:00–17:00",  "08:00–17:00", "OFF",         "OFF"};

        // team-ebene
        String[] mitch  = {"09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "AS 09:00–18:00", "AS 09:00–18:00"};
        String[] rodney = {"15:00–01:00", "15:00–01:00", "15:00–01:00", "15:00–01:00", "15:00–01:00", "OFF",            "OFF"           };

        // team-abidjan
        String[] tia    = {"07:00–15:00", "07:00–15:00", "07:00–15:00", "07:00–15:00", "07:00–15:00", "OFF", "OFF"};
        String[] sorho  = {"10:00–18:00", "10:00–18:00", "10:00–18:00", "10:00–18:00", "10:00–18:00", "OFF", "OFF"};
        String[] yomann = {"08:00–17:00", "08:00–17:00", "08:00–17:00", "08:00–17:00", "08:00–17:00", "OFF", "OFF"};

        // team-thies
        String[] papa   = {"07:00–16:00", "07:00–16:00", "07:00–16:00", "07:00–16:00", "07:00–16:00", "09:00–17:00", "OFF"};
        String[] malick = {"10:00–19:00", "10:00–19:00", "10:00–19:00", "10:00–19:00", "10:00–19:00", "OFF",         "OFF"};

        // team-pointe
        String[] cheikh  = {"10:00–19:00", "10:00–19:00", "10:00–19:00", "10:00–19:00", "09:00–17:00", "OFF", "OFF"};
        String[] ibrahim = {"11:00–20:00", "11:00–20:00", "11:00–20:00", "11:00–20:00", "11:00–20:00", "OFF", "OFF"};

        // team-paris
        String[] sebastien = {"09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "AS 09:00–18:00", "OFF"};
        String[] frederic  = {"09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "OFF",            "OFF"};
        String[] belall    = {"09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "09:00–18:00", "OFF",            "OFF"};

        LocalDate monday = LocalDate.of(2026, 1, 5);
        LocalDate end    = LocalDate.of(2026, 3, 31);

        while (!monday.isAfter(end)) {
            planWeek("T00157", "Jacky",   "team-mada2", monday, jacky);
            planWeek("T06442", "David",   "team-mada2", monday, david);
            planWeek("T08453", "Romy",    "team-mada2", monday, romy);
            planWeek("T09235", "Nanja",   "team-mada2", monday, nanja);
            planWeek("T08401", "Nilaina", "team-mada2", monday, nilaina);

            planWeek("T00851", "Eddy",       "team-mada1", monday, eddy);
            planWeek("T07419", "Nantenaina", "team-mada1", monday, nantenaina);
            planWeek("T07872", "Mathieu",    "team-mada1", monday, mathieu);
            planWeek("T07176", "Erica",      "team-mada1", monday, erica);
            planWeek("T08504", "Tojo",       "team-mada1", monday, tojo);

            planWeek("R00008", "Mitch Green",     "team-ebene", monday, mitch);
            planWeek("R01676", "Rodney Duchesne", "team-ebene", monday, rodney);

            planWeek("A00062", "Tia Curiae",      "team-abidjan", monday, tia);
            planWeek("A00278", "Sorho Dognon",    "team-abidjan", monday, sorho);
            planWeek("A01734", "YOMANN Emmanuel", "team-abidjan", monday, yomann);

            planWeek("SN001", "Papa El Hadj Moussa Thiam", "team-thies", monday, papa);
            planWeek("SN002", "El Hadj Malick Diop",       "team-thies", monday, malick);

            planWeek("SN003", "Cheikh Samb (D00470)",  "team-pointe", monday, cheikh);
            planWeek("SN004", "Ibrahim Faye (D04102)", "team-pointe", monday, ibrahim);

            planWeek("FR001", "Sébastien Katz",   "team-paris", monday, sebastien);
            planWeek("FR002", "Frédéric Dalcros", "team-paris", monday, frederic);
            planWeek("FR003", "Belall Bouhine",   "team-paris", monday, belall);

            monday = monday.plusWeeks(1);
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private Team team(String id, String name, String country, String site) {
        Team t = new Team();
        t.setId(id);
        t.setName(name);
        t.setCountry(country);
        t.setSite(site);
        return teamRepo.save(t);
    }

    private void collab(Team team, String id, String name, String gsm) {
        Collaborator c = new Collaborator();
        c.setId(id);
        c.setName(name);
        c.setGsm(gsm);
        c.setTeam(team);
        collaboratorRepo.save(c);
    }

    private void astreinte(String teamId, String country, String flag, String site,
                           String agentName, String gsm, boolean planned, LocalDate weekDate) {
        Team team = teamRepo.findById(teamId).orElse(null);
        Astreinte a = new Astreinte();
        a.setTeam(team);
        a.setCountry(country);
        a.setFlag(flag);
        a.setSite(site);
        a.setAgentName(agentName);
        a.setGsm(gsm);
        a.setPlanned(planned);
        a.setWeekDate(weekDate);
        astreinteRepo.save(a);
    }

    private void planWeek(String collabId, String name, String teamId,
                          LocalDate monday, String[] pattern) {
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            Planning p = new Planning();
            p.setCollaboratorId(collabId);
            p.setCollaboratorName(name);
            p.setTeamId(teamId);
            p.setDay(day);
            p.setShiftLabel(pattern[i]);
            p.setShiftType(shiftType(pattern[i]));
            planningRepo.save(p);
        }
    }

    private String shiftType(String label) {
        if ("OFF".equals(label))     return "off";
        if (label.startsWith("AS ")) return "astreinte";
        return "work";
    }
}
