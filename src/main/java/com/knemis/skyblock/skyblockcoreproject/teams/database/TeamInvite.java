package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_invites")
public class TeamInvite extends com.knemis.skyblock.skyblockcoreproject.teams.database.TeamData { // TODO: Ensure TeamData is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id;
    @DatabaseField(columnName = "user", canBeNull = false, uniqueCombo = true)
    private UUID user;

    @DatabaseField(columnName = "inviter", canBeNull = false)
    private UUID invitee;

    @DatabaseField(columnName = "time", canBeNull = false)
    private LocalDateTime time;

    public TeamInvite(com.knemis.skyblock.skyblockcoreproject.teams.database.Team team, UUID user, UUID invitee) { // TODO: Update Team to actual class
        super(team);
        this.user = user;
        this.invitee = invitee;
        this.time = LocalDateTime.now();
    }
}
