
package com.keviin.keviinteams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

@AllArgsConstructor
public class EnchantItemListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorItemEnchant(EnchantItemEvent event) {
        U user = keviinTeams.getUserManager().getUser(event.getEnchanter());
        XMaterial material = XMaterial.matchXMaterial(event.getItem().getType());
        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, event.getEnchanter().getLocation().getWorld(), "ENCHANT", material.name(), 1);
        });

    }

}
