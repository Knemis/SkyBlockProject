package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class SettingsGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;
    private Player player; // Added player field

    public SettingsGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().settingsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().settingsGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().settingsGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, SkyBlockProjectTeams.getInventories().settingsGUI.size, "Settings GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
            // if (teamSetting == null) continue;

            // String teamSettingDisplay = teamSetting.getValue();
            // switch(teamSetting.getValue()) { // TODO: Uncomment when Messages are refactored
                // case "Enabled": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().enabledPlaceholder;
                    // break;
                // }
                // case "Disabled": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().disabledPlaceholder;
                    // break;
                // }
                // case "Private": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().privatePlaceholder;
                    // break;
                // }
                // case "Public": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().publicPlaceholder;
                    // break;
                // }
                // case "Server": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().serverPlaceholder;
                    // break;
                // }
                // case "Sunny": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().sunnyPlaceholder;
                    // break;
                // }
                // case "Raining": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().rainingPlaceholder;
                    // break;
                // }
                // case "Sunrise": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().sunrisePlaceholder;
                    // break;
                // }
                // case "Day": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().dayPlaceholder;
                    // break;
                // }
                // case "Morning": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().morningPlaceholder;
                    // break;
                // }
                // case "Noon": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().noonPlaceholder;
                    // break;
                // }
                // case "Sunset": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().sunsetPlaceholder;
                    // break;
                // }
                // case "Night": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().nightPlaceholder;
                    // break;
                // }
                // case "Midnight": {
                    // teamSettingDisplay = SkyBlockProjectTeams.getMessages().midnightPlaceholder;
                    // break;
                // }
            // }

            // inventory.setItem(setting.getValue().getItem().slot, ItemStackUtils.makeItem(setting.getValue().getItem(), Collections.singletonList( // TODO: Replace ItemStackUtils.makeItem
                    // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("value", teamSettingDisplay) // TODO: Replace Placeholder
            // )));
        // }
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
            // if (setting.getValue().getItem().slot != event.getSlot()) continue;

            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
            // if (teamSetting == null) continue;
            // int currentIndex = setting.getValue().getValues().indexOf(teamSetting.getValue());
            // String newValue = setting.getValue().getValues().get(setting.getValue().getValues().size() > currentIndex + 1 ? currentIndex + 1 : 0);
            // SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().settingsCommand, new String[]{setting.getValue().getDisplayName(), newValue}); // TODO: Uncomment when CommandManager and Commands are refactored
            // return;
        // }
    }
}
