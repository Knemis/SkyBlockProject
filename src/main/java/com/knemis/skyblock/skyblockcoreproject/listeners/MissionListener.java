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

public class MissionListener implements Listener {

    private final SkyBlockProject plugin;

    public MissionListener(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("Player " + player.getName() + " joining, loading mission data...");
        plugin.getMissionPlayerDataManager().loadPlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("Player " + player.getName() + " quitting, saving mission data...");
        plugin.getMissionPlayerDataManager().savePlayerData(player.getUniqueId(), true);
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String viewTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(event.getView().title());

        if (viewTitle.startsWith(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MAIN_GUI_TITLE_PREFIX)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == org.bukkit.Material.AIR) return;

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.getPersistentDataContainer().has(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MISSION_ID_KEY, org.bukkit.persistence.PersistentDataType.STRING) &&
                    !meta.getPersistentDataContainer().has(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_ACTION_KEY, org.bukkit.persistence.PersistentDataType.STRING)) {
                return;
            }

            com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager missionGUIManager = plugin.getMissionGUIManager();
            com.knemis.skyblock.skyblockcoreproject.missions.MissionManager missionManager = plugin.getMissionManager();

            String missionId = meta.getPersistentDataContainer().get(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MISSION_ID_KEY, org.bukkit.persistence.PersistentDataType.STRING);
            String guiAction = meta.getPersistentDataContainer().get(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_ACTION_KEY, org.bukkit.persistence.PersistentDataType.STRING);
            int page = meta.getPersistentDataContainer().getOrDefault(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_PAGE_KEY, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
            String categoryName = meta.getPersistentDataContainer().get(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.GUI_CATEGORY_KEY, org.bukkit.persistence.PersistentDataType.STRING);

            com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory currentCategory = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.AVAILABLE; // Default
            try {
                if (categoryName != null) currentCategory = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If categoryName from item is invalid, stick to default or parse from title
                String titleCategoryPart = viewTitle.replace(com.knemis.skyblock.skyblockcoreproject.missions.MissionGUIManager.MAIN_GUI_TITLE_PREFIX, "").split(" \\(")[0];
                for(com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory cat : com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.values()){
                    if(cat.getDisplayName().equalsIgnoreCase(titleCategoryPart)){
                        currentCategory = cat;
                        break;
                    }
                }
            }


            if (missionId != null) {
                com.knemis.skyblock.skyblockcoreproject.missions.Mission mission = missionManager.getMission(missionId);
                if (mission == null) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Error: Could not find mission details.");
                    return;
                }

                com.knemis.skyblock.skyblockcoreproject.missions.PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());

                if (playerData.getActiveMissionProgress(missionId) != null) {
                    // Active mission clicked - for now, just a message. Later, detail/abandon GUI.
                    player.sendMessage(org.bukkit.ChatColor.YELLOW + "Mission '" + mission.getName() + "' is in progress. (Details/Abandon not yet implemented)");
                    // missionGUIManager.openMissionDetailGui(player, mission);
                } else if (playerData.hasCompletedMission(missionId) && !"NONE".equalsIgnoreCase(mission.getRepeatableType()) && !playerData.isMissionOnCooldown(mission.getId())) {
                    // Completed but repeatable and not on cooldown
                    if (missionManager.startMission(player, mission)) {
                        player.closeInventory();
                        missionGUIManager.openMainMissionGui(player, currentCategory, page); // Refresh GUI
                    }
                } else if (playerData.hasCompletedMission(missionId)) {
                    player.sendMessage(org.bukkit.ChatColor.GRAY + "You have already completed '" + mission.getName() + "'.");
                    if(playerData.isMissionOnCooldown(mission.getId())){
                        long remainingSeconds = (playerData.getCooldownEndTime(mission.getId()) - System.currentTimeMillis()) / 1000;
                        player.sendMessage(org.bukkit.ChatColor.GRAY + "It's on cooldown for: " + formatCooldown(remainingSeconds));
                    }
                } else if (missionManager.canStartMission(player, mission)) {
                    if (missionManager.startMission(player, mission)) {
                        player.closeInventory(); // Close and reopen to refresh, or update in place if possible
                        missionGUIManager.openMainMissionGui(player, com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.ACTIVE, 1); // Switch to active tab
                    }
                } else {
                    // canStartMission would have sent a message, or it's locked for other reasons.
                    player.sendMessage(org.bukkit.ChatColor.RED + "Cannot interact with this mission at the moment.");
                }

            } else if (guiAction != null) {
                com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory targetCategory = currentCategory;
                if (guiAction.startsWith("category_")) {
                    targetCategory = com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory.valueOf(guiAction.substring("category_".length()).toUpperCase());
                    page = 1; // Reset to page 1 when changing category
                }
                missionGUIManager.openMainMissionGui(player, targetCategory, page);
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
}