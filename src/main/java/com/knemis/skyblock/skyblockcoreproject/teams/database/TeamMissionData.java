package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_mission_data")
public class TeamMissionData extends com.knemis.skyblock.skyblockcoreproject.teams.database.DatabaseObject{ // TODO: Ensure DatabaseObject is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id;

    @DatabaseField(columnName = "mission_id", uniqueCombo = true)
    private int missionID;

    @DatabaseField(columnName = "mission_index", uniqueCombo = true)
    private int missionIndex;

    @DatabaseField(columnName = "progress")
    private int progress;

    public TeamMissionData(com.knemis.skyblock.skyblockcoreproject.teams.database.TeamMission teamMission, int missionIndex) { // TODO: Update TeamMission to actual class
        this.missionID = teamMission.getId();
        this.missionIndex = missionIndex;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        setChanged(true);
    }
}
