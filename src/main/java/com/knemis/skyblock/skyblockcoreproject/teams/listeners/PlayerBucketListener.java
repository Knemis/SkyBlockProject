
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.Optional;

@AllArgsConstructor
public class PlayerBucketListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

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
        U user = keviinTeams.getUserManager().getUser(player);
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaPlayerLocation(player, event.getBlock().getLocation());
        if (team.isPresent()) {
            if (!keviinTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.BUCKET)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotUseBuckets
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }
        }

    }

}
