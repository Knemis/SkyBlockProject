package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.cryptomorin.xseries.XMaterial;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "team_blocks")
public class TeamBlock extends com.knemis.skyblock.skyblockcoreproject.teams.database.TeamData { // TODO: Ensure TeamData is correctly referenced or imported

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id;

    @DatabaseField(columnName = "block", uniqueCombo = true)
    private XMaterial xMaterial;

    @DatabaseField(columnName = "amount", canBeNull = false)
    private int amount;

    public TeamBlock(@NotNull com.knemis.skyblock.skyblockcoreproject.teams.database.Team team, XMaterial xMaterial, int amount) { // TODO: Update Team to actual class
        super(team);
        this.xMaterial = xMaterial;
        this.amount = amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        setChanged(true);
    }
}
