package com.knemis.skyblock.skyblockcoreproject.teams.commands;


import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.MissionTypeSelectorInventoryConfig;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.MissionGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.MissionTypeSelectorGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MissionsCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public MissionsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().missionTypeSelectorGUI;
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "daily":
                    if (missionTypeSelectorInventoryConfig.daily.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.DAILY, player, SkyBlockProjectTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, SkyBlockProjectTeams).getInventory());
                    }
                    return true;
                case "weekly":
                    if (missionTypeSelectorInventoryConfig.weekly.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.WEEKLY, player, SkyBlockProjectTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, SkyBlockProjectTeams).getInventory());
                    }
                    return true;
                case "infinite":
                    if (missionTypeSelectorInventoryConfig.infinite.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.INFINITE, player, SkyBlockProjectTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, SkyBlockProjectTeams).getInventory());
                    }
                    return true;
                case "once":
                    if (missionTypeSelectorInventoryConfig.once.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.ONCE, player, SkyBlockProjectTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, SkyBlockProjectTeams).getInventory());
                    }
                    return true;
            }
        }
        player.openInventory(new MissionTypeSelectorGUI<>(player, SkyBlockProjectTeams).getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = SkyBlockProjectTeams.getInventories().missionTypeSelectorGUI;
        List<String> missionTypes = new ArrayList<>();
        if (missionTypeSelectorInventoryConfig.daily.enabled) {
            missionTypes.add("Daily");
        }

        if (missionTypeSelectorInventoryConfig.weekly.enabled) {
            missionTypes.add("Weekly");
        }

        if (missionTypeSelectorInventoryConfig.infinite.enabled) {
            missionTypes.add("Infinite");
        }

        if (missionTypeSelectorInventoryConfig.once.enabled) {
            missionTypes.add("Once");
        }
        return missionTypes;
    }
}
