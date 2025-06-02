package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.Setting;
import com.keviin.keviinteams.api.SettingUpdateEvent;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import com.keviin.keviinteams.gui.SettingsGUI;
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
public class SettingsCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public SettingsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new SettingsGUI<>(team, player, keviinTeams).getInventory());
            return true;
        } else if (args.length == 2) {
            if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETTINGS)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotChangeSettings
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
            String settingKey = args[0];
            for (Map.Entry<String, Setting> setting : keviinTeams.getSettingsList().entrySet()) {
                if (!setting.getValue().getDisplayName().equalsIgnoreCase(settingKey)) continue;
                TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, setting.getKey());
                Optional<String> value = setting.getValue().getValues().stream().filter(s -> s.equalsIgnoreCase(args[1])).findFirst();

                if (!value.isPresent() || teamSetting == null) {
                    player.sendMessage(StringUtils.color(keviinTeams.getMessages().invalidSettingValue
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    ));
                    return false;
                }

                teamSetting.setValue(value.get());
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().settingSet
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%setting%", setting.getValue().getDisplayName())
                        .replace("%value%", value.get())
                ));

                Bukkit.getPluginManager().callEvent(new SettingUpdateEvent<>(team, user, setting.getKey(), value.get()));
                return true;
            }
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().invalidSetting
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        switch (args.length) {
            case 1:
                return keviinTeams.getSettingsList().values().stream().map(Setting::getDisplayName).collect(Collectors.toList());
            case 2:
                for (Map.Entry<String, Setting> setting : keviinTeams.getSettingsList().entrySet()) {
                    if (!setting.getValue().getDisplayName().equalsIgnoreCase(args[0])) continue;
                    return setting.getValue().getValues();
                }
            default:
                return Collections.emptyList();
        }
    }

}
