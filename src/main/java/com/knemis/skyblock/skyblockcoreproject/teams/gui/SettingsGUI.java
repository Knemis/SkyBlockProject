package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class SettingsGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI

    private final IridiumTeams<T, U> iridiumTeams;
    private final T team;
    private Player player; // Added player field

    public SettingsGUI(T team, Player player, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().settingsGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.iridiumTeams = iridiumTeams;
        this.team = team;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, iridiumTeams.getInventories().settingsGUI.size, StringUtils.color(iridiumTeams.getInventories().settingsGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, iridiumTeams.getInventories().settingsGUI.size, "Settings GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<String, Setting> setting : iridiumTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = iridiumTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
            // if (teamSetting == null) continue;

            // String teamSettingDisplay = teamSetting.getValue();
            // switch(teamSetting.getValue()) { // TODO: Uncomment when Messages are refactored
                // case "Enabled": {
                    // teamSettingDisplay = iridiumTeams.getMessages().enabledPlaceholder;
                    // break;
                // }
                // case "Disabled": {
                    // teamSettingDisplay = iridiumTeams.getMessages().disabledPlaceholder;
                    // break;
                // }
                // case "Private": {
                    // teamSettingDisplay = iridiumTeams.getMessages().privatePlaceholder;
                    // break;
                // }
                // case "Public": {
                    // teamSettingDisplay = iridiumTeams.getMessages().publicPlaceholder;
                    // break;
                // }
                // case "Server": {
                    // teamSettingDisplay = iridiumTeams.getMessages().serverPlaceholder;
                    // break;
                // }
                // case "Sunny": {
                    // teamSettingDisplay = iridiumTeams.getMessages().sunnyPlaceholder;
                    // break;
                // }
                // case "Raining": {
                    // teamSettingDisplay = iridiumTeams.getMessages().rainingPlaceholder;
                    // break;
                // }
                // case "Sunrise": {
                    // teamSettingDisplay = iridiumTeams.getMessages().sunrisePlaceholder;
                    // break;
                // }
                // case "Day": {
                    // teamSettingDisplay = iridiumTeams.getMessages().dayPlaceholder;
                    // break;
                // }
                // case "Morning": {
                    // teamSettingDisplay = iridiumTeams.getMessages().morningPlaceholder;
                    // break;
                // }
                // case "Noon": {
                    // teamSettingDisplay = iridiumTeams.getMessages().noonPlaceholder;
                    // break;
                // }
                // case "Sunset": {
                    // teamSettingDisplay = iridiumTeams.getMessages().sunsetPlaceholder;
                    // break;
                // }
                // case "Night": {
                    // teamSettingDisplay = iridiumTeams.getMessages().nightPlaceholder;
                    // break;
                // }
                // case "Midnight": {
                    // teamSettingDisplay = iridiumTeams.getMessages().midnightPlaceholder;
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

        // for (Map.Entry<String, Setting> setting : iridiumTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
            // if (setting.getValue().getItem().slot != event.getSlot()) continue;

            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = iridiumTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
            // if (teamSetting == null) continue;
            // int currentIndex = setting.getValue().getValues().indexOf(teamSetting.getValue());
            // String newValue = setting.getValue().getValues().get(setting.getValue().getValues().size() > currentIndex + 1 ? currentIndex + 1 : 0);
            // iridiumTeams.getCommandManager().executeCommand(event.getWhoClicked(), iridiumTeams.getCommands().settingsCommand, new String[]{setting.getValue().getDisplayName(), newValue}); // TODO: Uncomment when CommandManager and Commands are refactored
            // return;
        // }
    }
}
