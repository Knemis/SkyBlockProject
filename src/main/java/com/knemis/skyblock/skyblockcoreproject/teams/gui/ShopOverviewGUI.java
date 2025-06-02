package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.Shop;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShopOverviewGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI
    private final IridiumTeams<T, U> iridiumTeams;
    private Player player; // Added player field

    public ShopOverviewGUI(Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().shopOverviewGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.iridiumTeams = iridiumTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = iridiumTeams.getInventories().shopOverviewGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "ShopOverview GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Shop.ShopCategory category : iridiumTeams.getShop().categories.values()) { // TODO: Uncomment when getShop is available
            // inventory.setItem(category.item.slot, ItemStackUtils.makeItem(category.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // for (Map.Entry<String, Shop.ShopCategory> category : iridiumTeams.getShop().categories.entrySet()) { // TODO: Uncomment when getShop is available
            // if (event.getSlot() != category.getValue().item.slot) continue;
            // event.getWhoClicked().openInventory(new ShopCategoryGUI<>(category.getKey(), (Player) event.getWhoClicked(), 1, iridiumTeams).getInventory()); // TODO: Uncomment when ShopCategoryGUI is refactored
            // return;
        // }
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method
    }
}


