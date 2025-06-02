package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.BlockValuesTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
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
public class BlockValueCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {

    public BlockValueCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {

        Player player = user.getPlayer();
        Optional<T> team;

        BlockValuesTypeSelectorInventoryConfig blockValuesTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().blockValuesTypeSelectorGUI;

        String teamArg = args.length > 1 ? args[0] : player.getName();
        team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(teamArg);

        if (!team.isPresent() && args.length >= 1) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team.get(), SettingType.VALUE_VISIBILITY.getSettingKey());

        if (teamSetting != null && teamSetting.getValue().equalsIgnoreCase("Private") && !SkyBlockProjectTeams.getTeamManager().getTeamMembers(team.get()).contains(user) && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamIsPrivate.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }

        if (args.length == 0) {
            player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, SkyBlockProjectTeams).getInventory());
            return true;
        }

        switch (args[args.length - 1]) {
            case ("blocks"): {
                if (blockValuesTypeSelectorInventoryConfig.blocks.enabled) {
                    player.openInventory(new BlockValueGUI<>(team.get(), player, SkyBlockProjectTeams).getInventory());
                    return true;
                }
            }
            case ("spawners"): {
                if (blockValuesTypeSelectorInventoryConfig.spawners.enabled) {
                    player.openInventory(new SpawnerValueGUI<>(team.get(), player, SkyBlockProjectTeams).getInventory());
                    return true;
                }
            }
            default: {
                player.openInventory(new BlockValuesTypeSelectorGUI<>(teamArg, player, SkyBlockProjectTeams).getInventory());
                return true;
            }
        }
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