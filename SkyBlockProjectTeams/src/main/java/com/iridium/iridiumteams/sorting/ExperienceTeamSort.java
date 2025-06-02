package com.keviin.keviinteams.sorting;

import com.keviin.keviincore.Item;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;

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
    public List<T> getSortedTeams(keviinTeams<T, ?> keviinTeams) {
        return keviinTeams.getTeamManager().getTeams().stream()
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
