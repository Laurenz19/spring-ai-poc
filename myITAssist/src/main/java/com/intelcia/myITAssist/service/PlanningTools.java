package com.intelcia.myITAssist.service;

import com.intelcia.myITAssist.model.Collaborator;
import com.intelcia.myITAssist.model.Planning;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlanningTools {

    private final AstreinteService astreinteService;
    private final PlanningService planningService;
    private final CollaboratorService collaboratorService;
    private final TeamService teamService;

    @Tool(description = "Get on-call agents (astreinte) for a country on the current week. If no country is specified, returns all countries.")
    public String getAstreinte(String country) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        var list = (country == null || country.isBlank())
            ? astreinteService.findByWeek(monday)
            : astreinteService.findByCountryAndWeek(country, monday);

        if (list.isEmpty()) {
            String suffix = (country != null && !country.isBlank()) ? " (" + country + ")" : "";
            return "Aucune astreinte trouvée pour la semaine du " + monday + suffix;
        }

        return list.stream()
            .map(a -> a.getFlag() + " " + a.getAgentName()
                + " (" + (a.getTeam() != null ? a.getTeam().getName() : a.getSite()) + ")"
                + " · GSM: " + a.getGsm()
                + " · " + (a.isPlanned() ? "Planifiée" : "Non planifiée"))
            .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get all agents who are OFF (day off) during the current week")
    public String getOffAgents() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        var offs = planningService.findOffAgentsByWeek(monday);

        if (offs.isEmpty()) return "Aucun agent OFF cette semaine.";

        return offs.stream()
            .map(p -> p.getCollaboratorName() + " · OFF le " + p.getDay())
            .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get the GSM contact of an agent by name")
    public String getContact(String name) {
        return collaboratorService.findByName(name)
            .map(c -> c.getName() + " · GSM: " + c.getGsm()
                + " (" + c.getTeam().getName() + ")")
            .orElse("Agent introuvable : " + name);
    }

    @Tool(description = "Get today's planning for a specific team by its name")
    public String getTeamPlanning(String teamName) {
        List<com.intelcia.myITAssist.model.Team> teams = teamService.findByName(teamName);
        if (teams.isEmpty()) return "Équipe non trouvée : " + teamName;

        return teams.stream()
            .map(team -> {
                var entries = planningService.findByTeamAndDay(team.getId(), LocalDate.now());
                if (entries.isEmpty()) return "Aucun planning pour " + team.getName() + " aujourd'hui.";
                return "**" + team.getName() + "**\n" + entries.stream()
                    .map(p -> p.getCollaboratorName() + " : " + p.getShiftLabel())
                    .collect(Collectors.joining("\n"));
            })
            .collect(Collectors.joining("\n\n"));
    }

    @Tool(description = "List all teams and their members for a given country")
    public String getTeamsAndTeamMemberByCountry(String country) {
        var teams = teamService.findByCountry(country);
        if (teams.isEmpty()) return "Aucune équipe trouvée pour le pays : " + country;

        return teams.stream()
            .map(t -> t.getName() + " : " + t.getCollaborators().stream()
                .map(Collaborator::getName)
                .collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));
    }

    @Tool(description = "List teams and members filtered by both country and team name")
    public String getTeamsAndTeamMemberByCountryAndTeamName(String country, String teamName) {
        var teams = teamService.findByCountryAndName(country, teamName);
        if (teams.isEmpty()) return "Aucune équipe trouvée pour " + country + " / " + teamName;

        return teams.stream()
            .map(t -> t.getName() + " : " + t.getCollaborators().stream()
                .map(Collaborator::getName)
                .collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get the full weekly planning of all teams. Provide any date within the target week in ISO format YYYY-MM-DD (e.g. 2026-01-05).")
    public String getPlanningOfAllTeamsByWeek(LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        var weekPlan = planningService.findByWeek(monday);
        if (weekPlan.isEmpty()) return "Aucun planning trouvé pour la semaine du " + monday;

        Map<String, String> teamNames = teamService.buildIdToNameMap();
        String[] dayLabels = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

        Map<String, Map<String, Map<Integer, String>>> grouped = weekPlan.stream()
            .collect(Collectors.groupingBy(
                Planning::getTeamId,
                Collectors.groupingBy(
                    Planning::getCollaboratorName,
                    Collectors.toMap(p -> p.getDay().getDayOfWeek().getValue(), Planning::getShiftLabel)
                )
            ));

        StringBuilder sb = new StringBuilder();
        sb.append("Semaine du ").append(monday).append(" au ").append(sunday).append("\n\n");

        grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(teamEntry -> {
                String teamName = teamNames.getOrDefault(teamEntry.getKey(), teamEntry.getKey());
                sb.append("### ").append(teamName).append("\n");

                teamEntry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(memberEntry -> {
                        String shifts = IntStream.rangeClosed(1, 7)
                            .mapToObj(i -> dayLabels[i - 1] + ": " + memberEntry.getValue().getOrDefault(i, "—"))
                            .collect(Collectors.joining(" | "));
                        sb.append("**").append(memberEntry.getKey()).append("** → ").append(shifts).append("\n");
                    });
                sb.append("\n");
            });

        return sb.toString();
    }
}
