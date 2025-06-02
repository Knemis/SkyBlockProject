
package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

@AllArgsConstructor
public class PlayerFishListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorPlayerFish(PlayerFishEvent event) {
        Entity caughtEntity = event.getCaught();
        if (caughtEntity == null || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        U user = keviinTeams.getUserManager().getUser(event.getPlayer());

        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, caughtEntity.getLocation().getWorld(), "FISH", ((Item) caughtEntity).getItemStack().getType().name(), 1);
        });

    }

}
