package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

@AllArgsConstructor
public class PlayerTeleportListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null) return; // This is possible apparently?
        U user = keviinTeams.getUserManager().getUser(player);
        Optional<T> toTeam = keviinTeams.getTeamManager().getTeamViaPlayerLocation(player, to);
        Optional<T> fromTeam = keviinTeams.getTeamManager().getTeamViaPlayerLocation(player, from);
        if (user.isFlying() && (to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) && !user.canFly(keviinTeams)) {
            user.setFlying(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().flightDisabled
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix))
            );
        }
        if (!toTeam.isPresent()) return;
        if (!keviinTeams.getTeamManager().canVisit(player, toTeam.get())) {
            event.setCancelled(true);
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotVisit
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix))
            );
            return;
        }

        if (!toTeam.map(T::getId).orElse(-1).equals(fromTeam.map(T::getId).orElse(-1))) {
            keviinTeams.getTeamManager().sendTeamTitle(player, toTeam.get());
        }
    }

}
