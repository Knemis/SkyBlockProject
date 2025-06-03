package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.MissionTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MissionTypeSelectorGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private Player player; // Added player field

    public MissionTypeSelectorGUI(Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().missionTypeSelectorGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().missionTypeSelectorGUI;
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "MissionTypeSelector GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method


        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().missionTypeSelectorGUI;
        // if (missionTypeSelectorInventoryConfig.daily.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // inventory.setItem(missionTypeSelectorInventoryConfig.daily.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.daily.item)); // TODO: Replace ItemStackUtils.makeItem
        // }

        // if (missionTypeSelectorInventoryConfig.weekly.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // inventory.setItem(missionTypeSelectorInventoryConfig.weekly.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.weekly.item)); // TODO: Replace ItemStackUtils.makeItem
        // }

        // if (missionTypeSelectorInventoryConfig.infinite.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // inventory.setItem(missionTypeSelectorInventoryConfig.infinite.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.infinite.item)); // TODO: Replace ItemStackUtils.makeItem
        // }

        // if (missionTypeSelectorInventoryConfig.once.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // inventory.setItem(missionTypeSelectorInventoryConfig.once.item.slot, ItemStackUtils.makeItem(missionTypeSelectorInventoryConfig.once.item)); // TODO: Replace ItemStackUtils.makeItem
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method
        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().missionTypeSelectorGUI;

        // if (event.getSlot() == missionTypeSelectorInventoryConfig.daily.item.slot && missionTypeSelectorInventoryConfig.daily.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().missionsCommand, new String[]{"Daily"}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }

        // if (event.getSlot() == missionTypeSelectorInventoryConfig.weekly.item.slot && missionTypeSelectorInventoryConfig.weekly.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().missionsCommand, new String[]{"Weekly"}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }

        // if (event.getSlot() == missionTypeSelectorInventoryConfig.infinite.item.slot && missionTypeSelectorInventoryConfig.infinite.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().missionsCommand, new String[]{"Infinite"}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }

        // if (event.getSlot() == missionTypeSelectorInventoryConfig.once.item.slot && missionTypeSelectorInventoryConfig.once.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().missionsCommand, new String[]{"Once"}); // TODO: Uncomment when CommandManager and Commands are refactored
        // }
    }
}
