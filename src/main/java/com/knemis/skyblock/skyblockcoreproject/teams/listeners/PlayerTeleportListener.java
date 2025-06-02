package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

@AllArgsConstructor
public class PlayerTeleportListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null) return; // This is possible apparently?
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> toTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player, to);
        Optional<T> fromTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player, from);
        if (user.isFlying() && (to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) && !user.canFly(SkyBlockProjectTeams)) {
            user.setFlying(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().flightDisabled
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            );
        }
        if (!toTeam.isPresent()) return;
        if (!SkyBlockProjectTeams.getTeamManager().canVisit(player, toTeam.get())) {
            event.setCancelled(true);
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotVisit
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))
            );
            return;
        }

        if (!toTeam.map(T::getId).orElse(-1).equals(fromTeam.map(T::getId).orElse(-1))) {
            SkyBlockProjectTeams.getTeamManager().sendTeamTitle(player, toTeam.get());
        }
    }

}
