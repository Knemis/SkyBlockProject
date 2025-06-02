
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockFertilizeListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Player player = event.getPlayer();

        Optional<T> currentTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());
        int currentTeamId = currentTeam.map(T::getId).orElse(0);

        if (player != null && currentTeam.isPresent()) {
            U user = SkyBlockProjectTeams.getUserManager().getUser(player);
            if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(currentTeam.get(), user, PermissionType.BLOCK_PLACE)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotBreakBlocks
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
                return;
            }
        }

        event.getBlocks().removeIf(blockState -> {
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(currentTeamId) != currentTeamId;
        });
    }

}
