package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

@AllArgsConstructor
public class BlockBurnListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {

        keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation()).ifPresent(team -> {
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team, SettingType.FIRE_SPREAD.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled")) {
                event.setCancelled(true);
            }
        });
    }

}