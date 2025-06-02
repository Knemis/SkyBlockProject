
package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockExplodeListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {

        if (!keviinTeams.getConfiguration().preventTntGriefing) return;
        Optional<T> currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());

        if (currentTeam.isPresent()) {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(currentTeam.get(), SettingType.TNT_DAMAGE.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled")) {
                event.setCancelled(true);
                return;
            }
        }

        int currentTeamId = currentTeam.map(T::getId).orElse(0);

        event.blockList().removeIf(blockState -> {
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(currentTeamId) != currentTeamId;
        });
    }

}
