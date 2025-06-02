
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

@AllArgsConstructor
public class EntitySpawnListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Optional<T> currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getEntity().getLocation());
        if (currentTeam.isPresent()) {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(currentTeam.get(), SettingType.MOB_SPAWNING.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled") && event.getEntity() instanceof LivingEntity && event.getEntityType() != EntityType.ARMOR_STAND) {
                event.setCancelled(true);
            }
        }

        event.getEntity().setMetadata("team_spawned", new FixedMetadataValue(keviinTeams, currentTeam.map(T::getId).orElse(0)));
    }


}
