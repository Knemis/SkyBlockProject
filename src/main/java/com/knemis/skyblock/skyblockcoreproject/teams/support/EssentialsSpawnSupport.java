package com.knemis.skyblock.skyblockcoreproject.teams.support;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsSpawnSupport<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements SpawnSupport<T> {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    EssentialsSpawn essentialsSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
    Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

    public EssentialsSpawnSupport(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
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
