package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.keviin.keviincore.gui.BackGUI;
import com.keviin.keviincore.utils.ItemStackUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.configs.inventories.NoItemGUI;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamMission;
import com.keviin.keviinteams.missions.Mission;
import com.keviin.keviinteams.missions.MissionData;
import com.keviin.keviinteams.missions.MissionType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MissionGUI<T extends Team, U extends keviinUser<T>> extends BackGUI {

    private final T team;
    @Getter
    private final MissionType missionType;
    private final keviinTeams<T, U> keviinTeams;

    public MissionGUI(T team, MissionType missionType, Player player, keviinTeams<T, U> keviinTeams) {
        super(keviinTeams.getInventories().missionGUI.get(missionType).background, player, keviinTeams.getInventories().backButton);
        this.team = team;
        this.missionType = missionType;
        this.keviinTeams = keviinTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = keviinTeams.getInventories().missionGUI.get(missionType);
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        List<TeamMission> teamMissions = keviinTeams.getTeamManager().getTeamMissions(team);

        // Deals where slot is not null
        for (Map.Entry<String, Mission> entry : keviinTeams.getMissions().missions.entrySet()) {
            if (entry.getValue().getMissionType() != missionType) continue;
            Optional<TeamMission> teamMission = teamMissions.stream().filter(m -> m.getMissionName().equals(entry.getKey())).findFirst();
            int level = teamMission.map(TeamMission::getMissionLevel).orElse(1);
            if(teamMission.isPresent() && teamMission.get().hasExpired()){
                keviinTeams.getTeamManager().deleteTeamMission(teamMission.get());
                keviinTeams.getTeamManager().deleteTeamMissionData(teamMission.get());
                level = 1;
            }
            MissionData missionData = entry.getValue().getMissionData().get(level);
            if (missionData.getItem().slot == null) continue;
            inventory.setItem(missionData.getItem().slot, getItem(entry.getKey()));
        }

        // Deals where slot is null, to randomly pick a few missions
        List<String> missions = keviinTeams.getTeamManager().getTeamMission(team, missionType);
        int index = 0;
        for (String missionName : missions) {
            if (keviinTeams.getMissions().dailySlots.size() <= index) continue;
            int slot = keviinTeams.getMissions().dailySlots.get(index);
            inventory.setItem(slot, getItem(missionName));
            index++;
        }
    }

    private ItemStack getItem(String missionName) {
        // This will create the mission if it doesnt exist
        TeamMission teamMission = keviinTeams.getTeamManager().getTeamMission(team, missionName);
        Mission mission = keviinTeams.getMissions().missions.get(missionName);
        MissionData missionData = mission.getMissionData().get(teamMission.getMissionLevel());

        List<Placeholder> placeholders = IntStream.range(0, missionData.getMissions().size())
                .boxed()
                .map(integer -> keviinTeams.getTeamManager().getTeamMissionData(teamMission, integer))
                .map(islandMission -> new Placeholder("progress_" + (islandMission.getMissionIndex() + 1), String.valueOf(islandMission.getProgress())))
                .collect(Collectors.toList());

        int seconds = Math.max((int) (teamMission.getRemainingTime() % 60), 0);
        int minutes = Math.max((int) ((teamMission.getRemainingTime() % 3600) / 60), 0);
        int hours = Math.max((int) (teamMission.getRemainingTime() / 3600), 0);
        placeholders.add(new Placeholder("timeremaining_hours", String.valueOf(hours)));
        placeholders.add(new Placeholder("timeremaining_minutes", String.valueOf(minutes)));
        placeholders.add(new Placeholder("timeremaining_seconds", String.valueOf(seconds)));
        return ItemStackUtils.makeItem(missionData.getItem(), placeholders);
    }
}
