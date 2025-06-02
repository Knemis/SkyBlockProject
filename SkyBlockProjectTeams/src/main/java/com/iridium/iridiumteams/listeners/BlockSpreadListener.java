package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamSetting;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.Optional;

@AllArgsConstructor
public class BlockSpreadListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        int currentTeam = keviinTeams.getTeamManager().getTeamViaLocation(event.getSource().getLocation()).map(T::getId).orElse(0);
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaLocation(event.getBlock().getLocation());
        if (team.map(T::getId).orElse(currentTeam) != currentTeam) {
            event.setCancelled(true);
        }
        if(team.isPresent() && event.getSource().getType() == Material.FIRE){
            TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team.get(), SettingType.FIRE_SPREAD.getSettingKey());
            if (teamSetting == null) return;
            if (teamSetting.getValue().equalsIgnoreCase("Disabled")) {
                event.setCancelled(true);
            }
        }
    }

}