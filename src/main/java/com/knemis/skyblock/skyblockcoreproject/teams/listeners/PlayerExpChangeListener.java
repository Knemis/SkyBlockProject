package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamEnhancement;
import com.keviin.keviinteams.enhancements.Enhancement;
import com.keviin.keviinteams.enhancements.ExperienceEnhancementData;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

@AllArgsConstructor
public class PlayerExpChangeListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    //Could cause dupe's of xp if they have a plugin to deposit xp
    @EventHandler(ignoreCancelled = true)
    public void onPlayerExperienceChange(PlayerExpChangeEvent event) {
        keviinTeams.getTeamManager().getTeamViaID(keviinTeams.getUserManager().getUser(event.getPlayer()).getTeamID()).ifPresent(team -> {
            Enhancement<ExperienceEnhancementData> spawnerEnhancement = keviinTeams.getEnhancements().experienceEnhancement;
            TeamEnhancement teamEnhancement = keviinTeams.getTeamManager().getTeamEnhancement(team, "experience");
            ExperienceEnhancementData data = spawnerEnhancement.levels.get(teamEnhancement.getLevel());

            if (!teamEnhancement.isActive(spawnerEnhancement.type)) return;
            if (data == null) return;

            event.setAmount((int) (event.getAmount() * data.experienceModifier));
        });
    }
}
