
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

@AllArgsConstructor
public class EntitySpawnListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Optional<T> currentTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getEntity().getLocation());
        if (currentTeam.isPresent()) {
            TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(currentTeam.get(), SettingType.MOB_SPAWNING.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled") && event.getEntity() instanceof LivingEntity && event.getEntityType() != EntityType.ARMOR_STAND) {
                event.setCancelled(true);
            }
        }

        event.getEntity().setMetadata("team_spawned", new FixedMetadataValue(SkyBlockProjectTeams, currentTeam.map(T::getId).orElse(0)));
    }


}
