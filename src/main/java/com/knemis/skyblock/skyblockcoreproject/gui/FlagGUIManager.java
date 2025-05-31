package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.knemis.skyblock.skyblockcoreproject.utils.CustomFlags; // Added for new custom flag
import com.sk89q.worldguard.protection.flags.Flags;
// import com.sk89q.worldguard.protection.flags.RegionGroup; // No longer managing groups
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlagGUIManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandFlagManager islandFlagManager;

    // Single general GUI title
    public static final Component GUI_TITLE_COMPONENT = Component.text("Island Flag Management", Style.style(NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
    // public static final String GUI_TITLE_STRING = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Island Flag Management"; // Old string for listener - REMOVED as it's unused by FlagGUIListener

    private final NamespacedKey flagNameKey;
    // private final NamespacedKey flagGroupKey; // Removed

    private static class FlagUIData {
        StateFlag flag;
        Material iconMaterial;
        String baseDisplayNameKey;
        List<String> descriptionLegacy;
        int slot;

        FlagUIData(StateFlag flag, Material iconMaterial, String baseDisplayNameKey, List<String> descriptionLegacy, int slot) {
            this.flag = flag;
            this.iconMaterial = iconMaterial;
            this.baseDisplayNameKey = baseDisplayNameKey;
            this.descriptionLegacy = descriptionLegacy;
            this.slot = slot;
        }
    }

    private final List<FlagUIData> flagLayout = new ArrayList<>();

    public FlagGUIManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandFlagManager islandFlagManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandFlagManager = islandFlagManager;

        if (this.islandFlagManager == null) {
            plugin.getLogger().severe("!!! CRITICAL ERROR: IslandFlagManager is NULL in FlagGUIManager !!!");
        }
        this.flagNameKey = new NamespacedKey(plugin, "skyblock_flag_name_key_v4");
        // this.flagGroupKey = new NamespacedKey(plugin, "skyblock_flag_group_key_v1"); // Removed
        initializeFlagLayout();
    }

    private void initializeFlagLayout() {
        flagLayout.clear();
        // Manageable flags are obtained from IslandFlagManager.
        // The descriptions of these flags should now reflect a general setting.
        flagLayout.add(new FlagUIData(Flags.BUILD, Material.DIAMOND_PICKAXE, "Building (Build)",
                Arrays.asList(ChatColor.DARK_AQUA + "Breaking/placing blocks on the island (General)."), 10));
        flagLayout.add(new FlagUIData(Flags.INTERACT, Material.LEVER, "Interaction (Interact)",
                Arrays.asList(ChatColor.DARK_AQUA + "Using doors, buttons, levers, etc. (General)."), 11));
        flagLayout.add(new FlagUIData(Flags.CHEST_ACCESS, Material.CHEST, "Chest Access",
                Arrays.asList(ChatColor.DARK_AQUA + "Accessing chests, furnaces, etc. inventories (General)."), 12));
        flagLayout.add(new FlagUIData(Flags.USE, Material.BUCKET, "Item Usage (Use)",
                Arrays.asList(ChatColor.DARK_AQUA + "Using buckets, flint and steel, etc. (General)."), 13));
        flagLayout.add(new FlagUIData(Flags.ITEM_DROP, Material.DROPPER, "Item Dropping (Drop)",
                Collections.singletonList(ChatColor.DARK_AQUA + "Ability to drop items on the island (General)."), 14));
        flagLayout.add(new FlagUIData(Flags.ITEM_PICKUP, Material.HOPPER, "Item Pickup",
                Collections.singletonList(ChatColor.DARK_AQUA + "Ability to pick up items from the island (General)."), 15));
        flagLayout.add(new FlagUIData(Flags.PVP, Material.DIAMOND_SWORD, "PVP",
                Arrays.asList(ChatColor.DARK_AQUA + "Player versus player damage (General)."), 19));
        // ... Other flag definitions are updated similarly ...
        flagLayout.add(new FlagUIData(Flags.TNT, Material.TNT, "TNT Explosion",
                Collections.singletonList(ChatColor.DARK_AQUA + "TNT causing damage to blocks (General)."), 21));
        flagLayout.add(new FlagUIData(Flags.ENDERPEARL, Material.ENDER_PEARL, "Ender Pearl",
                Collections.singletonList(ChatColor.DARK_AQUA + "Ability to throw ender pearls on the island (General)."), 24));
        flagLayout.add(new FlagUIData(Flags.MOB_SPAWNING, Material.PIG_SPAWN_EGG, "Mob Spawning",
                Arrays.asList(ChatColor.DARK_AQUA + "Natural mob spawning on the island (General)."), 31));

        // Dynamically assigning slots and flags to display using IslandFlagManager.getManagableFlags()
        // might be more flexible, but for now, we are using a fixed list.
    }

    /**
     * Opens the general island flags management GUI.
     * @param player The player who will open the GUI (usually the island owner).
     */
    public void openFlagsGUI(Player player) { // groupContext parameter removed
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cYou don't have an island whose flags you can edit!"));
            return;
        }

        int guiSize = 54; // 6 rows
        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE_COMPONENT); // Single general title used

        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.displayName(Component.text(" "));
            separator.setItemMeta(sepMeta);
        }

        // Separator placement can remain the same
        for (int i = 0; i < 9; i++) gui.setItem(i, separator.clone());
        for (int i = guiSize - 9; i < guiSize; i++) gui.setItem(i, separator.clone());
        for (int i = 9; i < guiSize - 9; i += 9) {
            gui.setItem(i, separator.clone());
            gui.setItem(i + 8, separator.clone());
        }

        for (FlagUIData flagData : flagLayout) {
            if (flagData.flag == null) continue;

            // Query flag status generally, without group
            StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), flagData.flag);
            // Query default status generally, without group
            StateFlag.State actualDefaultState = islandFlagManager.getDefaultStateForFlag(flagData.flag);

            Material itemMaterial;
            NamedTextColor statusColorText;
            String statusName;
            Component nextActionText;

            if (currentState == StateFlag.State.ALLOW) {
                itemMaterial = Material.LIME_WOOL; statusColorText = NamedTextColor.GREEN; statusName = "ALLOWED";
                nextActionText = Component.text("DENIED", NamedTextColor.RED).append(Component.text(" Set", NamedTextColor.GRAY));
            } else if (currentState == StateFlag.State.DENY) {
                itemMaterial = Material.RED_WOOL; statusColorText = NamedTextColor.RED; statusName = "DENIED";
                nextActionText = Component.text("DEFAULT", NamedTextColor.GRAY).append(Component.text(" Set", NamedTextColor.GRAY));
            } else { // null (DEFAULT)
                itemMaterial = Material.GRAY_WOOL; statusColorText = NamedTextColor.GRAY; statusName = "DEFAULT";
                nextActionText = Component.text("ALLOWED", NamedTextColor.GREEN).append(Component.text(" Set", NamedTextColor.GRAY));
            }

            ItemStack flagItem = new ItemStack(itemMaterial);
            ItemMeta itemMeta = flagItem.getItemMeta();

            if (itemMeta != null) {
                itemMeta.displayName(Component.text(flagData.baseDisplayNameKey, Style.style(statusColorText, TextDecoration.BOLD)));

                List<Component> loreComponents = new ArrayList<>();
                flagData.descriptionLegacy.forEach(line -> loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
                loreComponents.add(Component.text(" "));
                loreComponents.add(Component.text("➢ Current Status: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(statusName, Style.style(statusColorText, TextDecoration.BOLD))));

                if (currentState == null) { // If current state is DEFAULT, show the actual underlying default
                    String actualDefaultString = (actualDefaultState == null) ? "Global WG Setting" : actualDefaultState.name();
                    loreComponents.add(Component.text("  (Actual Value: " + actualDefaultString + ")", NamedTextColor.DARK_GRAY));
                }
                loreComponents.add(Component.text("➢ Click: ").color(NamedTextColor.AQUA).append(nextActionText));
                loreComponents.add(Component.text(" "));
                loreComponents.add(Component.text("Flag ID: ", NamedTextColor.DARK_PURPLE).append(Component.text(flagData.flag.getName(), NamedTextColor.LIGHT_PURPLE)));

                itemMeta.lore(loreComponents);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.getPersistentDataContainer().set(flagNameKey, PersistentDataType.STRING, flagData.flag.getName());
                // itemMeta.getPersistentDataContainer().set(flagGroupKey, PersistentDataType.STRING, groupContext.name()); // Removed

                flagItem.setItemMeta(itemMeta);
            }

            if (flagData.slot >= 0 && flagData.slot < gui.getSize()) {
                gui.setItem(flagData.slot, flagItem);
            }
        }
        player.openInventory(gui);
    }

    public NamespacedKey getFlagNameKey() {
        return flagNameKey;
    }
    // public NamespacedKey getFlagGroupKey() { // Removed
    // return flagGroupKey;
    // }
}