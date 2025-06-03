package com.knemis.skyblock.skyblockcoreproject.teams.api;

import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // Updated IridiumUser to User
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class TeamLevelUpEvent<T extends Team, U extends User<T>> extends Event { // TODO: Update Team to actual class if necessary

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
