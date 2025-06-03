package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BlockValueGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.BlockValuesTypeSelectorGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.SpawnerValueGUI;
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
public class BlockValueCommand<T extends Team, U extends User<T>> extends Command<T, U> {

    public BlockValueCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockTeams<T, U> skyblockTeams) {

        Player player = user.getPlayer();
        Optional<T> team;

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = skyblockTeams.getInventories().blockValuesTypeSelectorGUI;

        String teamArg = args.length > 1 ? args[0] : player.getName();
        team = skyblockTeams.getTeamManager().getTeamViaNameOrPlayer(teamArg); // TODO: Ensure TeamManager is functional

        if (!team.isPresent() && args.length >= 1) { // Corrected to check args.length
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamDoesntExistByName.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }

        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().dontHaveTeam.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }

        TeamSetting teamSetting = skyblockTeams.getTeamManager().getTeamSetting(team.get(), SettingType.VALUE_VISIBILITY.getSettingKey()); // TODO: Ensure TeamManager is functional

        if (teamSetting != null && teamSetting.getValue().equalsIgnoreCase("Private") && !skyblockTeams.getTeamManager().getTeamMembers(team.get()).contains(user) && !user.isBypassing()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamIsPrivate.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }

        if (args.length == 0 || (args.length == 1 && Bukkit.getPlayer(args[0]) != null)) { // if only arg is a player name, or no args
            player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, skyblockTeams).getInventory());
            return true;
        }

        String typeArg = args.length > 1 ? args[args.length-1] : args[0]; // Last argument is type if playername is also provided

        switch (typeArg.toLowerCase()) {
            case ("blocks"): {
                if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
                    player.openInventory(new BlockValueGUI<>(team.get(), player, skyblockTeams).getInventory());
                    return true;
                }
            }
            case ("spawners"): {
                if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
                    player.openInventory(new SpawnerValueGUI<>(team.get(), player, skyblockTeams).getInventory());
                    return true;
                }
            }
            default: { // If type is not specified or invalid, but a player name might be the first arg for teamArg
                if (Bukkit.getPlayer(args[0]) != null && args.length == 1) { // Player name was teamArg, no type specified
                     player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, skyblockTeams).getInventory());
                } else if (args.length > 1 && Bukkit.getPlayer(args[0]) == null) { // args[0] was type, but invalid
                     player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
                     return false;
                } else { // Default to selector if logic is unclear or args[0] is not a player
                    player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, skyblockTeams).getInventory());
                }
                return true;
            }
        }
        // player.sendMessage("BlockValue command needs to be reimplemented after refactoring."); // Placeholder
        // return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {

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