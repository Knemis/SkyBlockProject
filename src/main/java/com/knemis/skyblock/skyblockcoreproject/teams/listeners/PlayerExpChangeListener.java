package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamEnhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.ExperienceEnhancementData;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

@AllArgsConstructor
public class PlayerExpChangeListener<T extends Team, U extends SkyBlockProjectUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    //Could cause dupe's of xp if they have a plugin to deposit xp
    @EventHandler(ignoreCancelled = true)
    public void onPlayerExperienceChange(PlayerExpChangeEvent event) {
        SkyBlockProjectTeams.getTeamManager().getTeamViaID(SkyBlockProjectTeams.getUserManager().getUser(event.getPlayer()).getTeamID()).ifPresent(team -> {
            Enhancement<ExperienceEnhancementData> spawnerEnhancement = SkyBlockProjectTeams.getEnhancements().experienceEnhancement;
            TeamEnhancement teamEnhancement = SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, "experience");
            ExperienceEnhancementData data = spawnerEnhancement.levels.get(teamEnhancement.getLevel());

            if (!teamEnhancement.isActive(spawnerEnhancement.type)) return;
            if (data == null) return;

            event.setAmount((int) (event.getAmount() * data.experienceModifier));
        });
    }
}
