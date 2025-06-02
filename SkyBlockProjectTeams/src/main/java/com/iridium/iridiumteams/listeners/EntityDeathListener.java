
package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@AllArgsConstructor
public class EntityDeathListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if(killer==null)return;
        U user = keviinTeams.getUserManager().getUser(killer);
        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, killer.getLocation().getWorld(), "KILL", event.getEntityType().name(), 1);
        });

    }

}
