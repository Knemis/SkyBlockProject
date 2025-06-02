package com.knemis.skyblock.skyblockcoreproject.teams.support;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsSpawnSupport<T extends Team, U extends keviinUser<T>> implements SpawnSupport<T> {

    private final keviinTeams<T, U> keviinTeams;

    EssentialsSpawn essentialsSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
    Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

    public EssentialsSpawnSupport(keviinTeams<T, U> keviinTeams) {
        this.keviinTeams = keviinTeams;
    }

    @Override
    public String supportProvider() {
        return essentialsSpawn.getName();
    }

    @Override
    public Location getSpawn(Player player) {
        if (essentialsSpawn != null && essentials != null) return essentialsSpawn.getSpawn(essentials.getUser(player).getGroup());
        else return Bukkit.getWorlds().get(0).getSpawnLocation();
    }
}
