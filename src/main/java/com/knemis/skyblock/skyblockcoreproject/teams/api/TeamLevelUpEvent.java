package com.knemis.skyblock.skyblockcoreproject.teams.api;

// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class TeamLevelUpEvent<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Event { // TODO: Update Team and SkyBlockProjectUser to actual classes

    private static final HandlerList handlers = new HandlerList();
    private final T team;
    private final int level;

    public TeamLevelUpEvent(T team, int level) {
        this.team = team;
        this.level = level;
    }

    public boolean isFirstTimeAsLevel() {
        // return team.getExperience() > team.getMaxExperience(); // TODO: Uncomment when Team class is refactored
        return false;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
