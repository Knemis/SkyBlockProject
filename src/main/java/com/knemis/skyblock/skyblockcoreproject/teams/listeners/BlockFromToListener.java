package com.knemis.skyblock.skyblockcoreproject.teams.listeners;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;


import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockFromToListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getToBlock().getLocation());
        int currentTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation(), team).map(T::getId).orElse(0);
        if (team.map(T::getId).orElse(currentTeam) != currentTeam) {
            event.setCancelled(true);
        }
    }

}