package com.knemis.skyblock.skyblockcoreproject.teams.placeholders;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Placeholders<T extends Team, U extends SkyBlockProjectTeamsUser<T>> {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public Placeholders(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    public List<Placeholder> getDefaultPlaceholders() {
        return SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(Optional.empty());
    }

    public List<Placeholder> getPlaceholders(@Nullable Player player) {
        U user = player == null ? null : SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> team = user == null ? Optional.empty() : SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());
        Optional<T> current = user == null ? Optional.empty() : SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player);
        List<T> topValue = SkyBlockProjectTeams.getTeamManager().getTeams(TeamManager.SortType.Value, true);
        List<T> topExperience = SkyBlockProjectTeams.getTeamManager().getTeams(TeamManager.SortType.Experience, true);

        List<Placeholder> placeholders = new ArrayList<>();

        placeholders.addAll(SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team));
        placeholders.addAll(SkyBlockProjectTeams.getUserPlaceholderBuilder().getPlaceholders(Optional.ofNullable(user)));
        for (Placeholder placeholder : SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(current)) {
            placeholders.add(new Placeholder("current_" + formatPlaceholderKey(placeholder.getKey()), placeholder.getValue()));
        }

        for (int i = 1; i <= 20; i++) {
            Optional<T> value = topValue.size() >= i ? Optional.of(topValue.get(i - 1)) : Optional.empty();
            Optional<T> experience = topExperience.size() >= i ? Optional.of(topExperience.get(i - 1)) : Optional.empty();
            for (Placeholder placeholder : SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(value)) {
                placeholders.add(new Placeholder("top_value_" + i + "_" + formatPlaceholderKey(placeholder.getKey()), placeholder.getValue()));
            }
            for (Placeholder placeholder : SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(experience)) {
                placeholders.add(new Placeholder("top_experience_" + i + "_" + formatPlaceholderKey(placeholder.getKey()), placeholder.getValue()));
            }
        }

        return placeholders;
    }

    private String formatPlaceholderKey(String key) {
        return key.replace("%", "");
    }

}
