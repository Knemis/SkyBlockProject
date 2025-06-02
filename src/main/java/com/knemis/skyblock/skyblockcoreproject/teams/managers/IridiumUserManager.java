package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface keviinUserManager<T extends Team, U extends keviinUser<T>> {

    @NotNull U getUser(@NotNull OfflinePlayer offlinePlayer);

    Optional<U> getUserByUUID(@NotNull UUID uuid);

    List<U> getUsers();
}
