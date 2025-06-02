
package com.keviin.keviinteams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockFertilizeListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Player player = event.getPlayer();

        Optional<T> currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());
        int currentTeamId = currentTeam.map(T::getId).orElse(0);

        if (player != null && currentTeam.isPresent()) {
            U user = keviinTeams.getUserManager().getUser(player);
            if (!keviinTeams.getTeamManager().getTeamPermission(currentTeam.get(), user, PermissionType.BLOCK_PLACE)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotBreakBlocks
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
                return;
            }
        }

        event.getBlocks().removeIf(blockState -> {
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(blockState.getLocation());
            return team.map(T::getId).orElse(currentTeamId) != currentTeamId;
        });
    }

}
