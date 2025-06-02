package com.knemis.skyblock.skyblockcoreproject.teams.placeholders;

import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.managers.TeamManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Placeholders<T extends Team, U extends keviinUser<T>> {
    private final keviinTeams<T, U> keviinTeams;

    public Placeholders(keviinTeams<T, U> keviinTeams) {
        this.keviinTeams = keviinTeams;
    }

    public List<Placeholder> getDefaultPlaceholders() {
        return keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(Optional.empty());
    }

    public List<Placeholder> getPlaceholders(@Nullable Player player) {
        U user = player == null ? null : keviinTeams.getUserManager().getUser(player);
        Optional<T> team = user == null ? Optional.empty() : keviinTeams.getTeamManager().getTeamViaID(user.getTeamID());
        Optional<T> current = user == null ? Optional.empty() : keviinTeams.getTeamManager().getTeamViaPlayerLocation(player);
        List<T> topValue = keviinTeams.getTeamManager().getTeams(TeamManager.SortType.Value, true);
        List<T> topExperience = keviinTeams.getTeamManager().getTeams(TeamManager.SortType.Experience, true);

        List<Placeholder> placeholders = new ArrayList<>();

        placeholders.addAll(keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(team));
        placeholders.addAll(keviinTeams.getUserPlaceholderBuilder().getPlaceholders(Optional.ofNullable(user)));
        for (Placeholder placeholder : keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(current)) {
            placeholders.add(new Placeholder("current_" + formatPlaceholderKey(placeholder.getKey()), placeholder.getValue()));
        }

        for (int i = 1; i <= 20; i++) {
            Optional<T> value = topValue.size() >= i ? Optional.of(topValue.get(i - 1)) : Optional.empty();
            Optional<T> experience = topExperience.size() >= i ? Optional.of(topExperience.get(i - 1)) : Optional.empty();
            for (Placeholder placeholder : keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(value)) {
                placeholders.add(new Placeholder("top_value_" + i + "_" + formatPlaceholderKey(placeholder.getKey()), placeholder.getValue()));
            }
            for (Placeholder placeholder : keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(experience)) {
                placeholders.add(new Placeholder("top_experience_" + i + "_" + formatPlaceholderKey(placeholder.getKey()), placeholder.getValue()));
            }
        }

        return placeholders;
    }

    private String formatPlaceholderKey(String key) {
        return key.replace("%", "");
    }

}
