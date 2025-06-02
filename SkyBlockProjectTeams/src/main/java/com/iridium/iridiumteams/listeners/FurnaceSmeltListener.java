
package com.keviin.keviinteams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

@AllArgsConstructor
public class FurnaceSmeltListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitorFurnaceSmelt(FurnaceSmeltEvent event) {
        XMaterial material = XMaterial.matchXMaterial(event.getSource().getType());

        keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            keviinTeams.getMissionManager().handleMissionUpdate(team, event.getBlock().getLocation().getWorld(), "SMELT", material.name(), 1);
        });

    }

}
