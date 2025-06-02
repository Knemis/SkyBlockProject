package com.knemis.skyblock.skyblockcoreproject.teams.listeners;


import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Optional;

@AllArgsConstructor
public class StructureGrowListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(StructureGrowEvent event) {
        int currentTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getLocation()).map(T::getId).orElse(0);
        event.getBlocks().removeIf(blockState -> {
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(currentTeam) != currentTeam;
        });
    }

}