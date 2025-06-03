package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class BlockValuesTypeSelectorGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final String teamArg;
    private Player player; // Added player field

    public BlockValuesTypeSelectorGUI(String teamArg, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.teamArg = teamArg;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "BlockValuesTypeSelector GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;
        // if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // inventory.setItem(blockValuesTypeSelectorInventoryConfig.blocks.item.slot, ItemStackUtils.makeItem(blockValuesTypeSelectorInventoryConfig.blocks.item)); // TODO: Replace ItemStackUtils.makeItem
        // }

        // if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // inventory.setItem(blockValuesTypeSelectorInventoryConfig.spawners.item.slot, ItemStackUtils.makeItem(blockValuesTypeSelectorInventoryConfig.spawners.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method
        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;

        // if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.blocks.item.slot && blockValuesTypeSelectorInventoryConfig.blocks.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().blockValueCommand, new String[]{"blocks", teamArg}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }

        // if (event.getSlot() == blockValuesTypeSelectorInventoryConfig.spawners.item.slot && blockValuesTypeSelectorInventoryConfig.spawners.enabled) { // TODO: Uncomment when blockValuesTypeSelectorInventoryConfig is available
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().blockValueCommand, new String[]{"spawners", teamArg}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }
    }
}
