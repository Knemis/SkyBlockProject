package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Optional;

@AllArgsConstructor
public class StructureGrowListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(StructureGrowEvent event) {
        int currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getLocation()).map(T::getId).orElse(0);
        event.getBlocks().removeIf(blockState -> {
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(currentTeam) != currentTeam;
        });
    }

}