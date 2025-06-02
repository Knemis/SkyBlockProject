package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

@AllArgsConstructor
public class EntityDamageListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) return;
        Player player = (Player) damager;
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(event.getEntity().getLocation());
        if (team.isPresent()) {
            if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team.get(), user, PermissionType.KILL_MOBS)) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotKillMobs
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                event.setCancelled(true);
            }
        }
    }
}
