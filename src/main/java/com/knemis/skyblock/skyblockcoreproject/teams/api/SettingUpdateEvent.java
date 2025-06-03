package com.knemis.skyblock.skyblockcoreproject.teams.api;

import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // Updated IridiumUser to User
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class SettingUpdateEvent<T extends Team, U extends User<T>> extends Event { // TODO: Update Team to actual class if necessary

    private static final HandlerList handlers = new HandlerList();
    private final T team;
    private final U user;
    private final String setting;
    private final String value;

    public SettingUpdateEvent(T team, U user, String setting, String value) {
        this.team = team;
        this.user = user;
        this.setting = setting;
        this.value = value;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}