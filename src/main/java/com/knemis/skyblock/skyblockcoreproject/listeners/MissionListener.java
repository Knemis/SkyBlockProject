package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material; // For Material.AIR comparison
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.entity.EntityDeathEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.EntityType;
import java.util.regex.Pattern;

public class MissionListener implements Listener {

    private final SkyBlockProject plugin;

    public MissionListener(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info(String.format("Player %s (UUID: %s) joining, loading mission data...", player.getName(), player.getUniqueId()));
        plugin.getMissionPlayerDataManager().loadPlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info(String.format("Player %s (UUID: %s) quitting, saving mission data...", player.getName(), player.getUniqueId()));
        plugin.getMissionPlayerDataManager().savePlayerData(player.getUniqueId(), true);
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String viewTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(event.getView().title());

        if (viewTitle.startsWith(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MAIN_GUI_TITLE_PREFIX)) {
            ItemStack clickedItem = event.getCurrentItem();
            String clickedItemName = (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) ?
                                     net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(clickedItem.getItemMeta().displayName()) :
                                     (clickedItem != null ? clickedItem.getType().name() : "null");

            plugin.getLogger().info(String.format("Player %s (UUID: %s) clicked in Mission GUI: '%s', Slot: %d, Item: %s",
                    player.getName(), player.getUniqueId(), viewTitle, event.getSlot(), clickedItemName));

            event.setCancelled(true);
            if (clickedItem == null || clickedItem.getType() == org.bukkit.Material.AIR) {
                plugin.getLogger().info(String.format("MissionListener: Player %s (UUID: %s) clicked on an empty slot in Mission GUI '%s'. Slot: %d",
                        player.getName(), player.getUniqueId(), viewTitle, event.getSlot()));
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || (!meta.getPersistentDataContainer().has(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MISSION_ID_KEY, org.bukkit.persistence.PersistentDataType.STRING) &&
                                !meta.getPersistentDataContainer().has(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_ACTION_KEY, org.bukkit.persistence.PersistentDataType.STRING))) {
                plugin.getLogger().warning(String.format("MissionListener: Player %s (UUID: %s) clicked on an item ('%s') without mission ID or GUI action in Mission GUI '%s'. Slot: %d",
                        player.getName(), player.getUniqueId(), clickedItemName, viewTitle, event.getSlot()));
                return;
            }

            com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager missionGUIManager = plugin.getMissionGUIManager();
            com.knemis.skyblock.skyblockcoreproject.missions.MissionManager missionManager = plugin.getMissionManager();

            if (missionGUIManager == null || missionManager == null) {
                plugin.getLogger().severe(String.format("MissionListener: MissionGUIManager or MissionManager is null for player %s (UUID: %s) in GUI click. GUI: %s",
                        player.getName(), player.getUniqueId(), viewTitle));
                player.sendMessage(Component.text("An error occurred with the mission system. Please contact an admin.", NamedTextColor.RED));
                return;
            }

            String missionId = meta.getPersistentDataContainer().get(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MISSION_ID_KEY, org.bukkit.persistence.PersistentDataType.STRING);
            String guiAction = meta.getPersistentDataContainer().get(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_ACTION_KEY, org.bukkit.persistence.PersistentDataType.STRING);
            int page = meta.getPersistentDataContainer().getOrDefault(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_PAGE_KEY, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
            String categoryName = meta.getPersistentDataContainer().get(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_CATEGORY_KEY, org.bukkit.persistence.PersistentDataType.STRING);

            com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory currentCategory = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.AVAILABLE; // Default
            try {
                if (categoryName != null) currentCategory = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(String.format("MissionListener: Invalid categoryName '%s' from item clicked by %s. Attempting to parse from title '%s'.",
                        categoryName, player.getName(), viewTitle));
                String titleCategoryPart = viewTitle.replace(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MAIN_GUI_TITLE_PREFIX, "").split(" \\(")[0];
                boolean found = false;
                for(com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory cat : com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.values()){
                    if(cat.getDisplayName().equalsIgnoreCase(titleCategoryPart)){
                        currentCategory = cat;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    plugin.getLogger().warning(String.format("MissionListener: Could not determine category from title part '%s' for player %s. Defaulting to AVAILABLE.", titleCategoryPart, player.getName()));
                }
            }


            if (missionId != null) {
                com.knemis.skyblock.skyblockcoreproject.missions.Mission mission = missionManager.getMission(missionId);
                if (mission == null) {
                    player.sendMessage(Component.text("Error: Could not find mission details.", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("MissionListener: Player %s (UUID: %s) clicked on mission item in GUI, but mission ID '%s' is not found by MissionManager.",
                            player.getName(), player.getUniqueId(), missionId));
                    return;
                }

                com.knemis.skyblock.skyblockcoreproject.missions.PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());

                if (playerData.getActiveMissionProgress(missionId) != null) {
                    // Check if the current view is the "Active" missions category
                    String currentViewTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(event.getView().title());
                    boolean isActiveCategoryView = currentViewTitle.contains(com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.ACTIVE.getDisplayName());

                    if (isActiveCategoryView) {
                        plugin.getLogger().info(String.format("[MissionListener] Player %s (UUID: %s) clicked to abandon active mission '%s' (ID: %s) in GUI.", player.getName(), player.getUniqueId(), mission.getName(), missionId));
                        plugin.getMissionManager().abandonMission(player, missionId); // Call the abandonMission method

                        // Refresh the GUI
                        // Extract current page from title, default to 1 if not parsable
                        int currentPageFromTitle = 1;
                        String pageString = currentViewTitle.substring(currentViewTitle.lastIndexOf("Page ") + 5, currentViewTitle.lastIndexOf("/"));
                        try {
                            currentPageFromTitle = Integer.parseInt(pageString);
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("[MissionListener] Could not parse page number from title: " + currentViewTitle + ". Defaulting to page 1 for refresh.");
                        }
                        com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory categoryForRefresh = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.ACTIVE;
                         String categoryNameFromTitle = currentViewTitle.replace(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MAIN_GUI_TITLE_PREFIX, "").split(java.util.regex.Pattern.quote(" ("))[0];
                            try {
                                categoryForRefresh = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.fromString(categoryNameFromTitle);
                            } catch (IllegalArgumentException e) {
                                 plugin.getLogger().warning("[MissionListener] Could not parse category from title: " + currentViewTitle + " for refresh. Defaulting to ACTIVE.");
                            }

                        final int finalCurrentPage = currentPageFromTitle; // Effectively final for lambda/runnable
                        final com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory finalCurrentCategory = categoryForRefresh;
                        // Refresh GUI on next tick to allow inventory event to complete
                        org.bukkit.scheduler.BukkitRunnable runnable = new org.bukkit.scheduler.BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.getMissionGUIManager().openMainMissionGui(player, finalCurrentCategory, finalCurrentPage);
                            }
                        };
                        runnable.runTask(plugin);

                    } else {
                        // If not in the "Active" category view, perhaps just show info or do nothing different
                        player.sendMessage(Component.text("Mission '" + mission.getName() + "' is in progress. View it in the 'Active' category to manage.", NamedTextColor.YELLOW));
                        plugin.getLogger().info(String.format("[MissionListener] Player %s (UUID: %s) clicked active mission '%s' (ID: %s) in a non-active category view ('%s').", player.getName(), player.getUniqueId(), mission.getName(), missionId, currentViewTitle));
                    }
                } else if (playerData.hasCompletedMission(missionId) && !"NONE".equalsIgnoreCase(mission.getRepeatableType()) && !playerData.isMissionOnCooldown(mission.getId())) {
                    if (missionManager.startMission(player, mission)) { // startMission logs success/failure
                        plugin.getLogger().info(String.format("Player %s (UUID: %s) started repeatable mission '%s' (ID: %s) via GUI click.", player.getName(), player.getUniqueId(), mission.getName(), missionId));
                        player.closeInventory();
                        missionGUIManager.openMainMissionGui(player, currentCategory, page); 
                    }
                } else if (playerData.hasCompletedMission(missionId)) {
                    player.sendMessage(Component.text("You have already completed '" + mission.getName() + "'.", NamedTextColor.GRAY));
                    if(playerData.isMissionOnCooldown(mission.getId())){
                        long remainingSeconds = (playerData.getCooldownEndTime(mission.getId()) - System.currentTimeMillis()) / 1000;
                        player.sendMessage(Component.text("It's on cooldown for: " + formatCooldown(remainingSeconds), NamedTextColor.GRAY));
                    }
                    plugin.getLogger().info(String.format("Player %s (UUID: %s) clicked already completed (repeatable: %s, on cooldown: %s) mission '%s' (ID: %s) in GUI.",
                            player.getName(), player.getUniqueId(), mission.getRepeatableType(), playerData.isMissionOnCooldown(mission.getId()), mission.getName(), missionId));
                } else if (missionManager.canStartMission(player, mission)) {
                    if (missionManager.startMission(player, mission)) { // startMission logs success/failure
                        plugin.getLogger().info(String.format("Player %s (UUID: %s) started new mission '%s' (ID: %s) via GUI click.", player.getName(), player.getUniqueId(), mission.getName(), missionId));
                        player.closeInventory(); 
                        missionGUIManager.openMainMissionGui(player, com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.ACTIVE, 1); 
                    }
                } else {
                    // canStartMission would have sent a message (and potentially logged)
                    player.sendMessage(Component.text("Cannot interact with this mission at the moment.", NamedTextColor.RED));
                    plugin.getLogger().info(String.format("Player %s (UUID: %s) clicked mission '%s' (ID: %s) in GUI, but cannot start it (canStartMission=false or other reason).",
                            player.getName(), player.getUniqueId(), mission.getName(), missionId));
                }

            } else if (guiAction != null) {
                com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory targetCategory = currentCategory;
                String originalAction = guiAction; // For logging
                if (guiAction.startsWith("category_")) {
                    try {
                        targetCategory = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.valueOf(guiAction.substring("category_".length()).toUpperCase());
                        page = 1; // Reset to page 1 when changing category
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning(String.format("MissionListener: Player %s (UUID: %s) clicked GUI item with invalid category action '%s'. Defaulting to %s",
                                player.getName(), player.getUniqueId(), guiAction, currentCategory.name()));
                        // targetCategory remains currentCategory
                    }
                }
                // For page changes like "next_page", "prev_page", page is already extracted from item NBT.
                // MissionGUIManager.openMainMissionGui will handle page boundaries.
                missionGUIManager.openMainMissionGui(player, targetCategory, page);
                plugin.getLogger().info(String.format("Player %s (UUID: %s) performed GUI action: '%s' (Target Category: %s, Target Page: %d) in Mission GUI.",
                        player.getName(), player.getUniqueId(), originalAction, targetCategory.name(), page));
            }
        }
    }

    private String formatCooldown(long totalSeconds) { // Duplicated from MissionManager, consider a utility class
        if (totalSeconds <= 0) return "Ready";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) return String.format("%dh %dm %ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds);
        return String.format("%ds", seconds);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if the killer is a player
        if (event.getEntity().getKiller() == null) {
            return; // Not a player kill
        }

        Player player = event.getEntity().getKiller();
        EntityType killedEntityType = event.getEntityType();

        System.out.println("[TRACE] MissionListener.onEntityDeath: Player " + player.getName() + " killed entity " + killedEntityType.name());

        // Call MissionManager to update progress
        if (plugin.getMissionManager() != null) {
            plugin.getMissionManager().updateKillMobProgress(player, killedEntityType);
        } else {
            System.out.println("[TRACE] MissionListener.onEntityDeath: MissionManager is null, cannot update kill progress for " + player.getName());
            // Log a warning or error if MissionManager should always be available
            plugin.getLogger().warning("[MissionListener] MissionManager was null when trying to update kill progress for player " + player.getName());
        }
    }
}