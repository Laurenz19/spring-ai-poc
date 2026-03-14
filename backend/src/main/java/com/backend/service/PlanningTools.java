package com.backend.service;

import com.backend.model.Collaborator;
import com.backend.model.Planning;
import com.backend.model.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PlanningTools {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);

    private final AstreinteService astreinteService;
    private final PlanningService planningService;
    private final CollaboratorService collaboratorService;
    private final TeamService teamService;

    // ─── ASTREINTE ────────────────────────────────────────────────────────────

    @Tool(description = "Get on-call agents (astreinte) for the current week. Optionally filter by country.")
    public String getAstreinte(
        @ToolParam(required = false, description = "Country name (e.g. 'Madagascar', 'France'). Omit for all countries.") String country) {

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

    // ─── PLANNING FLEXIBLE ────────────────────────────────────────────────────

    @Tool(description = """
        Main planning query tool. Supports any combination of filters.
        Use for: who is OFF today/this week, schedule of a team or person, planning by country or shift,
        weekly/monthly overview, night shifts, who works on a specific date.
        If no date is given, defaults to today. For a full week use from=monday to=sunday.
        shiftType values: 'off' (day off), 'work' (normal), 'astreinte' (on-call).
        shiftKeyword searches inside the shift label (e.g. '19:00' for evening shifts, 'AS' for astreinte shifts).
        """)
    public String getPlanning(
        @ToolParam(required = false, description = "Start date ISO YYYY-MM-DD. Defaults to today.") LocalDate from,
        @ToolParam(required = false, description = "End date ISO YYYY-MM-DD. Defaults to same as from (single day).") LocalDate to,
        @ToolParam(required = false, description = "Team name, partial match (e.g. 'MADA2', 'Ebene', 'Paris').") String teamName,
        @ToolParam(required = false, description = "Country (e.g. 'Madagascar', 'France', 'Maurice', 'Sénégal', 'Abidjan').") String country,
        @ToolParam(required = false, description = "Collaborator name, partial match (e.g. 'Jacky', 'David').") String collaboratorName,
        @ToolParam(required = false, description = "Shift type filter: 'off', 'work', 'astreinte'. Omit for all shifts.") String shiftType,
        @ToolParam(required = false, description = "Keyword in shift label, e.g. '19:00', 'AS', '00:00'. Useful for night/evening shift queries.") String shiftKeyword
    ) {
        LocalDate effectiveFrom = (from != null) ? from : LocalDate.now();
        LocalDate effectiveTo   = (to   != null) ? to   : effectiveFrom;

        // Resolve team IDs from teamName and/or country
        List<String> teamIds = resolveTeamIds(teamName, country);

        List<Planning> entries = planningService.findByFilters(
            effectiveFrom, effectiveTo, teamIds, collaboratorName, shiftType, shiftKeyword
        );

        if (entries.isEmpty()) {
            return buildEmptyMessage(effectiveFrom, effectiveTo, teamName, country, collaboratorName, shiftType);
        }

        Map<String, String> teamNames = teamService.buildIdToNameMap();
        long days = effectiveTo.toEpochDay() - effectiveFrom.toEpochDay() + 1;

        // Single day → group by team, flat list
        if (days == 1) {
            return formatSingleDay(entries, teamNames, effectiveFrom);
        }

        // Period ≤ 7 days → weekly grid per team
        if (days <= 7) {
            return formatWeeklyGrid(entries, teamNames, effectiveFrom, effectiveTo);
        }

        // Longer period → summary by collaborator
        return formatPeriodSummary(entries, teamNames, effectiveFrom, effectiveTo);
    }

    // ─── CONTACT ──────────────────────────────────────────────────────────────

    @Tool(description = "Get the GSM contact of an agent by name.")
    public String getContact(String name) {
        return collaboratorService.findByName(name)
            .map(c -> c.getName() + " · GSM: " + c.getGsm()
                + " (" + c.getTeam().getName() + ")")
            .orElse("Agent introuvable : " + name);
    }

    // ─── TEAMS ────────────────────────────────────────────────────────────────

    @Tool(description = "List teams and their members. Optional filters by country and team name.")
    public String getTeams(
        @ToolParam(required = false, description = "Country name (e.g. 'Madagascar', 'France')") String country,
        @ToolParam(required = false, description = "Team name, partial match (e.g. 'MADA2')") String teamName
    ) {
        List<Team> teams;
        if (country != null && !country.isBlank() && teamName != null && !teamName.isBlank()) {
            teams = teamService.findByCountryAndName(country, teamName);
        } else if (country != null && !country.isBlank()) {
            teams = teamService.findByCountry(country);
        } else if (teamName != null && !teamName.isBlank()) {
            teams = teamService.findByName(teamName);
        } else {
            return "Veuillez préciser un pays ou un nom d'équipe pour lister les membres.";
        }

        if (teams.isEmpty()) {
            return "Aucune équipe trouvée avec les filtres fournis.";
        }

        return teams.stream()
            .map(t -> t.getName() + " (" + t.getCountry() + ") : " + t.getCollaborators().stream()
                .map(Collaborator::getName)
                .collect(Collectors.joining(", ")))
            .collect(Collectors.joining("\n"));
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    /** Resolve team IDs from optional teamName and/or country filters. Returns null if no filter. */
    private List<String> resolveTeamIds(String teamName, String country) {
        if ((teamName == null || teamName.isBlank()) && (country == null || country.isBlank())) {
            return null; // no filter
        }

        List<Team> teams;
        if (teamName != null && !teamName.isBlank() && country != null && !country.isBlank()) {
            teams = teamService.findByCountryAndName(country, teamName);
        } else if (teamName != null && !teamName.isBlank()) {
            teams = teamService.findByName(teamName);
        } else {
            teams = teamService.findByCountry(country);
        }

        return teams.stream().map(Team::getId).collect(Collectors.toList());
    }

    /** Single-day output: grouped by team */
    private String formatSingleDay(List<Planning> entries, Map<String, String> teamNames, LocalDate day) {
        StringBuilder sb = new StringBuilder("Planning du " + day.format(DAY_FMT) + " :\n\n");

        entries.stream()
            .collect(Collectors.groupingBy(Planning::getTeamId, LinkedHashMap::new, Collectors.toList()))
            .forEach((teamId, list) -> {
                sb.append("▸ ").append(teamNames.getOrDefault(teamId, teamId)).append("\n");
                list.stream()
                    .sorted(Comparator.comparing(Planning::getCollaboratorName))
                    .forEach(p -> sb.append("  ").append(padRight(p.getCollaboratorName(), 14))
                        .append(" → ").append(p.getShiftLabel()).append("\n"));
                sb.append("\n");
            });

        return sb.toString().trim();
    }

    /** Weekly grid: team → collaborator → Mon|Tue|...|Sun */
    private String formatWeeklyGrid(List<Planning> entries, Map<String, String> teamNames,
                                    LocalDate from, LocalDate to) {
        long days = to.toEpochDay() - from.toEpochDay() + 1;
        String[] dayLabels = IntStream.range(0, (int) days)
            .mapToObj(i -> from.plusDays(i).format(DAY_FMT))
            .toArray(String[]::new);

        StringBuilder sb = new StringBuilder(
            "Planning du " + from.format(DAY_FMT) + " au " + to.format(DAY_FMT) + " :\n\n");

        // group: teamId → collaboratorName → dayOffset → shiftLabel
        Map<String, Map<String, Map<Long, String>>> grouped = entries.stream()
            .collect(Collectors.groupingBy(
                Planning::getTeamId,
                LinkedHashMap::new,
                Collectors.groupingBy(
                    Planning::getCollaboratorName,
                    LinkedHashMap::new,
                    Collectors.toMap(
                        p -> p.getDay().toEpochDay() - from.toEpochDay(),
                        Planning::getShiftLabel,
                        (a, b) -> a
                    )
                )
            ));

        grouped.forEach((teamId, members) -> {
            sb.append("### ").append(teamNames.getOrDefault(teamId, teamId)).append("\n");
            members.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String shifts = IntStream.range(0, (int) days)
                        .mapToObj(i -> dayLabels[i] + ": " + e.getValue().getOrDefault((long) i, "—"))
                        .collect(Collectors.joining(" | "));
                    sb.append("**").append(e.getKey()).append("** → ").append(shifts).append("\n");
                });
            sb.append("\n");
        });

        return sb.toString().trim();
    }

    /** Period summary: list with date + shift per line, grouped by collaborator */
    private String formatPeriodSummary(List<Planning> entries, Map<String, String> teamNames,
                                       LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder(
            "Planning du " + from + " au " + to + " :\n\n");

        entries.stream()
            .collect(Collectors.groupingBy(Planning::getCollaboratorName, TreeMap::new, Collectors.toList()))
            .forEach((name, list) -> {
                String teamLabel = teamNames.getOrDefault(list.get(0).getTeamId(), list.get(0).getTeamId());
                sb.append("**").append(name).append("** (").append(teamLabel).append(")\n");
                list.stream()
                    .sorted(Comparator.comparing(Planning::getDay))
                    .forEach(p -> sb.append("  ").append(p.getDay().format(DAY_FMT))
                        .append(" → ").append(p.getShiftLabel()).append("\n"));
                sb.append("\n");
            });

        return sb.toString().trim();
    }

    private String buildEmptyMessage(LocalDate from, LocalDate to,
                                     String teamName, String country,
                                     String collaboratorName, String shiftType) {
        List<String> filters = new ArrayList<>();
        if (teamName != null && !teamName.isBlank())        filters.add("équipe '" + teamName + "'");
        if (country != null && !country.isBlank())          filters.add("pays '" + country + "'");
        if (collaboratorName != null && !collaboratorName.isBlank()) filters.add("agent '" + collaboratorName + "'");
        if (shiftType != null && !shiftType.isBlank())      filters.add("type '" + shiftType + "'");

        String period = from.equals(to) ? "le " + from : "du " + from + " au " + to;
        String filterStr = filters.isEmpty() ? "" : " [" + String.join(", ", filters) + "]";
        return "Aucun planning trouvé " + period + filterStr + ".";
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
