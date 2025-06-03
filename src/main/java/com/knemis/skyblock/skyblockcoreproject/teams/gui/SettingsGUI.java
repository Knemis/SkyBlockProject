package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
// TODO: Address keviincore imports later, possibly replace with com.knemis.skyblock.skyblockcoreproject.utils.*
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Setting;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting; // TODO: Update to actual TeamSetting class
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

// TODO: Update Team and User to actual classes, resolve BackGUI
public class SettingsGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> /* extends com.keviin.keviincore.gui.BackGUI */ {

    private final SkyBlockTeams<T, U> skyblockTeams;
    private final T team;
    private Player player; // Added player field

    public SettingsGUI(T team, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        // super(skyblockTeams.getInventories().settingsGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.skyblockTeams = skyblockTeams;
        this.team = team;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        // Inventory inventory = Bukkit.createInventory(this, skyblockTeams.getInventories().settingsGUI.size, StringUtils.color(skyblockTeams.getInventories().settingsGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, skyblockTeams.getInventories().settingsGUI.size, "Settings GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // for (Map.Entry<String, Setting> setting : skyblockTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = skyblockTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
            // if (teamSetting == null) continue;

            // String teamSettingDisplay = teamSetting.getValue();
            // switch(teamSetting.getValue()) { // TODO: Uncomment when Messages are refactored
                // case "Enabled": {
                    // teamSettingDisplay = skyblockTeams.getMessages().enabledPlaceholder;
                    // break;
                // }
                // case "Disabled": {
                    // teamSettingDisplay = skyblockTeams.getMessages().disabledPlaceholder;
                    // break;
                // }
                // case "Private": {
                    // teamSettingDisplay = skyblockTeams.getMessages().privatePlaceholder;
                    // break;
                // }
                // case "Public": {
                    // teamSettingDisplay = skyblockTeams.getMessages().publicPlaceholder;
                    // break;
                // }
                // case "Server": {
                    // teamSettingDisplay = skyblockTeams.getMessages().serverPlaceholder;
                    // break;
                // }
                // case "Sunny": {
                    // teamSettingDisplay = skyblockTeams.getMessages().sunnyPlaceholder;
                    // break;
                // }
                // case "Raining": {
                    // teamSettingDisplay = skyblockTeams.getMessages().rainingPlaceholder;
                    // break;
                // }
                // case "Sunrise": {
                    // teamSettingDisplay = skyblockTeams.getMessages().sunrisePlaceholder;
                    // break;
                // }
                // case "Day": {
                    // teamSettingDisplay = skyblockTeams.getMessages().dayPlaceholder;
                    // break;
                // }
                // case "Morning": {
                    // teamSettingDisplay = skyblockTeams.getMessages().morningPlaceholder;
                    // break;
                // }
                // case "Noon": {
                    // teamSettingDisplay = skyblockTeams.getMessages().noonPlaceholder;
                    // break;
                // }
                // case "Sunset": {
                    // teamSettingDisplay = skyblockTeams.getMessages().sunsetPlaceholder;
                    // break;
                // }
                // case "Night": {
                    // teamSettingDisplay = skyblockTeams.getMessages().nightPlaceholder;
                    // break;
                // }
                // case "Midnight": {
                    // teamSettingDisplay = skyblockTeams.getMessages().midnightPlaceholder;
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

        // for (Map.Entry<String, Setting> setting : skyblockTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
            // if (setting.getValue().getItem().slot != event.getSlot()) continue;

            // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = skyblockTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
            // if (teamSetting == null) continue;
            // int currentIndex = setting.getValue().getValues().indexOf(teamSetting.getValue());
            // String newValue = setting.getValue().getValues().get(setting.getValue().getValues().size() > currentIndex + 1 ? currentIndex + 1 : 0);
            // skyblockTeams.getCommandManager().executeCommand(event.getWhoClicked(), skyblockTeams.getCommands().settingsCommand, new String[]{setting.getValue().getDisplayName(), newValue}); // TODO: Uncomment when CommandManager and Commands are refactored
            // return;
        // }
    }
}
