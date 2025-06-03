package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Setting;
import com.knemis.skyblock.skyblockcoreproject.teams.api.SettingUpdateEvent;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.SettingsGUI;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class SettingsCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public SettingsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new SettingsGUI<>(team, player, skyblockTeams).getInventory());
            return true;
        } else if (args.length == 2) {
            if (!skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETTINGS)) { // TODO: Ensure TeamManager is functional
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotChangeSettings
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                ));
                return false;
            }
            String settingKey = args[0];
            for (Map.Entry<String, Setting> setting : skyblockTeams.getSettingsList().entrySet()) { // TODO: Ensure getSettingsList is functional
                if (!setting.getValue().getDisplayName().equalsIgnoreCase(settingKey)) continue;
                TeamSetting teamSetting = skyblockTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Ensure TeamManager and TeamSetting are functional
                Optional<String> value = setting.getValue().getValues().stream().filter(s -> s.equalsIgnoreCase(args[1])).findFirst();

                if (!value.isPresent() || teamSetting == null) {
                    player.sendMessage(StringUtils.color(skyblockTeams.getMessages().invalidSettingValue
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    ));
                    return false;
                }

                teamSetting.setValue(value.get()); // TODO: Ensure TeamSetting set/save methods are functional
                player.sendMessage(StringUtils.color(skyblockTeams.getMessages().settingSet
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                        .replace("%setting%", setting.getValue().getDisplayName())
                        .replace("%value%", value.get())
                ));

                Bukkit.getPluginManager().callEvent(new SettingUpdateEvent<>(team, user, setting.getKey(), value.get()));
                return true;
            }
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().invalidSetting
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
        // player.sendMessage("Settings command needs to be reimplemented after refactoring."); // Placeholder
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        switch (args.length) {
            case 1:
                return skyblockTeams.getSettingsList().values().stream().map(Setting::getDisplayName).collect(Collectors.toList()); // TODO: Ensure getSettingsList is functional
            case 2:
                for (Map.Entry<String, Setting> setting : skyblockTeams.getSettingsList().entrySet()) { // TODO: Ensure getSettingsList is functional
                    if (!setting.getValue().getDisplayName().equalsIgnoreCase(args[0])) continue;
                    return setting.getValue().getValues();
                }
            default:
                return Collections.emptyList();
        }
        // return Collections.emptyList(); // Placeholder
    }

}
