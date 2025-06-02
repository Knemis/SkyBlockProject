package com.knemis.skyblock.skyblockcoreproject.teams.sorting;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
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

    public abstract List<T> getSortedTeams(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams);

    public abstract String getName();
    public abstract double getValue(T team);

    public int getRank(T team, SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        List<T> teams = getSortedTeams(SkyBlockProjectTeams);
        return teams.indexOf(team) + 1;
    }
}
