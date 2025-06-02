package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockFromToListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(event.getToBlock().getLocation());
        int currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation(), team).map(T::getId).orElse(0);
        if (team.map(T::getId).orElse(currentTeam) != currentTeam) {
            event.setCancelled(true);
        }
    }

}