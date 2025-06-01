package com.knemis.skyblock.skyblockcoreproject.api;

import lombok.Getter;
import org.bukkit.entity.Player; // Changed from original User class
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class IslandCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @Nullable
    private String islandName;
    @NotNull
    private final Player user; // Changed to Bukkit Player
    @NotNull
    private PlaceholderSchematicConfig schematicConfig; // Changed to a placeholder

    // Placeholder for SchematicConfig
    // This will need to be replaced or properly defined later.
    public static class PlaceholderSchematicConfig {
        public String name;
        // Add other fields as necessary from the original Schematics.SchematicConfig if known
        public PlaceholderSchematicConfig(String name) {
            this.name = name;
        }
    }

    public IslandCreateEvent(@NotNull Player user, @Nullable String islandName, @NotNull PlaceholderSchematicConfig schematicConfig) {
        this.islandName = islandName;
        this.user = user;
        this.schematicConfig = schematicConfig;
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

    @Nullable
    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(@Nullable String islandName) {
        this.islandName = islandName;
    }

    @NotNull
    public PlaceholderSchematicConfig getSchematicConfig() {
        return schematicConfig;
    }

    public void setSchematicConfig(@NotNull PlaceholderSchematicConfig schematicConfig) {
        this.schematicConfig = schematicConfig;
    }
}
