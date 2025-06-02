package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import com.keviin.keviinteams.gui.BlockValueGUI;
import com.keviin.keviinteams.gui.BlockValuesTypeSelectorGUI;
import com.keviin.keviinteams.gui.SpawnerValueGUI;
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
public class BlockValueCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {

    public BlockValueCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, keviinTeams<T, U> keviinTeams) {

        Player player = user.getPlayer();
        Optional<T> team;

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = keviinTeams.getInventories().blockValuesTypeSelectorGUI;

        String teamArg = args.length > 1 ? args[0] : player.getName();
        team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(teamArg);

        if (!team.isPresent() && args.length >= 1) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDoesntExistByName.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().dontHaveTeam.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team.get(), SettingType.VALUE_VISIBILITY.getSettingKey());

        if (teamSetting != null && teamSetting.getValue().equalsIgnoreCase("Private") && !keviinTeams.getTeamManager().getTeamMembers(team.get()).contains(user) && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamIsPrivate.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }

        if (args.length == 0) {
            player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, keviinTeams).getInventory());
            return true;
        }

        switch (args[args.length - 1]) {
            case ("blocks"): {
                if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
                    player.openInventory(new BlockValueGUI<>(team.get(), player, keviinTeams).getInventory());
                    return true;
                }
            }
            case ("spawners"): {
                if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
                    player.openInventory(new SpawnerValueGUI<>(team.get(), player, keviinTeams).getInventory());
                    return true;
                }
            }
            default: {
                player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, keviinTeams).getInventory());
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {

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