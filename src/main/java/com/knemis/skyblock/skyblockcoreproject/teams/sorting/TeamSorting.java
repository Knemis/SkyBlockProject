package com.knemis.skyblock.skyblockcoreproject.teams.sorting;

import com.keviin.keviincore.Item;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class TeamSorting<T extends Team> {

    public Item item;
    public boolean enabled;

    public abstract List<T> getSortedTeams(keviinTeams<T, ?> keviinTeams);

    public abstract String getName();
    public abstract double getValue(T team);

    public int getRank(T team, keviinTeams<T, ?> keviinTeams) {
        List<T> teams = getSortedTeams(keviinTeams);
        return teams.indexOf(team) + 1;
    }
}
