package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_permissions")
public final class TeamPermission extends com.knemis.skyblock.skyblockcoreproject.teams.database.TeamData { // TODO: Ensure TeamData is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id;

    @DatabaseField(columnName = "permission", canBeNull = false, uniqueCombo = true)
    private String permission;

    @DatabaseField(columnName = "rank", canBeNull = false)
    private int rank;

    @DatabaseField(columnName = "allowed", canBeNull = false)
    private boolean allowed;

    public TeamPermission(com.knemis.skyblock.skyblockcoreproject.teams.database.Team team, String permission, int rank, boolean allowed) { // TODO: Update Team to actual class
        super(team);
        this.permission = permission;
        this.rank = rank;
        this.allowed = allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
        setChanged(true);
    }
}
