
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;


import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

@AllArgsConstructor
public class PlayerFishListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorPlayerFish(PlayerFishEvent event) {
        Entity caughtEntity = event.getCaught();
        if (caughtEntity == null || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        U user = SkyBlockProjectTeams.getUserManager().getUser(event.getPlayer());

        SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, caughtEntity.getLocation().getWorld(), "FISH", ((Item) caughtEntity).getItemStack().getType().name(), 1);
        });

    }

}
