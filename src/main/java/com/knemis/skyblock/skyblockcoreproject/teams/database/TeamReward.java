package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.knemis.skyblock.skyblockcoreproject.teams.Reward;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_rewards")
public class TeamReward extends com.knemis.skyblock.skyblockcoreproject.teams.database.TeamData { // TODO: Ensure TeamData is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, uniqueCombo = true)
    private int id;

    @DatabaseField(columnName = "reward", canBeNull = false, width = 2048)
    private com.knemis.skyblock.skyblockcoreproject.teams.Reward reward; // TODO: Update Reward to actual class

    public TeamReward(@NotNull com.knemis.skyblock.skyblockcoreproject.teams.database.Team team, com.knemis.skyblock.skyblockcoreproject.teams.Reward reward) { // TODO: Update Team and Reward to actual classes
        super(team);
        this.reward = reward;
    }
}
