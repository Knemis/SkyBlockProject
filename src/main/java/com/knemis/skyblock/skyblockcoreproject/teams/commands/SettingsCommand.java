package com.knemis.skyblock.skyblockcoreproject.teams.commands;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Setting;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.api.SettingUpdateEvent;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
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
public class SettingsCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public SettingsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.openInventory(new SettingsGUI<>(team, player, SkyBlockProjectTeams).getInventory());
            return true;
        } else if (args.length == 2) {
            if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETTINGS)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotChangeSettings
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
            String settingKey = args[0];
            for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) {
                if (!setting.getValue().getDisplayName().equalsIgnoreCase(settingKey)) continue;
                TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team, setting.getKey());
                Optional<String> value = setting.getValue().getValues().stream().filter(s -> s.equalsIgnoreCase(args[1])).findFirst();

                if (!value.isPresent() || teamSetting == null) {
                    player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidSettingValue
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    ));
                    return false;
                }

                teamSetting.setValue(value.get());
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().settingSet
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%setting%", setting.getValue().getDisplayName())
                        .replace("%value%", value.get())
                ));

                Bukkit.getPluginManager().callEvent(new SettingUpdateEvent<>(team, user, setting.getKey(), value.get()));
                return true;
            }
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidSetting
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        switch (args.length) {
            case 1:
                return SkyBlockProjectTeams.getSettingsList().values().stream().map(Setting::getDisplayName).collect(Collectors.toList());
            case 2:
                for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) {
                    if (!setting.getValue().getDisplayName().equalsIgnoreCase(args[0])) continue;
                    return setting.getValue().getValues();
                }
            default:
                return Collections.emptyList();
        }
    }

}
