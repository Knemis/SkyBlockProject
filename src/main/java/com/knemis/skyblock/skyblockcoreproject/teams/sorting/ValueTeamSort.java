package com.knemis.skyblock.skyblockcoreproject.teams.sorting;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ValueTeamSort<T extends Team> extends TeamSorting<T> {

    public ValueTeamSort(Item item) {
        this.item = item;
        this.enabled = true;
    }

    @Override
    public List<T> getSortedTeams(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        return SkyBlockProjectTeams.getTeamManager().getTeams().stream()
                .sorted(Comparator.comparing(T::getValue).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "Value";
    }

    @Override
    public double getValue(T team) {
        return team.getValue();
    }
}
