package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
// Assuming IslandLifecycleManager or a new IslandBorderManager would handle sending border visuals
// For now, IslandLifecycleManager is imported but its direct use for sending border is commented out.
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SkyBlockProject plugin;
    private final boolean createIslandOnJoin;

    public PlayerJoinListener(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.createIslandOnJoin = plugin.getConfig().getBoolean("settings.island.create-on-first-join", false); // Adjusted path
    }

    @EventHandler(priority = EventPriority.MONITOR) // MONITOR to act after other plugins potentially set up the player
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (createIslandOnJoin && !player.hasPlayedBefore()) {
            // Ensure the command is run by the player themselves for correct context
            // Make sure the command does not include a leading slash
            String commandToRun = plugin.getConfig().getString("commands.island-create.on-join-command", "island create");
            if (commandToRun.startsWith("/")) {
                commandToRun = commandToRun.substring(1);
            }
            Bukkit.dispatchCommand(player, commandToRun);
        }

        // Regarding sending island border information:
        // The original Iridium code had a direct `getTeamManager().sendIslandBorder(event.getPlayer())`.
        // SkyBlockProject's `IslandLifecycleManager` creates WorldGuard regions.
        // Visual borders are often client-side effects that WorldGuard can trigger based on region entry/awareness,
        // or they might require a custom packet solution if a persistent visual is desired without client mods.
        //
        // If SkyBlockProject relies on WorldGuard to show region borders (e.g., via WG's own visualization commands
        // or on region entry if WG has such a feature), then no specific 'send' call might be needed here.
        // Players would see borders as they interact with or enter regions as per WG's setup.
        //
        // If a custom border visualization is intended (like the particle borders some plugins use),
        // SkyBlockProject would need a dedicated manager for that (e.g., IslandDisplayManager or IslandBorderManager).

        // TODO: Determine how island borders are visually presented in SkyBlockProject.
        // If it's automatic via WorldGuard region awareness on the client, no action is needed.
        // If it requires a specific trigger (e.g., sending particle packets), that logic
        // would need to be called here, likely through a dedicated manager.
        // Example:
        // IslandBorderManager borderManager = plugin.getIslandBorderManager(); // Hypothetical
        // if (borderManager != null) {
        //     borderManager.showBorderToPlayer(player);
        // }

        // For now, logging that this is the point where such an action would occur.
        // plugin.getLogger().info("PlayerJoinListener: Player " + player.getName() + " joined. Island border display/update would be handled here if applicable.");
    }
}
