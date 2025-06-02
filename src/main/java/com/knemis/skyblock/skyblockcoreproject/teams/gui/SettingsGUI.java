package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class SettingsGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private final T team;

    public SettingsGUI(T team, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().settingsGUI.background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.team = team;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, SkyBlockProjectTeams.getInventories().settingsGUI.size, StringUtils.color(SkyBlockProjectTeams.getInventories().settingsGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) {
            TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team, setting.getKey());
            if (teamSetting == null) continue;

            String teamSettingDisplay = teamSetting.getValue();
            switch(teamSetting.getValue()) {
                case "Enabled": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().enabledPlaceholder;
                    break;
                }
                case "Disabled": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().disabledPlaceholder;
                    break;
                }
                case "Private": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().privatePlaceholder;
                    break;
                }
                case "Public": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().publicPlaceholder;
                    break;
                }
                case "Server": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().serverPlaceholder;
                    break;
                }
                case "Sunny": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().sunnyPlaceholder;
                    break;
                }
                case "Raining": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().rainingPlaceholder;
                    break;
                }
                case "Sunrise": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().sunrisePlaceholder;
                    break;
                }
                case "Day": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().dayPlaceholder;
                    break;
                }
                case "Morning": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().morningPlaceholder;
                    break;
                }
                case "Noon": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().noonPlaceholder;
                    break;
                }
                case "Sunset": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().sunsetPlaceholder;
                    break;
                }
                case "Night": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().nightPlaceholder;
                    break;
                }
                case "Midnight": {
                    teamSettingDisplay = SkyBlockProjectTeams.getMessages().midnightPlaceholder;
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

        for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) {
            if (setting.getValue().getItem().slot != event.getSlot()) continue;

            TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team, setting.getKey());
            if (teamSetting == null) continue;
            int currentIndex = setting.getValue().getValues().indexOf(teamSetting.getValue());
            String newValue = setting.getValue().getValues().get(setting.getValue().getValues().size() > currentIndex + 1 ? currentIndex + 1 : 0);
            SkyBlockProjectTeams.getCommandManager().executeCommand(event.getWhoClicked(), SkyBlockProjectTeams.getCommands().settingsCommand, new String[]{setting.getValue().getDisplayName(), newValue});
            return;
        }
    }
}
