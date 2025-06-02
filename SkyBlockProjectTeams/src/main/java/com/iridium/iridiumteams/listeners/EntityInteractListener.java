
package com.keviin.keviinteams.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;

@AllArgsConstructor
public class EntityInteractListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, SettingType.CROP_TRAMPLE.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled") && (XMaterial.matchXMaterial(event.getBlock().getType()) == XMaterial.FARMLAND)) {
                event.setCancelled(true);
            }
        });
    }
}
