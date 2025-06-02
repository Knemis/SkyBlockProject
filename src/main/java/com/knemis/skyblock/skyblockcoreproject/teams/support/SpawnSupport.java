package com.knemis.skyblock.skyblockcoreproject.teams.support;

import com.keviin.keviinteams.database.Team;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SpawnSupport<T extends Team> {
    public Location getSpawn(Player player);
    public String supportProvider();
}