package com.keviin.keviinteams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

@AllArgsConstructor
public class PlayerMoveListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Location to = event.getTo();
        if (to == null) return; // This is possible apparently?

        Location from = event.getFrom();

        // might help speed things up - if the next location does not change blocks, why do anything?
        if ((from.getBlockX() == to.getBlockX()) && (from.getBlockZ() == to.getBlockZ()) && (from.getBlockY() == to.getBlockY())) return;

        Player player = event.getPlayer();

        Optional<T> fromTeam = keviinTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), from);
        Optional<T> toTeam = keviinTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), to);

        if (fromTeam.isPresent()) {
            keviinTeams.getTeamManager().sendTeamTime(player);
            keviinTeams.getTeamManager().sendTeamWeather(player);
        }

        if (toTeam.isPresent() && !keviinTeams.getTeamManager().canVisit(player, toTeam.get())) {
            event.setCancelled(true);
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotVisit
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix))
            );
            return;
        }

        // we should only be checking if the player is flying if the flight enhancement is enabled (this is a global config setting)
        // we're not an anti-cheat, we don't care otherwise
        U user = keviinTeams.getUserManager().getUser(player);
        if (keviinTeams.getEnhancements().flightEnhancement.enabled && user.isFlying()) {
            if (!user.canFly(keviinTeams) && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                user.setFlying(false);
                player.setFlying(false);
                player.setAllowFlight(false);

                player.sendMessage(StringUtils.color(keviinTeams.getMessages().flightDisabled
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix))
                );
            }
        }

        if (!toTeam.isPresent()) return;
        if (!toTeam.map(T::getId).orElse(-99999).equals(fromTeam.map(T::getId).orElse(-99999))) {
            keviinTeams.getTeamManager().sendTeamTitle(player, toTeam.get());
        }
    }
}
