package com.knemis.skyblock.skyblockcoreproject.missions;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MissionGUIManager {
    private final SkyBlockProject plugin;
    public static final String MAIN_GUI_TITLE_PREFIX = "Missions - ";
    public static final NamespacedKey MISSION_ID_KEY = new NamespacedKey(SkyBlockProject.getPlugin(SkyBlockProject.class), "mission_id");
    public static final NamespacedKey GUI_ACTION_KEY = new NamespacedKey(SkyBlockProject.getPlugin(SkyBlockProject.class), "gui_action");
    public static final NamespacedKey GUI_PAGE_KEY = new NamespacedKey(SkyBlockProject.getPlugin(SkyBlockProject.class), "gui_page");
    public static final NamespacedKey GUI_CATEGORY_KEY = new NamespacedKey(SkyBlockProject.getPlugin(SkyBlockProject.class), "gui_category");


    private final int MISSIONS_PER_PAGE = 45;

    public MissionGUIManager(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    public void openMainMissionGui(Player player, MissionCategory categoryToShow, int page) {
        plugin.getLogger().info(String.format("[MissionGUIManager] Opening main mission GUI for player %s (UUID: %s), Category: %s, Page: %d",
                player.getName(), player.getUniqueId(), categoryToShow.name(), page));

        MissionManager missionManager = plugin.getMissionManager();
        PlayerMissionData playerData = plugin.getMissionPlayerDataManager().getPlayerData(player.getUniqueId());

        if (missionManager == null || playerData == null) {
            plugin.getLogger().severe(String.format("[MissionGUIManager] Failed to open GUI for %s: MissionManager (%s) or PlayerMissionData (%s) is null.",
                    player.getName(), missionManager == null ? "null" : "ok", playerData == null ? "null" : "ok"));
            player.sendMessage(ChatColor.RED + "An error occurred while opening the missions GUI. Please contact an administrator.");
            return;
        }

        List<Mission> missionsToDisplay;
        switch (categoryToShow) {
            case ACTIVE:
                missionsToDisplay = playerData.getActiveMissions().keySet().stream()
                        .map(missionManager::getMission)
                        .filter(java.util.Objects::nonNull)
                        .sorted(Comparator.comparing(Mission::getName))
                        .collect(Collectors.toList());
                break;
            case COMPLETED:
                missionsToDisplay = playerData.getCompletedMissions().stream()
                        .map(missionManager::getMission)
                        .filter(java.util.Objects::nonNull)
                        .sorted(Comparator.comparing(Mission::getName))
                        .collect(Collectors.toList());
                break;
            case AVAILABLE:
            default:
                missionsToDisplay = missionManager.getAllMissions().stream()
                        .filter(mission -> missionManager.canStartMission(player, mission))
                        .sorted(Comparator.comparing(Mission::getName))
                        .collect(Collectors.toList());
                break;
        }

        int totalPages = (int) Math.ceil((double) missionsToDisplay.size() / MISSIONS_PER_PAGE);
        if (totalPages == 0) totalPages = 1; // Ensure at least one page even if empty
        page = Math.max(1, Math.min(page, totalPages)); // Clamp page number

        String guiTitle = MAIN_GUI_TITLE_PREFIX + categoryToShow.getDisplayName() + " (Page " + page + "/" + totalPages + ")";
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(guiTitle));

        int startIndex = (page - 1) * MISSIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + MISSIONS_PER_PAGE, missionsToDisplay.size());

        for (int i = startIndex; i < endIndex; i++) {
            Mission mission = missionsToDisplay.get(i);
            ItemStack missionItem = new ItemStack(Material.matchMaterial(mission.getIconMaterial()) != null ? Material.matchMaterial(mission.getIconMaterial()) : Material.BOOK);
            ItemMeta meta = missionItem.getItemMeta();
            if (meta == null) continue;

            List<Component> lore = new ArrayList<>();
            NamedTextColor nameColor = NamedTextColor.GRAY;
            String status;

            if (playerData.getActiveMissions().containsKey(mission.getId())) {
                nameColor = NamedTextColor.YELLOW;
                status = ChatColor.YELLOW + "In Progress";
                PlayerMissionProgress progress = playerData.getActiveMissionProgress(mission.getId());
                for (int j = 0; j < mission.getObjectives().size(); j++) {
                    MissionObjective obj = mission.getObjectives().get(j);
                    lore.add(Component.text(" - " + obj.getType() + ": " + obj.getTarget() + " (" + progress.getProgress(j) + "/" + obj.getAmount() + ")", NamedTextColor.GRAY));
                }
            } else if (playerData.isMissionOnCooldown(mission.getId())) {
                nameColor = NamedTextColor.DARK_GRAY;
                long remainingSeconds = (playerData.getCooldownEndTime(mission.getId()) - System.currentTimeMillis()) / 1000;
                status = ChatColor.DARK_GRAY + "On Cooldown (" + formatCooldown(remainingSeconds) + ")";
            } else if (playerData.hasCompletedMission(mission.getId())) {
                nameColor = NamedTextColor.DARK_GRAY; // Or GREEN if you want to show completed differently
                status = ChatColor.DARK_GREEN + "Completed";
                if (!"NONE".equalsIgnoreCase(mission.getRepeatableType())) {
                    status += ChatColor.GRAY + " (Repeatable)";
                }
            } else if (missionManager.canStartMission(player, mission)) {
                nameColor = NamedTextColor.GREEN;
                status = ChatColor.GREEN + "Available";
            } else { // Cannot start (e.g. unmet dependencies, no permission) - canStartMission should provide specific feedback
                nameColor = NamedTextColor.RED;
                status = ChatColor.RED + "Locked";
            }

            meta.displayName(Component.text(mission.getName(), nameColor, TextDecoration.BOLD));


            mission.getDescription().forEach(descLine -> lore.add(Component.text(ChatColor.translateAlternateColorCodes('&', descLine), NamedTextColor.WHITE)));
            lore.add(Component.text(" "));
            lore.add(Component.text("Category: ", NamedTextColor.BLUE).append(Component.text(mission.getCategory(), NamedTextColor.WHITE)));
            lore.add(Component.text("Status: ").append(Component.text(ChatColor.stripColor(status), nameColor))); // Use nameColor for status too

            if (mission.getRewards() != null) {
                lore.add(Component.text("Rewards:", NamedTextColor.GOLD));
                if (mission.getRewards().getMoney() > 0) lore.add(Component.text(" - Money: $" + mission.getRewards().getMoney(), NamedTextColor.GRAY));
                if (mission.getRewards().getExperience() > 0) lore.add(Component.text(" - XP: " + mission.getRewards().getExperience(), NamedTextColor.GRAY));
                mission.getRewards().getItems().forEach(item -> lore.add(Component.text(" - Item: " + item.getType() + " x" + item.getAmount(), NamedTextColor.GRAY)));
            }
            lore.add(Component.text(" "));
            if (categoryToShow == MissionCategory.AVAILABLE) {
                lore.add(Component.text("Click to start!", NamedTextColor.GREEN));
            } else if (categoryToShow == MissionCategory.ACTIVE) {
                lore.add(Component.text("Click to view progress or abandon.", NamedTextColor.YELLOW));
            } else {
                lore.add(Component.text("Click for details.", NamedTextColor.GRAY));
            }


            meta.getPersistentDataContainer().set(MISSION_ID_KEY, PersistentDataType.STRING, mission.getId());
            meta.lore(lore);
            missionItem.setItemMeta(meta);
            gui.setItem(i - startIndex, missionItem);
        }

        // Controls
        // Previous Page
        if (page > 1) {
            gui.setItem(45, createControlButton(Material.ARROW, "Previous Page", "previous_page", page -1, categoryToShow.name()));
        }
        // Category Buttons
        gui.setItem(48, createControlButton(Material.WRITABLE_BOOK, "Available", "category_available", 1, MissionCategory.AVAILABLE.name()));
        gui.setItem(49, createControlButton(Material.BOOK, "Active", "category_active", 1, MissionCategory.ACTIVE.name()));
        gui.setItem(50, createControlButton(Material.ENCHANTED_BOOK, "Completed", "category_completed", 1, MissionCategory.COMPLETED.name()));

        // Next Page
        if (page < totalPages) {
            gui.setItem(53, createControlButton(Material.ARROW, "Next Page", "next_page", page + 1, categoryToShow.name()));
        }

        // Close button (optional, players can usually close with Esc)
        // gui.setItem(49, createControlButton(Material.BARRIER, "Close", "close_gui", 0, categoryToShow.name()));


        player.openInventory(gui);
    }

    private ItemStack createControlButton(Material material, String name, String action, int page, String category) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW));
        meta.getPersistentDataContainer().set(GUI_ACTION_KEY, PersistentDataType.STRING, action);
        meta.getPersistentDataContainer().set(GUI_PAGE_KEY, PersistentDataType.INTEGER, page);
        meta.getPersistentDataContainer().set(GUI_CATEGORY_KEY, PersistentDataType.STRING, category);
        item.setItemMeta(meta);
        return item;
    }

    private String formatCooldown(long totalSeconds) {
        if (totalSeconds <= 0) return "Ready";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) return String.format("%dh %dm %ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds);
        return String.format("%ds", seconds);
    }

    // public void openMissionDetailGui(Player player, Mission mission) { ... } // For future
}