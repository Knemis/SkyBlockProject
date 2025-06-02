package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_trusts")
public class TeamTrust extends com.knemis.skyblock.skyblockcoreproject.teams.database.TeamData { // TODO: Ensure TeamData is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id;
    @DatabaseField(columnName = "user", canBeNull = false, uniqueCombo = true)
    private UUID user;

    @DatabaseField(columnName = "truster", canBeNull = false)
    private UUID truster;

    @DatabaseField(columnName = "time", canBeNull = false)
    private LocalDateTime time;

    public TeamTrust(com.knemis.skyblock.skyblockcoreproject.teams.database.Team team, UUID user, UUID truster) { // TODO: Update Team to actual class
        super(team);
        this.user = user;
        this.truster = truster;
        this.time = LocalDateTime.now();
    }
}
