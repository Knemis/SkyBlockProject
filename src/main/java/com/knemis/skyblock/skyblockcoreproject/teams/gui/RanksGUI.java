package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.UserRank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RanksGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final keviinTeams<T, U> keviinTeams;
    private final T team;

    public RanksGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().ranksGUI.background, player, keviinTeams.getInventories().backButton);
        this.team = team;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, keviinTeams.getInventories().ranksGUI.size, StringUtils.color(keviinTeams.getInventories().ranksGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (UserRank userRank : keviinTeams.getUserRanks().values()) {
            inventory.setItem(userRank.item.slot, ItemStackUtils.makeItem(userRank.item));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        for (Map.Entry<Integer, UserRank> userRank : keviinTeams.getUserRanks().entrySet()) {
            if (event.getSlot() != userRank.getValue().item.slot) continue;
            event.getWhoClicked().openInventory(new PermissionsGUI<>(team, userRank.getKey(), (Player) event.getWhoClicked(), keviinTeams).getInventory());
            return;
        }
    }
}
