
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;


import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockExplodeListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {

        if (!SkyBlockProjectTeams.getConfiguration().preventTntGriefing) return;
        Optional<T> currentTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());

        if (currentTeam.isPresent()) {
            TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(currentTeam.get(), SettingType.TNT_DAMAGE.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled")) {
                event.setCancelled(true);
                return;
            }
        }

        int currentTeamId = currentTeam.map(T::getId).orElse(0);

        event.blockList().removeIf(blockState -> {
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(currentTeamId) != currentTeamId;
        });
    }

}
