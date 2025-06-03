package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
// import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.BlockValuesTypeSelectorInventoryConfig; // TODO: Update to actual class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting; // TODO: Update to actual TeamSetting class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.BlockValueGUI; // TODO: Update to actual BlockValueGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.BlockValuesTypeSelectorGUI; // TODO: Update to actual BlockValuesTypeSelectorGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.SpawnerValueGUI; // TODO: Update to actual SpawnerValueGUI class
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class BlockValueCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes

    public BlockValueCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {

        Player player = user.getPlayer();
        Optional<T> team;

        // BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI; // TODO: Uncomment when Inventories config is refactored

        String teamArg = args.length > 1 ? args[0] : player.getName();
        // team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(teamArg); // TODO: Uncomment when TeamManager is refactored

        // if (!team.isPresent() && args.length >= 1) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        // if (!team.isPresent()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        // TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team.get(), SettingType.VALUE_VISIBILITY.getSettingKey()); // TODO: Uncomment when TeamManager is refactored

        // if (teamSetting != null && teamSetting.getValue().equalsIgnoreCase("Private") && !SkyBlockProjectTeams.getTeamManager().getTeamMembers(team.get()).contains(user) && !user.isBypassing()) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamIsPrivate.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            // return false;
        // }

        // if (args.length == 0) {
            // player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when BlockValuesTypeSelectorGUI is refactored
            // return true;
        // }

        // switch (args[args.length - 1]) { // TODO: Uncomment when dependent parts are refactored
            // case ("blocks"): {
                // if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
                    // player.openInventory(new BlockValueGUI<>(team.get(), player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when BlockValueGUI is refactored
                    // return true;
                // }
            // }
            // case ("spawners"): {
                // if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
                    // player.openInventory(new SpawnerValueGUI<>(team.get(), player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when SpawnerValueGUI is refactored
                    // return true;
                // }
            // }
            // default: {
                // player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, SkyBlockProjectTeams).getInventory()); // TODO: Uncomment when BlockValuesTypeSelectorGUI is refactored
                // return true;
            // }
        // }
        player.sendMessage("BlockValue command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {

        switch (args.length) {
            case 1:
                return Arrays.asList("blocks", "spawners");
            case 2:
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            default:
                return Collections.emptyList();
        }
    }
}