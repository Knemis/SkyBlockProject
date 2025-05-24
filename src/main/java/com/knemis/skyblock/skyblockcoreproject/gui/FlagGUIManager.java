package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor; // Legacy renk kodları için hala kullanılabilir, ama Component tercih edilir.
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

// Adventure API importları
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer; // Legacy <-> Component dönüşümü için

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FlagGUIManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandFlagManager islandFlagManager;

    // GUI_TITLE Component olarak tanımlanacak
    public static final Component GUI_TITLE_COMPONENT = Component.text("Ada Bayrakları Yönetimi", Style.style(NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
    // Eski String başlık, event listener'da karşılaştırma için kalabilir veya listener da Component kullanacak şekilde güncellenebilir.
    public static final String GUI_TITLE_STRING = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Ada Bayrakları Yönetimi";


    private final NamespacedKey flagNameKey;

    private static class FlagUIData {
        StateFlag flag;
        Material iconMaterial;
        String baseDisplayNameKey; // Component için anahtar veya direkt Component
        List<String> descriptionLegacy; // Legacy stringler, Component'e dönüştürülecek
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
        this.flagNameKey = new NamespacedKey(plugin, "skyblock_flag_name_key_v3");
        initializeFlagLayout();
    }

    private void initializeFlagLayout() {
        flagLayout.clear();
        // Açıklamalar için Collections.singletonList() veya List.of() (Java 9+) kullanılacak
        flagLayout.add(new FlagUIData(Flags.BUILD, Material.DIAMOND_PICKAXE, "İnşa Etme (Build)",
                Arrays.asList(ChatColor.DARK_AQUA + "Adada blok kırma/koyma izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 10));
        flagLayout.add(new FlagUIData(Flags.INTERACT, Material.LEVER, "Etkileşim (Interact)",
                Arrays.asList(ChatColor.DARK_AQUA + "Kapı, düğme, şalter vb. kullanım izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 11));
        flagLayout.add(new FlagUIData(Flags.CHEST_ACCESS, Material.CHEST, "Sandık Erişimi",
                Arrays.asList(ChatColor.DARK_AQUA + "Sandık, fırın, huni vb. envanterlerine", ChatColor.DARK_AQUA + "erişim izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 12));
        flagLayout.add(new FlagUIData(Flags.USE, Material.BUCKET, "Eşya Kullanımı (Use)",
                Arrays.asList(ChatColor.DARK_AQUA + "Kova, çakmaktaşı, zırh askısı vb.", ChatColor.DARK_AQUA + "kullanım izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 13));

        // Tek argümanlı asList() yerine Collections.singletonList()
        flagLayout.add(new FlagUIData(Flags.ITEM_DROP, Material.DROPPER, "Eşya Atma (Drop)",
                Collections.singletonList(ChatColor.DARK_AQUA + "Adaya eşya atılabilme izni."), 14));
        flagLayout.add(new FlagUIData(Flags.ITEM_PICKUP, Material.HOPPER, "Eşya Toplama",
                Collections.singletonList(ChatColor.DARK_AQUA + "Adadan eşya toplanabilme izni."), 15));

        flagLayout.add(new FlagUIData(Flags.TRAMPLE_BLOCKS, Material.FARMLAND, "Ekin Ezme",
                Arrays.asList(ChatColor.DARK_AQUA + "Ekinlerin üzerinde zıplayarak", ChatColor.DARK_AQUA + "bozulmasını etkiler."), 16));
        flagLayout.add(new FlagUIData(Flags.RIDE, Material.SADDLE, "Binme (Ride)",
                Arrays.asList(ChatColor.DARK_AQUA + "At, domuz gibi binek hayvanlarına", ChatColor.DARK_AQUA + "binme izni."), 17));

        flagLayout.add(new FlagUIData(Flags.PVP, Material.DIAMOND_SWORD, "PVP",
                Arrays.asList(ChatColor.DARK_AQUA + "Adada oyuncuların birbirine", ChatColor.DARK_AQUA + "hasar verme durumu."), 19));
        flagLayout.add(new FlagUIData(Flags.DAMAGE_ANIMALS, Material.LEAD, "Hayvanlara Hasar",
                Arrays.asList(ChatColor.DARK_AQUA + "Adadaki hayvanlara hasar verme durumu."), 20));

        flagLayout.add(new FlagUIData(Flags.TNT, Material.TNT, "TNT Patlaması",
                Collections.singletonList(ChatColor.DARK_AQUA + "TNT'nin bloklara hasar vermesi."), 21));
        flagLayout.add(new FlagUIData(Flags.CREEPER_EXPLOSION, Material.CREEPER_HEAD, "Creeper Patlaması",
                Arrays.asList(ChatColor.DARK_AQUA + "Creeper patlamalarının bloklara", ChatColor.DARK_AQUA + "hasar vermesi."), 22));
        flagLayout.add(new FlagUIData(Flags.OTHER_EXPLOSION, Material.FIRE_CHARGE, "Diğer Patlamalar",
                Arrays.asList(ChatColor.DARK_AQUA + "Ghast ateşi, Wither vb. patlamaların", ChatColor.DARK_AQUA + "bloklara hasar vermesi."), 23));
        flagLayout.add(new FlagUIData(Flags.ENDERPEARL, Material.ENDER_PEARL, "Ender Pearl",
                Collections.singletonList(ChatColor.DARK_AQUA + "Adada ender pearl atılabilmesi."), 24));
        flagLayout.add(new FlagUIData(Flags.POTION_SPLASH, Material.SPLASH_POTION, "İksir Sıçraması",
                Arrays.asList(ChatColor.DARK_AQUA + "Sıçrayan iksirlerin (örn: zarar)", ChatColor.DARK_AQUA + "oyuncuları etkilemesi."), 25));

        flagLayout.add(new FlagUIData(Flags.FIRE_SPREAD, Material.FLINT_AND_STEEL, "Ateş Yayılması",
                Collections.singletonList(ChatColor.DARK_AQUA + "Ateşin bloklara yayılması."), 28));
        flagLayout.add(new FlagUIData(Flags.LAVA_FLOW, Material.LAVA_BUCKET, "Lav Akışı",
                Collections.singletonList(ChatColor.DARK_AQUA + "Lavın adada akması."), 29));
        flagLayout.add(new FlagUIData(Flags.WATER_FLOW, Material.WATER_BUCKET, "Su Akışı",
                Collections.singletonList(ChatColor.DARK_AQUA + "Suyun adada akması."), 30));
        flagLayout.add(new FlagUIData(Flags.MOB_SPAWNING, Material.PIG_SPAWN_EGG, "Yaratık Doğması",
                Arrays.asList(ChatColor.DARK_AQUA + "Adada doğal yaratıkların doğması."), 31));
        flagLayout.add(new FlagUIData(Flags.LEAF_DECAY, Material.OAK_LEAVES, "Yaprak Dökülmesi",
                Arrays.asList(ChatColor.DARK_AQUA + "Ağaç yapraklarının doğal dökülmesi."), 32));
        flagLayout.add(new FlagUIData(Flags.LIGHTNING, Material.BEACON, "Şimşek Çakması",
                Arrays.asList(ChatColor.DARK_AQUA + "Adada şimşek çakması ve etkileri."), 33));
        flagLayout.add(new FlagUIData(Flags.SNOW_FALL, Material.SNOW_BLOCK, "Kar Yağışı (Blok)",
                Arrays.asList(ChatColor.DARK_AQUA + "Kar yağdığında yerde", ChatColor.DARK_AQUA + "kar katmanı oluşması."), 34));
    }

    public void openFlagsGUI(Player player) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cBayraklarını düzenleyebileceğin bir adan yok!"));
            return;
        }

        int guiSize = 54;
        // createInventory için Component başlık kullanıldı
        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE_COMPONENT);

        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            // setDisplayName yerine displayName(Component)
            sepMeta.displayName(Component.text(" "));
            separator.setItemMeta(sepMeta);
        }

        for (int i = 0; i < 9; i++) gui.setItem(i, separator.clone());
        gui.setItem(18, separator.clone());
        gui.setItem(26, separator.clone());
        gui.setItem(36, separator.clone());
        gui.setItem(44, separator.clone());
        for (int i = guiSize - 9; i < guiSize; i++) gui.setItem(i, separator.clone());


        for (FlagUIData flagData : flagLayout) {
            if (flagData.flag == null) {
                plugin.getLogger().warning("[FlagGUIManager] flagData.flag null, atlanıyor. Slot: " + flagData.slot);
                continue;
            }
            StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), flagData.flag);

            Material itemMaterial;
            NamedTextColor statusColorText; // ChatColor yerine NamedTextColor
            String statusName;
            Component nextActionText; // String yerine Component

            if (currentState == StateFlag.State.ALLOW) {
                itemMaterial = Material.LIME_WOOL;
                statusColorText = NamedTextColor.GREEN;
                statusName = "İZİNLİ";
                nextActionText = Component.text("YASAKLI", NamedTextColor.RED).append(Component.text(" Yap", NamedTextColor.GRAY));
            } else if (currentState == StateFlag.State.DENY) {
                itemMaterial = Material.RED_WOOL;
                statusColorText = NamedTextColor.RED;
                statusName = "YASAKLI";
                nextActionText = Component.text("VARSAYILAN", NamedTextColor.GRAY).append(Component.text(" Yap", NamedTextColor.GRAY));
            } else {
                itemMaterial = Material.GRAY_WOOL;
                statusColorText = NamedTextColor.GRAY;
                statusName = "VARSAYILAN";
                nextActionText = Component.text("İZİNLİ", NamedTextColor.GREEN).append(Component.text(" Yap", NamedTextColor.GRAY));
            }

            ItemStack flagItem = new ItemStack(itemMaterial);
            ItemMeta itemMeta = flagItem.getItemMeta();

            if (itemMeta != null) {
                // setDisplayName yerine displayName(Component)
                itemMeta.displayName(Component.text(flagData.baseDisplayNameKey, Style.style(statusColorText, TextDecoration.BOLD)));

                List<Component> loreComponents = new ArrayList<>();
                for (String descLineLegacy : flagData.descriptionLegacy) {
                    loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(descLineLegacy));
                }
                loreComponents.add(Component.text(" "));
                loreComponents.add(Component.text("➢ Mevcut Durum: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(statusName, Style.style(statusColorText, TextDecoration.BOLD))));

                if (currentState == null) {
                    StateFlag.State actualDefaultState = islandFlagManager.getDefaultStateFor(flagData.flag);
                    String actualDefaultString = (actualDefaultState == null) ? "Belirsiz (WG Geneli)" : actualDefaultState.name();
                    loreComponents.add(Component.text("  (Gerçek Değer: " + actualDefaultString + ")", NamedTextColor.DARK_GRAY));
                }
                loreComponents.add(Component.text("➢ Tıkla: ").color(NamedTextColor.AQUA)
                        .append(nextActionText));
                loreComponents.add(Component.text(" "));
                loreComponents.add(Component.text("Bayrak ID: ", NamedTextColor.DARK_PURPLE)
                        .append(Component.text(flagData.flag.getName(), NamedTextColor.LIGHT_PURPLE)));

                // setLore yerine lore(List<Component>)
                itemMeta.lore(loreComponents);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.getPersistentDataContainer().set(flagNameKey, PersistentDataType.STRING, flagData.flag.getName());
                flagItem.setItemMeta(itemMeta);
            }

            if (flagData.slot >= 0 && flagData.slot < gui.getSize()) {
                gui.setItem(flagData.slot, flagItem);
            } else {
                if(plugin.getConfig().getBoolean("logging.detailed-flag-changes", false)) {
                    plugin.getLogger().warning("[FlagGUIManager] Geçersiz slot ("+ flagData.slot +") için bayrak '" + flagData.baseDisplayNameKey + "' eklenemedi. GUI Boyutu: " + gui.getSize());
                }
            }
        }
        player.openInventory(gui);
    }

    public NamespacedKey getFlagNameKey() {
        return flagNameKey;
    }
}