package com.keviin.keviinteams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.api.SettingUpdateEvent;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

@AllArgsConstructor
public class SettingUpdateListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler
    public void onSettingUpdate(SettingUpdateEvent<T, U> event) {
        if (event.getSetting().equalsIgnoreCase(SettingType.TIME.getSettingKey())) {
            keviinTeams.getTeamManager().getTeamMembers(event.getTeam()).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(player ->
                    keviinTeams.getTeamManager().sendTeamTime(player)
            );
        }
        if (event.getSetting().equalsIgnoreCase(SettingType.WEATHER.getSettingKey())) {
            keviinTeams.getTeamManager().getTeamMembers(event.getTeam()).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(player ->
                    keviinTeams.getTeamManager().sendTeamWeather(player)
            );
        }
    }

}
