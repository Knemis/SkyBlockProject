package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.MissionTypeSelectorInventoryConfig; // TODO: Update to actual class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.MissionGUI; // TODO: Update to actual MissionGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.gui.MissionTypeSelectorGUI; // TODO: Update to actual MissionTypeSelectorGUI class
// import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType; // TODO: Update to actual MissionType class
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MissionsCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public MissionsCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        // MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = iridiumTeams.getInventories().missionTypeSelectorGUI; // TODO: Uncomment when Inventories config is refactored
        // if (args.length == 1) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // switch (args[0].toLowerCase()) {
                // case "daily":
                    // if (missionTypeSelectorInventoryConfig.daily.enabled) {
                        // player.openInventory(new MissionGUI<>(team, MissionType.DAILY, player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionGUI and MissionType are refactored
                    // } else {
                        // player.openInventory(new MissionTypeSelectorGUI<>(player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionTypeSelectorGUI is refactored
                    // }
                    // return true;
                // case "weekly":
                    // if (missionTypeSelectorInventoryConfig.weekly.enabled) {
                        // player.openInventory(new MissionGUI<>(team, MissionType.WEEKLY, player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionGUI and MissionType are refactored
                    // } else {
                        // player.openInventory(new MissionTypeSelectorGUI<>(player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionTypeSelectorGUI is refactored
                    // }
                    // return true;
                // case "infinite":
                    // if (missionTypeSelectorInventoryConfig.infinite.enabled) {
                        // player.openInventory(new MissionGUI<>(team, MissionType.INFINITE, player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionGUI and MissionType are refactored
                    // } else {
                        // player.openInventory(new MissionTypeSelectorGUI<>(player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionTypeSelectorGUI is refactored
                    // }
                    // return true;
                // case "once":
                    // if (missionTypeSelectorInventoryConfig.once.enabled) {
                        // player.openInventory(new MissionGUI<>(team, MissionType.ONCE, player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionGUI and MissionType are refactored
                    // } else {
                        // player.openInventory(new MissionTypeSelectorGUI<>(player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionTypeSelectorGUI is refactored
                    // }
                    // return true;
            // }
        // }
        // player.openInventory(new MissionTypeSelectorGUI<>(player, iridiumTeams).getInventory()); // TODO: Uncomment when MissionTypeSelectorGUI is refactored
        player.sendMessage("Missions command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        // MissionTypeSelectorInventoryConfig missionTypeSelectorInventoryConfig = iridiumTeams.getInventories().missionTypeSelectorGUI; // TODO: Uncomment when Inventories config is refactored
        List<String> missionTypes = new ArrayList<>();
        // if (missionTypeSelectorInventoryConfig.daily.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // missionTypes.add("Daily");
        // }

        // if (missionTypeSelectorInventoryConfig.weekly.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // missionTypes.add("Weekly");
        // }

        // if (missionTypeSelectorInventoryConfig.infinite.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // missionTypes.add("Infinite");
        // }

        // if (missionTypeSelectorInventoryConfig.once.enabled) { // TODO: Uncomment when missionTypeSelectorInventoryConfig is available
            // missionTypes.add("Once");
        // }
        return missionTypes;
    }
}
