package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkyBlockProjectUserManager<T extends Team, U extends SkyBlockProjectUser<T>> {

    @NotNull U getUser(@NotNull OfflinePlayer offlinePlayer);

    Optional<U> getUserByUUID(@NotNull UUID uuid);

    List<U> getUsers();
}
