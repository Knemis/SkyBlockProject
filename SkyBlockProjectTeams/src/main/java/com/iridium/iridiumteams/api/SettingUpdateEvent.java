package com.keviin.keviinteams.api;

import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class SettingUpdateEvent<T extends Team, U extends keviinUser<T>> extends Event {

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