package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class RanksGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI

    private final IridiumTeams<T, U> iridiumTeams;
    private final T team;
    private Player player; // Added player field

    public RanksGUI(T team, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().ranksGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.team = team;
        this.iridiumTeams = iridiumTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, iridiumTeams.getInventories().ranksGUI.size, StringUtils.color(iridiumTeams.getInventories().ranksGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, iridiumTeams.getInventories().ranksGUI.size, "Ranks GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (UserRank userRank : iridiumTeams.getUserRanks().values()) { // TODO: Uncomment when getUserRanks is available
            // inventory.setItem(userRank.item.slot, ItemStackUtils.makeItem(userRank.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<Integer, UserRank> userRank : iridiumTeams.getUserRanks().entrySet()) { // TODO: Uncomment when getUserRanks is available
            // if (event.getSlot() != userRank.getValue().item.slot) continue;
            // event.getWhoClicked().openInventory(new PermissionsGUI<>(team, userRank.getKey(), (Player) event.getWhoClicked(), iridiumTeams).getInventory()); // TODO: Uncomment when PermissionsGUI is refactored
            // return;
        // }
    }
}
