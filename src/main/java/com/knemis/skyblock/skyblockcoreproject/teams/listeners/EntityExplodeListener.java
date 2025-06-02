
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class EntityExplodeListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!keviinTeams.getConfiguration().preventTntGriefing) return;
        List<MetadataValue> list = event.getEntity().getMetadata("team_spawned");
        Optional<T> currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getEntity().getLocation());

        if (currentTeam.isPresent()) {
            TeamSetting tntDisabled = keviinTeams.getTeamManager().getTeamSetting(currentTeam.get(), SettingType.TNT_DAMAGE.getSettingKey());
            TeamSetting entityGriefDisabled = keviinTeams.getTeamManager().getTeamSetting(currentTeam.get(), SettingType.ENTITY_GRIEF.getSettingKey());

            if (tntDisabled == null && entityGriefDisabled == null) return;
            boolean isTnTDisabled = tntDisabled != null && tntDisabled.getValue().equalsIgnoreCase("Disabled");
            boolean isEntityGriefDisabled = entityGriefDisabled != null && entityGriefDisabled.getValue().equalsIgnoreCase("Disabled");
            if (isTnTDisabled || isEntityGriefDisabled) {
                event.setCancelled(true);
                return;
            }
        }

        int originalTeamId = list.stream().map(MetadataValue::asInt).findFirst().orElse(0);

        event.blockList().removeIf(blockState -> {
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(originalTeamId) != originalTeamId;
        });
    }

}
