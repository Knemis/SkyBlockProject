package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldguard.protection.flags.Flags;
// import com.sk89q.worldguard.protection.flags.RegionGroup; // Artık grup yönetimi yok
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

    // Tek bir genel GUI başlığı
    public static final Component GUI_TITLE_COMPONENT = Component.text("Ada Bayrakları Yönetimi", Style.style(NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
    public static final String GUI_TITLE_STRING = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Ada Bayrakları Yönetimi"; // Listener için eski string

    private final NamespacedKey flagNameKey;
    // private final NamespacedKey flagGroupKey; // Kaldırıldı

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
            plugin.getLogger().severe("!!! KRİTİK HATA: IslandFlagManager, FlagGUIManager içinde NULL !!!");
        }
        this.flagNameKey = new NamespacedKey(plugin, "skyblock_flag_name_key_v4");
        // this.flagGroupKey = new NamespacedKey(plugin, "skyblock_flag_group_key_v1"); // Kaldırıldı
        initializeFlagLayout();
    }

    private void initializeFlagLayout() {
        flagLayout.clear();
        // Yönetilebilir bayraklar IslandFlagManager'dan alınır.
        // Bu bayrakların açıklamaları artık genel bir ayarı yansıtmalı.
        flagLayout.add(new FlagUIData(Flags.BUILD, Material.DIAMOND_PICKAXE, "İnşa Etme (Build)",
                Arrays.asList(ChatColor.DARK_AQUA + "Adada blok kırma/koyma (Genel)."), 10));
        flagLayout.add(new FlagUIData(Flags.INTERACT, Material.LEVER, "Etkileşim (Interact)",
                Arrays.asList(ChatColor.DARK_AQUA + "Kapı, düğme, şalter vb. kullanım (Genel)."), 11));
        flagLayout.add(new FlagUIData(Flags.CHEST_ACCESS, Material.CHEST, "Sandık Erişimi",
                Arrays.asList(ChatColor.DARK_AQUA + "Sandık, fırın vb. envanterlere erişim (Genel)."), 12));
        flagLayout.add(new FlagUIData(Flags.USE, Material.BUCKET, "Eşya Kullanımı (Use)",
                Arrays.asList(ChatColor.DARK_AQUA + "Kova, çakmaktaşı vb. kullanım (Genel)."), 13));
        flagLayout.add(new FlagUIData(Flags.ITEM_DROP, Material.DROPPER, "Eşya Atma (Drop)",
                Collections.singletonList(ChatColor.DARK_AQUA + "Adaya eşya atılabilme (Genel)."), 14));
        flagLayout.add(new FlagUIData(Flags.ITEM_PICKUP, Material.HOPPER, "Eşya Toplama",
                Collections.singletonList(ChatColor.DARK_AQUA + "Adadan eşya toplanabilme (Genel)."), 15));
        flagLayout.add(new FlagUIData(Flags.PVP, Material.DIAMOND_SWORD, "PVP",
                Arrays.asList(ChatColor.DARK_AQUA + "Oyuncular arası hasar (Genel)."), 19));
        // ... Diğer bayrak tanımlamaları benzer şekilde güncellenir ...
        flagLayout.add(new FlagUIData(Flags.TNT, Material.TNT, "TNT Patlaması",
                Collections.singletonList(ChatColor.DARK_AQUA + "TNT'nin bloklara hasar vermesi (Genel)."), 21));
        flagLayout.add(new FlagUIData(Flags.ENDERPEARL, Material.ENDER_PEARL, "Ender Pearl",
                Collections.singletonList(ChatColor.DARK_AQUA + "Adada ender pearl atılabilmesi (Genel)."), 24));
        flagLayout.add(new FlagUIData(Flags.MOB_SPAWNING, Material.PIG_SPAWN_EGG, "Yaratık Doğması",
                Arrays.asList(ChatColor.DARK_AQUA + "Adada doğal yaratıkların doğması (Genel)."), 31));

        // Slot atamalarını ve gösterilecek bayrakları IslandFlagManager.getManagableFlags() ile
        // dinamik olarak yapmak daha esnek olabilir, ancak şimdilik sabit liste kullanıyoruz.
    }

    /**
     * Genel ada bayrakları yönetim GUI'sini açar.
     * @param player GUI'yi açacak oyuncu (genellikle ada sahibi).
     */
    public void openFlagsGUI(Player player) { // groupContext parametresi kaldırıldı
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cBayraklarını düzenleyebileceğin bir adan yok!"));
            return;
        }

        int guiSize = 54; // 6 sıra
        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE_COMPONENT); // Tek genel başlık kullanıldı

        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.displayName(Component.text(" "));
            separator.setItemMeta(sepMeta);
        }

        // Ayırıcıların yerleşimi aynı kalabilir
        for (int i = 0; i < 9; i++) gui.setItem(i, separator.clone());
        for (int i = guiSize - 9; i < guiSize; i++) gui.setItem(i, separator.clone());
        for (int i = 9; i < guiSize - 9; i += 9) {
            gui.setItem(i, separator.clone());
            gui.setItem(i + 8, separator.clone());
        }

        for (FlagUIData flagData : flagLayout) {
            if (flagData.flag == null) continue;

            // Bayrak durumunu grup olmadan, genel olarak sorgula
            StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), flagData.flag);
            // Varsayılan durumu grup olmadan, genel olarak sorgula
            StateFlag.State actualDefaultState = islandFlagManager.getDefaultStateForFlag(flagData.flag);

            Material itemMaterial;
            NamedTextColor statusColorText;
            String statusName;
            Component nextActionText;

            if (currentState == StateFlag.State.ALLOW) {
                itemMaterial = Material.LIME_WOOL; statusColorText = NamedTextColor.GREEN; statusName = "İZİNLİ";
                nextActionText = Component.text("YASAKLI", NamedTextColor.RED).append(Component.text(" Yap", NamedTextColor.GRAY));
            } else if (currentState == StateFlag.State.DENY) {
                itemMaterial = Material.RED_WOOL; statusColorText = NamedTextColor.RED; statusName = "YASAKLI";
                nextActionText = Component.text("VARSAYILAN", NamedTextColor.GRAY).append(Component.text(" Yap", NamedTextColor.GRAY));
            } else {
                itemMaterial = Material.GRAY_WOOL; statusColorText = NamedTextColor.GRAY; statusName = "VARSAYILAN";
                nextActionText = Component.text("İZİNLİ", NamedTextColor.GREEN).append(Component.text(" Yap", NamedTextColor.GRAY));
            }

            ItemStack flagItem = new ItemStack(itemMaterial);
            ItemMeta itemMeta = flagItem.getItemMeta();

            if (itemMeta != null) {
                itemMeta.displayName(Component.text(flagData.baseDisplayNameKey, Style.style(statusColorText, TextDecoration.BOLD)));

                List<Component> loreComponents = new ArrayList<>();
                flagData.descriptionLegacy.forEach(line -> loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
                loreComponents.add(Component.text(" "));
                loreComponents.add(Component.text("➢ Mevcut Durum: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(statusName, Style.style(statusColorText, TextDecoration.BOLD))));

                if (currentState == null) {
                    String actualDefaultString = (actualDefaultState == null) ? "Genel WG Ayarı" : actualDefaultState.name();
                    loreComponents.add(Component.text("  (Gerçek Değer: " + actualDefaultString + ")", NamedTextColor.DARK_GRAY));
                }
                loreComponents.add(Component.text("➢ Tıkla: ").color(NamedTextColor.AQUA).append(nextActionText));
                loreComponents.add(Component.text(" "));
                loreComponents.add(Component.text("Bayrak ID: ", NamedTextColor.DARK_PURPLE).append(Component.text(flagData.flag.getName(), NamedTextColor.LIGHT_PURPLE)));

                itemMeta.lore(loreComponents);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.getPersistentDataContainer().set(flagNameKey, PersistentDataType.STRING, flagData.flag.getName());
                // itemMeta.getPersistentDataContainer().set(flagGroupKey, PersistentDataType.STRING, groupContext.name()); // Kaldırıldı

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
    // public NamespacedKey getFlagGroupKey() { // Kaldırıldı
    // return flagGroupKey;
    // }
}