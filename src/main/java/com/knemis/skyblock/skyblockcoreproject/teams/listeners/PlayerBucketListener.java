
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.Optional;

@AllArgsConstructor
public class PlayerBucketListener<T extends Team, U extends SkyBlockProjectUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        onBucketEvent(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFillEvent(PlayerBucketFillEvent event) {
        onBucketEvent(event);
    }

    public void onBucketEvent(PlayerBucketEvent event) {
        Player player = event.getPlayer();
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player, event.getBlock().getLocation());
        if (team.isPresent()) {
            if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.BUCKET)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotUseBuckets
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }
        }

    }

}
