package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_bank")
public class TeamBank extends com.knemis.skyblock.skyblockcoreproject.teams.database.TeamData { // TODO: Ensure TeamData is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id;

    @DatabaseField(columnName = "bank_item", uniqueCombo = true)
    private String bankItem;

    @DatabaseField(columnName = "number")
    private double number;

    public TeamBank(@NotNull com.knemis.skyblock.skyblockcoreproject.teams.database.Team team, @NotNull String bankItem, double number) { // TODO: Update Team to actual class
        super(team);
        this.bankItem = bankItem;
        this.number = number;
    }

    public void setNumber(double number) {
        this.number = number;
        setChanged(true);
    }
}
