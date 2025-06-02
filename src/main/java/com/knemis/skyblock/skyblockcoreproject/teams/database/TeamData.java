package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TeamData extends com.knemis.skyblock.skyblockcoreproject.teams.database.DatabaseObject{ // TODO: Ensure DatabaseObject is correctly referenced or imported
    @DatabaseField(columnName = "team_id", canBeNull = false, uniqueCombo = true)
    private int teamID;

    public TeamData(com.knemis.skyblock.skyblockcoreproject.teams.database.Team team) { // TODO: Update Team to actual class
        this.teamID = team.getId();
    }
}
