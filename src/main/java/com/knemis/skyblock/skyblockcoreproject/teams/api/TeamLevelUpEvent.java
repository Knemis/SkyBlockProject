package com.knemis.skyblock.skyblockcoreproject.teams.api;

import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class TeamLevelUpEvent<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final T team;
    private final int level;

    public TeamLevelUpEvent(T team, int level) {
        this.team = team;
        this.level = level;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isFirstTimeAsLevel() {
        return team.getExperience() > team.getMaxExperience();
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
