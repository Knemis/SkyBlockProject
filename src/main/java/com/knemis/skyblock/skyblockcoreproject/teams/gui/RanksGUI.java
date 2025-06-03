package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
// TODO: Address keviincore imports later, possibly replace with com.knemis.skyblock.skyblockcoreproject.utils.*
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.UserRank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// TODO: Update Team and User to actual classes, resolve BackGUI
public class RanksGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> /* extends com.keviin.keviincore.gui.BackGUI */ {

    private final SkyBlockTeams<T, U> skyblockTeams;
    private final T team;
    private Player player; // Added player field

    public RanksGUI(T team, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        // super(skyblockTeams.getInventories().ranksGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.team = team;
        this.skyblockTeams = skyblockTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, skyblockTeams.getInventories().ranksGUI.size, StringUtils.color(skyblockTeams.getInventories().ranksGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, skyblockTeams.getInventories().ranksGUI.size, "Ranks GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (UserRank userRank : skyblockTeams.getUserRanks().values()) { // TODO: Uncomment when getUserRanks is available
            // inventory.setItem(userRank.item.slot, ItemStackUtils.makeItem(userRank.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<Integer, UserRank> userRank : skyblockTeams.getUserRanks().entrySet()) { // TODO: Uncomment when getUserRanks is available
            // if (event.getSlot() != userRank.getValue().item.slot) continue;
            // event.getWhoClicked().openInventory(new PermissionsGUI<>(team, userRank.getKey(), (Player) event.getWhoClicked(), skyblockTeams).getInventory()); // TODO: Uncomment when PermissionsGUI is refactored
            // return;
        // }
    }
}
