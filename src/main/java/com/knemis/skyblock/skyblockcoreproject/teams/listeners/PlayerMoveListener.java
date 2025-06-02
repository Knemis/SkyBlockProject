package com.knemis.skyblock.skyblockcoreproject.teams.listeners;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

@AllArgsConstructor
public class PlayerMoveListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Location to = event.getTo();
        if (to == null) return; // This is possible apparently?

        Location from = event.getFrom();

        // might help speed things up - if the next location does not change blocks, why do anything?
        if ((from.getBlockX() == to.getBlockX()) && (from.getBlockZ() == to.getBlockZ()) && (from.getBlockY() == to.getBlockY())) return;

        Player player = event.getPlayer();

        Optional<T> fromTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), from);
        Optional<T> toTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(event.getPlayer(), to);

        if (fromTeam.isPresent()) {
            SkyBlockProjectTeams.getTeamManager().sendTeamTime(player);
            SkyBlockProjectTeams.getTeamManager().sendTeamWeather(player);
        }

        if (toTeam.isPresent() && !SkyBlockProjectTeams.getTeamManager().canVisit(player, toTeam.get())) {
            event.setCancelled(true);
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotVisit
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            );
            return;
        }

        // we should only be checking if the player is flying if the flight enhancement is enabled (this is a global config setting)
        // we're not an anti-cheat, we don't care otherwise
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        if (SkyBlockProjectTeams.getEnhancements().flightEnhancement.enabled && user.isFlying()) {
            if (!user.canFly(SkyBlockProjectTeams) && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                user.setFlying(false);
                player.setFlying(false);
                player.setAllowFlight(false);

                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().flightDisabled
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
                );
            }
        }

        if (!toTeam.isPresent()) return;
        if (!toTeam.map(T::getId).orElse(-99999).equals(fromTeam.map(T::getId).orElse(-99999))) {
            SkyBlockProjectTeams.getTeamManager().sendTeamTitle(player, toTeam.get());
        }
    }
}
