package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final SkyBlockProject plugin;
    private final boolean allowPvPOnIslands;
    private final String cannotHurtPlayersMessage;

    public EntityDamageListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.allowPvPOnIslands = plugin.getConfig().getBoolean("settings.pvp.allow-on-islands", false);
        String prefix = plugin.getConfig().getString("messages.prefix", "&b[SkyBlock] &r"); // Example prefix
        this.cannotHurtPlayersMessage = ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.pvp.cannot-hurt-players", "&cYou cannot hurt players here."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (allowPvPOnIslands) return; // If PvP is allowed on islands, this listener does nothing further in this method.
        if (!(event.getEntity() instanceof Player)) return; // We only care about players being damaged.

        Player victim = (Player) event.getEntity();
        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        if (islandDataHandler == null) return;

        Island islandAtVictimLocation = islandDataHandler.getIslandAt(victim.getLocation());

        if (islandAtVictimLocation == null) return; // Victim is not on any island's protected area.

        // If global island PvP is disabled, and the event is NOT EntityDamageByEntityEvent (e.g., fall, fire, etc.)
        // We need to check if the original logic (protecting visitors on islands they don't own) should apply.
        // Original: if (user.getTeamID() != island.get().getId()) { event.setCancelled(true); }
        // This implies visitors are protected from *all* damage types on islands not their own if global pvp is off.
        if (!(event instanceof EntityDamageByEntityEvent)) {
            Island victimOwnIsland = islandDataHandler.getIslandByOwner(victim.getUniqueId());
            if (victimOwnIsland == null || !victimOwnIsland.getRegionId().equals(islandAtVictimLocation.getRegionId())) {
                // Victim is on an island, but it's not their own island (or they have no island).
                // Cancel environmental damage to visitors on islands if global island PvP is off.
                event.setCancelled(true);
            }
        }
        // EntityDamageByEntityEvent is handled in the method below for more specific PvP/PvE logic.
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (allowPvPOnIslands) return; // If PvP is allowed on islands, this listener does nothing further.

        IslandDataHandler islandDataHandler = plugin.getIslandDataHandler();
        if (islandDataHandler == null) return;

        Island islandAtVictimLocation = islandDataHandler.getIslandAt(event.getEntity().getLocation());
        if (islandAtVictimLocation == null) return; // Victim/Entity not on an island's protected area.

        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (event.getEntity() instanceof Player) { // Victim is a Player
            Player victim = (Player) event.getEntity();
            if (attacker != null) { // Attacker is a Player (or projectile shot by player) -> PvP
                // Global island PvP is off, and this is a PvP situation on an island.
                event.setCancelled(true);
                if (!attacker.equals(victim)) { // Don't send message for self-harm attempts
                    attacker.sendMessage(cannotHurtPlayersMessage);
                }
            } else { // Attacker is a Mob (or other non-player entity), Victim is a Player -> PvE
                // Check if the victim is a visitor on this island.
                // If so, cancel damage (mobs shouldn't hurt visitors if global island PvP is off).
                Island victimOwnIsland = islandDataHandler.getIslandByOwner(victim.getUniqueId());
                if (victimOwnIsland == null || !victimOwnIsland.getRegionId().equals(islandAtVictimLocation.getRegionId())) {
                    // Victim is on an island that is not their own (visitor).
                    event.setCancelled(true);
                }
                // If the victim is on their own island, mobs can hurt them (standard PvE).
            }
        } else { // Victim is a Mob/Entity (not a player)
            if (attacker != null) { // Attacker is a Player -> PvE
                // Player attacking a Mob on an island.
                // This is generally allowed even if PvP is off. No cancellation needed here
                // unless specific island flags for PvE are introduced later.
            }
            // Mob vs Mob on island - not typically handled by this type of listener.
        }
    }
}
