package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Setting;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class SettingsGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final keviinTeams<T, U> keviinTeams;
    private final T team;

    public SettingsGUI(T team, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().settingsGUI.background, player, keviinTeams.getInventories().backButton);
        this.keviinTeams = keviinTeams;
        this.team = team;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, keviinTeams.getInventories().settingsGUI.size, StringUtils.color(keviinTeams.getInventories().settingsGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<String, Setting> setting : keviinTeams.getSettingsList().entrySet()) {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, setting.getKey());
            if (teamSetting == null) continue;

            String teamSettingDisplay = teamSetting.getValue();
            switch(teamSetting.getValue()) {
                case "Enabled": {
                    teamSettingDisplay = keviinTeams.getMessages().enabledPlaceholder;
                    break;
                }
                case "Disabled": {
                    teamSettingDisplay = keviinTeams.getMessages().disabledPlaceholder;
                    break;
                }
                case "Private": {
                    teamSettingDisplay = keviinTeams.getMessages().privatePlaceholder;
                    break;
                }
                case "Public": {
                    teamSettingDisplay = keviinTeams.getMessages().publicPlaceholder;
                    break;
                }
                case "Server": {
                    teamSettingDisplay = keviinTeams.getMessages().serverPlaceholder;
                    break;
                }
                case "Sunny": {
                    teamSettingDisplay = keviinTeams.getMessages().sunnyPlaceholder;
                    break;
                }
                case "Raining": {
                    teamSettingDisplay = keviinTeams.getMessages().rainingPlaceholder;
                    break;
                }
                case "Sunrise": {
                    teamSettingDisplay = keviinTeams.getMessages().sunrisePlaceholder;
                    break;
                }
                case "Day": {
                    teamSettingDisplay = keviinTeams.getMessages().dayPlaceholder;
                    break;
                }
                case "Morning": {
                    teamSettingDisplay = keviinTeams.getMessages().morningPlaceholder;
                    break;
                }
                case "Noon": {
                    teamSettingDisplay = keviinTeams.getMessages().noonPlaceholder;
                    break;
                }
                case "Sunset": {
                    teamSettingDisplay = keviinTeams.getMessages().sunsetPlaceholder;
                    break;
                }
                case "Night": {
                    teamSettingDisplay = keviinTeams.getMessages().nightPlaceholder;
                    break;
                }
                case "Midnight": {
                    teamSettingDisplay = keviinTeams.getMessages().midnightPlaceholder;
                    break;
                }
            }

            inventory.setItem(setting.getValue().getItem().slot, ItemStackUtils.makeItem(setting.getValue().getItem(), Collections.singletonList(
                    new Placeholder("value", teamSettingDisplay)
            )));
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        for (Map.Entry<String, Setting> setting : keviinTeams.getSettingsList().entrySet()) {
            if (setting.getValue().getItem().slot != event.getSlot()) continue;

            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, setting.getKey());
            if (teamSetting == null) continue;
            int currentIndex = setting.getValue().getValues().indexOf(teamSetting.getValue());
            String newValue = setting.getValue().getValues().get(setting.getValue().getValues().size() > currentIndex + 1 ? currentIndex + 1 : 0);
            keviinTeams.getCommandManager().executeCommand(event.getWhoClicked(), keviinTeams.getCommands().settingsCommand, new String[]{setting.getValue().getDisplayName(), newValue});
            return;
        }
    }
}
