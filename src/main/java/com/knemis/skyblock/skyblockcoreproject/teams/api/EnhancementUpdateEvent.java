package com.knemis.skyblock.skyblockcoreproject.teams.api;

// import com.keviin.keviinteams.database.keviinUser; // TODO: Update to new package
// import com.keviin.keviinteams.database.Team; // TODO: Update to new package
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class EnhancementUpdateEvent<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Event implements Cancellable { // TODO: Update Team and IridiumUser to actual classes

    private static final HandlerList handlers = new HandlerList();
    private T team;
    private U user;
    private int nextLevel;
    private String enhancement;
    private boolean cancelled;

    public EnhancementUpdateEvent(T team, U user, int nextLevel, String enhancement) {
        this.team = team;
        this.user = user;
        this.nextLevel = nextLevel;
        this.enhancement = enhancement;
        this.cancelled = false;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}