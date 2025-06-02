package com.knemis.skyblock.skyblockcoreproject.teams.gui;

import com.knemis.skyblock.skyblockcoreproject.secondcore.gui.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.ItemStackUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamMission;
import com.knemis.skyblock.skyblockcoreproject.teams.missions.Mission;
import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionData;
import com.knemis.skyblock.skyblockcoreproject.teams.missions.MissionType;
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

public class MissionGUI<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends BackGUI {

    private final T team;
    @Getter
    private final MissionType missionType;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public MissionGUI(T team, MissionType missionType, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        super(SkyBlockProjectTeams.getInventories().missionGUI.get(missionType).background, player, SkyBlockProjectTeams.getInventories().backButton);
        this.team = team;
        this.missionType = missionType;
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().missionGUI.get(missionType);
        Inventory inventory = Bukkit.createInventory(this, noItemGUI.size, StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        List<TeamMission> teamMissions = SkyBlockProjectTeams.getTeamManager().getTeamMissions(team);

        // Deals where slot is not null
        for (Map.Entry<String, Mission> entry : SkyBlockProjectTeams.getMissions().missions.entrySet()) {
            if (entry.getValue().getMissionType() != missionType) continue;
            Optional<TeamMission> teamMission = teamMissions.stream().filter(m -> m.getMissionName().equals(entry.getKey())).findFirst();
            int level = teamMission.map(TeamMission::getMissionLevel).orElse(1);
            if(teamMission.isPresent() && teamMission.get().hasExpired()){
                SkyBlockProjectTeams.getTeamManager().deleteTeamMission(teamMission.get());
                SkyBlockProjectTeams.getTeamManager().deleteTeamMissionData(teamMission.get());
                level = 1;
            }
            MissionData missionData = entry.getValue().getMissionData().get(level);
            if (missionData.getItem().slot == null) continue;
            inventory.setItem(missionData.getItem().slot, getItem(entry.getKey()));
        }

        // Deals where slot is null, to randomly pick a few missions
        List<String> missions = SkyBlockProjectTeams.getTeamManager().getTeamMission(team, missionType);
        int index = 0;
        for (String missionName : missions) {
            if (SkyBlockProjectTeams.getMissions().dailySlots.size() <= index) continue;
            int slot = SkyBlockProjectTeams.getMissions().dailySlots.get(index);
            inventory.setItem(slot, getItem(missionName));
            index++;
        }
    }

    private ItemStack getItem(String missionName) {
        // This will create the mission if it doesnt exist
        TeamMission teamMission = SkyBlockProjectTeams.getTeamManager().getTeamMission(team, missionName);
        Mission mission = SkyBlockProjectTeams.getMissions().missions.get(missionName);
        MissionData missionData = mission.getMissionData().get(teamMission.getMissionLevel());

        List<Placeholder> placeholders = IntStream.range(0, missionData.getMissions().size())
                .boxed()
                .map(integer -> SkyBlockProjectTeams.getTeamManager().getTeamMissionData(teamMission, integer))
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
