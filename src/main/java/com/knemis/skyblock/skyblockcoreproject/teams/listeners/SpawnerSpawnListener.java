package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamEnhancement;
import com.keviin.keviinteams.enhancements.Enhancement;
import com.keviin.keviinteams.enhancements.SpawnerEnhancementData;
import lombok.AllArgsConstructor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

@AllArgsConstructor
public class SpawnerSpawnListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(SpawnerSpawnEvent event) {
        keviinTeams.getTeamManager().getTeamViaLocation(event.getLocation()).ifPresent(team -> {
            Enhancement<SpawnerEnhancementData> spawnerEnhancement = keviinTeams.getEnhancements().spawnerEnhancement;
            TeamEnhancement teamEnhancement = keviinTeams.getTeamManager().getTeamEnhancement(team, "spawner");
            SpawnerEnhancementData data = spawnerEnhancement.levels.get(teamEnhancement.getLevel());
            CreatureSpawner spawner = event.getSpawner();

            if (!teamEnhancement.isActive(spawnerEnhancement.type)) return;
            if (data == null) return;

            spawner.setSpawnCount((spawner.getSpawnCount() * data.spawnMultiplier) + data.spawnCount);
            spawner.update(true);
        });
    }
}
