package com.keviin.keviinteams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

@AllArgsConstructor
public class EntityDamageListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) return;
        Player player = (Player) damager;
        U user = keviinTeams.getUserManager().getUser(player);
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(event.getEntity().getLocation());
        if (team.isPresent()) {
            if (!keviinTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.KILL_MOBS)) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotKillMobs
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }
        }
    }
}
