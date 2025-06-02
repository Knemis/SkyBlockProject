
package com.knemis.skyblock.skyblockcoreproject.teams.listeners;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.cryptomorin.xseries.XMaterial;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

@AllArgsConstructor
public class EnchantItemListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorItemEnchant(EnchantItemEvent event) {
        U user = SkyBlockProjectTeams.getUserManager().getUser(event.getEnchanter());
        XMaterial material = XMaterial.matchXMaterial(event.getItem().getType());
        SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            SkyBlockProjectTeams.getMissionManager().handleMissionUpdate(team, event.getEnchanter().getLocation().getWorld(), "ENCHANT", material.name(), 1);
        });

    }

}
