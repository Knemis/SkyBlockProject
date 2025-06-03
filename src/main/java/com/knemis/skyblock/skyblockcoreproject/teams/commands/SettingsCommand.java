package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.Setting;
import com.knemis.skyblock.skyblockcoreproject.teams.api.SettingUpdateEvent;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting; // TODO: Update to actual TeamSetting class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.SettingsGUI; // TODO: Update to actual SettingsGUI class
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
public class SettingsCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public SettingsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (args.length == 0) { // TODO: Uncomment when SettingsGUI is refactored
            // player.openInventory(new SettingsGUI<>(team, player, SkyBlockProjectTeams).getInventory());
            // return true;
        // } else if (args.length == 2) {
            // if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETTINGS)) { // TODO: Uncomment when TeamManager is refactored
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotChangeSettings // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // return false;
            // }
            // String settingKey = args[0];
            // for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) { // TODO: Uncomment when getSettingsList is available
                // if (!setting.getValue().getDisplayName().equalsIgnoreCase(settingKey)) continue;
                // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team, setting.getKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
                // Optional<String> value = setting.getValue().getValues().stream().filter(s -> s.equalsIgnoreCase(args[1])).findFirst();

                // if (!value.isPresent() || teamSetting == null) {
                    // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidSettingValue // TODO: Replace StringUtils.color
                            // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // ));
                    // return false;
                // }

                // teamSetting.setValue(value.get());
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().settingSet // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%setting%", setting.getValue().getDisplayName())
                        // .replace("%value%", value.get())
                // ));

                // Bukkit.getPluginManager().callEvent(new SettingUpdateEvent<>(team, user, setting.getKey(), value.get()));
                // return true;
            // }
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().invalidSetting // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
        player.sendMessage("Settings command needs to be reimplemented after refactoring."); // Placeholder
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // switch (args.length) { // TODO: Uncomment when getSettingsList is available
            // case 1:
                // return SkyBlockProjectTeams.getSettingsList().values().stream().map(Setting::getDisplayName).collect(Collectors.toList());
            // case 2:
                // for (Map.Entry<String, Setting> setting : SkyBlockProjectTeams.getSettingsList().entrySet()) {
                    // if (!setting.getValue().getDisplayName().equalsIgnoreCase(args[0])) continue;
                    // return setting.getValue().getValues();
                // }
            // default:
                // return Collections.emptyList();
        // }
        return Collections.emptyList(); // Placeholder
    }

}
