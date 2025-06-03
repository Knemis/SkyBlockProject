package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MissionGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI

    private final T team;
    @Getter
    private final com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType missionType; // TODO: Update to actual MissionType class
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    private Player player; // Added player field

    public MissionGUI(T team, com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType missionType, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) { // TODO: Update MissionType
        // super(SkyBlockProjectTeams.getInventories().missionGUI.get(missionType).background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.team = team;
        this.missionType = missionType;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().missionGUI.get(missionType);
        // Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title)); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, noItemGUI.size, "Mission GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // List<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamMission> teamMissions = SkyBlockProjectTeams.getTeamManager().getTeamMissions(team); // TODO: Uncomment when TeamManager and TeamMission are refactored

        // Deals where slot is not null
        // for (Map.Entry<String, com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission> entry : SkyBlockProjectTeams.getMissions().missions.entrySet()) { // TODO: Uncomment when getMissions and Mission are available
            // if (entry.getValue().getMissionType() != missionType) continue;
            // Optional<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamMission> teamMission = teamMissions.stream().filter(m -> m.getMissionName().equals(entry.getKey())).findFirst(); // TODO: Uncomment when teamMissions is available
            // int level = teamMission.map(com.knemis.skyblock.skyblockcoreproject.teams.database.TeamMission::getMissionLevel).orElse(1); // TODO: Uncomment when TeamMission is refactored
            // if(teamMission.isPresent() && teamMission.get().hasExpired()){
                // SkyBlockProjectTeams.getTeamManager().deleteTeamMission(teamMission.get()); // TODO: Uncomment when TeamManager is refactored
                // SkyBlockProjectTeams.getTeamManager().deleteTeamMissionData(teamMission.get()); // TODO: Uncomment when TeamManager is refactored
                // level = 1;
            // }
            // com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData missionData = entry.getValue().getMissionData().get(level); // TODO: Uncomment when MissionData is refactored
            // if (missionData.getItem().slot == null) continue; // TODO: Uncomment when missionData is available
            // inventory.setItem(missionData.getItem().slot, getItem(entry.getKey())); // TODO: Uncomment when missionData is available
        // }

        // Deals where slot is null, to randomly pick a few missions
        // List<String> missions = SkyBlockProjectTeams.getTeamManager().getTeamMission(team, missionType); // TODO: Uncomment when TeamManager is refactored
        // int index = 0;
        // for (String missionName : missions) { // TODO: Uncomment when missions is available
            // if (SkyBlockProjectTeams.getMissions().dailySlots.size() <= index) continue; // TODO: Uncomment when getMissions is available
            // int slot = SkyBlockProjectTeams.getMissions().dailySlots.get(index); // TODO: Uncomment when getMissions is available
            // inventory.setItem(slot, getItem(missionName));
            // index++;
        // }
    }

    private ItemStack getItem(String missionName) {
        // This will create the mission if it doesnt exist
        // com.knemis.skyblock.skyblockcoreproject.teams.database.TeamMission teamMission = SkyBlockProjectTeams.getTeamManager().getTeamMission(team, missionName); // TODO: Uncomment when TeamManager and TeamMission are refactored
        // com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission mission = SkyBlockProjectTeams.getMissions().missions.get(missionName); // TODO: Uncomment when getMissions and Mission are available
        // com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData missionData = mission.getMissionData().get(teamMission.getMissionLevel()); // TODO: Uncomment when MissionData and teamMission are available

        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = IntStream.range(0, missionData.getMissions().size()) // TODO: Replace Placeholder
                // .boxed()
                // .map(integer -> SkyBlockProjectTeams.getTeamManager().getTeamMissionData(teamMission, integer)) // TODO: Uncomment when TeamManager and teamMission are available
                // .map(islandMission -> new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("progress_" + (islandMission.getMissionIndex() + 1), String.valueOf(islandMission.getProgress()))) // TODO: Replace Placeholder
                // .collect(Collectors.toList());

        // int seconds = Math.max((int) (teamMission.getRemainingTime() % 60), 0); // TODO: Uncomment when teamMission is available
        // int minutes = Math.max((int) ((teamMission.getRemainingTime() % 3600) / 60), 0); // TODO: Uncomment when teamMission is available
        // int hours = Math.max((int) (teamMission.getRemainingTime() / 3600), 0); // TODO: Uncomment when teamMission is available
        // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_hours", String.valueOf(hours))); // TODO: Replace Placeholder
        // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_minutes", String.valueOf(minutes))); // TODO: Replace Placeholder
        // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("timeremaining_seconds", String.valueOf(seconds))); // TODO: Replace Placeholder
        // return ItemStackUtils.makeItem(missionData.getItem(), placeholders); // TODO: Replace ItemStackUtils.makeItem, uncomment when missionData is available
        return null; // Placeholder
    }
}
