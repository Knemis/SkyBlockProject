package com.keviin.keviinteams.commands;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.MissionTypeSelectorInventoryConfig;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.MissionGUI;
import com.keviin.keviinteams.gui.MissionTypeSelectorGUI;
import com.keviin.keviinteams.missions.MissionType;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MissionsCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public MissionsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = keviinTeams.getInventories().missionTypeSelectorGUI;
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "daily":
                    if (missionTypeSelectorInventoryConfig.daily.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.DAILY, player, keviinTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, keviinTeams).getInventory());
                    }
                    return true;
                case "weekly":
                    if (missionTypeSelectorInventoryConfig.weekly.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.WEEKLY, player, keviinTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, keviinTeams).getInventory());
                    }
                    return true;
                case "infinite":
                    if (missionTypeSelectorInventoryConfig.infinite.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.INFINITE, player, keviinTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, keviinTeams).getInventory());
                    }
                    return true;
                case "once":
                    if (missionTypeSelectorInventoryConfig.once.enabled) {
                        player.openInventory(new MissionGUI<>(team, MissionType.ONCE, player, keviinTeams).getInventory());
                    } else {
                        player.openInventory(new MissionTypeSelectorGUI<>(player, keviinTeams).getInventory());
                    }
                    return true;
            }
        }
        player.openInventory(new MissionTypeSelectorGUI<>(player, keviinTeams).getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = keviinTeams.getInventories().missionTypeSelectorGUI;
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
