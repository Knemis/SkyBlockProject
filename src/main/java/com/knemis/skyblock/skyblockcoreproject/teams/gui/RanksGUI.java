package com.knemis.skyblock.skyblockcoreproject.teams.gui;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.UserRank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RanksGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;

    public RanksGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().ranksGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.team = team;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().ranksGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().ranksGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (UserRank userRank : SkyBlockProjectTeams.getUserRanks().values()) {
            inventory.setItem(userRank.item.slot, ItemStackUtils.makeItem(userRank.item));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        for (Map.Entry<Integer, UserRank> userRank : SkyBlockProjectTeams.getUserRanks().entrySet()) {
            if (event.getSlot() != userRank.getValue().item.slot) continue;
            event.getWhoClicked().openInventory(new PermissionsGUI<>(team, userRank.getKey(), (Player) event.getWhoClicked(), SkyBlockProjectTeams).getInventory());
            return;
        }
    }
}
