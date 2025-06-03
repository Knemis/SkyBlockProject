package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class RanksGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;
    private Player player; // Added player field

    public RanksGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().ranksGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.team = team;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().ranksGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().ranksGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, SkyBlockProjectTeams.getInventories().ranksGUI.size, "Ranks GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (UserRank userRank : SkyBlockProjectTeams.getUserRanks().values()) { // TODO: Uncomment when getUserRanks is available
            // inventory.setItem(userRank.item.slot, ItemStackUtils.makeItem(userRank.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<Integer, UserRank> userRank : SkyBlockProjectTeams.getUserRanks().entrySet()) { // TODO: Uncomment when getUserRanks is available
            // if (event.getSlot() != userRank.getValue().item.slot) continue;
            // event.getWhoClicked().openInventory(new PermissionsGUI<>(team, userRank.getKey(), (Player) event.getWhoClicked(), SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when PermissionsGUI is refactored
            // return;
        // }
    }
}
