package com.knemis.skyblock.skyblockcoreproject.teams.sorting;

import com.keviin.keviincore.Item;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.Team;
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
    public List<T> getSortedTeams(keviinTeams<T, ?> keviinTeams) {
        return keviinTeams.getTeamManager().getTeams().stream()
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
