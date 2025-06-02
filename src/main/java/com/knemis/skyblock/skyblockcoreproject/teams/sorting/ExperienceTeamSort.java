package com.knemis.skyblock.skyblockcoreproject.teams.sorting;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import lombok.NoArgsConstructor;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ExperienceTeamSort<T extends Team> extends TeamSorting<T> {

    public ExperienceTeamSort(Item item) {
        this.item = item;
        this.enabled = true;
    }

    @Override
    public List<T> getSortedTeams(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        return SkyBlockProjectTeams.getTeamManager().getTeams().stream()
                .sorted(Comparator.comparing(T::getExperience).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "Experience";
    }

    @Override
    public double getValue(T team) {
        return team.getExperience();
    }
}
