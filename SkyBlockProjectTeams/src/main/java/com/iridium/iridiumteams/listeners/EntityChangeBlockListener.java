
package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

@AllArgsConstructor
public class EntityChangeBlockListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK || event.getEntityType() == EntityType.PLAYER) return;
        keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, SettingType.ENTITY_GRIEF.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled")) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, SettingType.ENTITY_GRIEF.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled")) {
                event.setCancelled(true);
            }
        });
    }

}
