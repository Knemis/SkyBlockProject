package com.knemis.skyblock.skyblockcoreproject.api;

// Assuming com.knemis.skyblock.skyblockcoreproject.island.Island is the correct Island class for this project
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import lombok.Getter;
import org.bukkit.entity.Player; // Changed from Iridium User
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class IslandDeleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @NotNull private final Island island; // Changed to this project's Island class
    @NotNull private final Player user;   // Changed to Bukkit Player

    public IslandDeleteEvent(@NotNull Island island, @NotNull Player user) {
        this.island = island;
        this.user = user;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @NotNull
    public Island getIsland() {
        return island;
    }

    @NotNull
    public Player getUser() {
        return user;
    }
}
