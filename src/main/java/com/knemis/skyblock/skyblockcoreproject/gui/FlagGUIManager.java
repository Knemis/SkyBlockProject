package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import com.sk89q.worldguard.protection.flags.Flag; // Değişiklik
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
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
import java.util.Arrays;
import java.util.List;

public class FlagGUIManager {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;
    public static final String GUI_TITLE = ChatColor.DARK_BLUE + "Ada Bayrakları Yönetimi";
    private final NamespacedKey flagNameKey;
    private final List<FlagDisplayInfo> flagLayout = new ArrayList<>(); // Bu satır zaten olmalı

    // EĞER BU INNER CLASS TANIMI EKSİKSE, BURAYA EKLEYİN:
    // vv==================================================================vv
    private static class FlagDisplayInfo { // FlagUIData yerine FlagDisplayInfo kullanmıştım, aynı şey
        StateFlag flag;
        Material material;
        String displayName;
        List<String> lore;
        int slot;
        // IslandManager.FlagTargetType targetType; // Bir önceki basitleştirilmiş modelde bunu kaldırmıştık

        // Constructor'ı da targetType olmadan (basitleştirilmiş model için)
        FlagDisplayInfo(StateFlag flag, Material material, String displayName, List<String> lore, int slot) {
            this.flag = flag;
            this.material = material;
            this.displayName = displayName;
            this.lore = lore;
            this.slot = slot;
        }
    }
    // ^^==================================================================^^

    public FlagGUIManager(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.flagNameKey = new NamespacedKey(plugin, "skyblock_flag_name_key");
        initializeFlagLayout(); // flagLayout'u dolduran metodu çağır
    }

    private void initializeFlagLayout() {
        // Bayrakları ve GUI bilgilerini burada FlagDisplayInfo kullanarak tanımla
        // Örnek (targetType olmadan, çünkü FlagGUIListener ve IslandManager'ı basitleştirmiştik):
        flagLayout.add(new FlagDisplayInfo(Flags.BUILD, Material.BRICKS, ChatColor.GREEN + "İnşa İzni", Arrays.asList(ChatColor.GRAY + "Adanızda blok kırıp koymayı", ChatColor.GRAY + "herkes için ayarlar."), 10));
        flagLayout.add(new FlagDisplayInfo(Flags.INTERACT, Material.LEVER, ChatColor.YELLOW + "Etkileşim", Arrays.asList(ChatColor.GRAY + "Kapı, düğme vb. ile etkileşimi", ChatColor.GRAY + "herkes için ayarlar."), 11));
        flagLayout.add(new FlagDisplayInfo(Flags.CHEST_ACCESS, Material.CHEST, ChatColor.GOLD + "Sandık Erişimi", Arrays.asList(ChatColor.GRAY + "Sandıklara erişimi", ChatColor.GRAY + "herkes için ayarlar."), 12));
        flagLayout.add(new FlagDisplayInfo(Flags.USE, Material.WATER_BUCKET, ChatColor.AQUA + "Eşya Kullanımı", Arrays.asList(ChatColor.GRAY + "Kova, çakmaktaşı vb. eşyaların", ChatColor.GRAY + "kullanımını herkes için ayarlar."), 13));
        flagLayout.add(new FlagDisplayInfo(Flags.ITEM_DROP, Material.DROPPER, ChatColor.GRAY + "Eşya Atma (Drop)", Arrays.asList(ChatColor.GRAY + "Adaya eşya atılmasını", ChatColor.GRAY + "herkes için ayarlar."), 14));
        flagLayout.add(new FlagDisplayInfo(Flags.ITEM_PICKUP, Material.HOPPER, ChatColor.GRAY + "Eşya Toplama", Arrays.asList(ChatColor.GRAY + "Adadan eşya toplanmasını", ChatColor.GRAY + "herkes için ayarlar."), 15));
        flagLayout.add(new FlagDisplayInfo(Flags.ENDERPEARL, Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "Ender Pearl", Arrays.asList(ChatColor.GRAY + "Adada ender pearl kullanımını", ChatColor.GRAY + "herkes için ayarlar."), 16));

        // Çevresel Bayraklar (Ada genelini etkiler, sahip dahil)
        flagLayout.add(new FlagDisplayInfo(Flags.PVP, Material.DIAMOND_SWORD, ChatColor.DARK_RED + "PVP", Arrays.asList(ChatColor.GRAY + "Adada oyuncular arası dövüşü", ChatColor.GRAY + "herkes için ayarlar."), 28));
        flagLayout.add(new FlagDisplayInfo(Flags.TNT, Material.TNT, ChatColor.RED + "TNT Patlaması", Arrays.asList(ChatColor.GRAY + "TNT'nin patlayıp hasar vermesini", ChatColor.GRAY + "herkes için ayarlar."), 29));
        flagLayout.add(new FlagDisplayInfo(Flags.CREEPER_EXPLOSION, Material.CREEPER_HEAD, ChatColor.DARK_GREEN + "Creeper Patlaması", Arrays.asList(ChatColor.GRAY + "Creeper patlamalarının adaya", ChatColor.GRAY + "hasar vermesini ayarlar."), 30));
        flagLayout.add(new FlagDisplayInfo(Flags.MOB_SPAWNING, Material.SPAWNER, ChatColor.DARK_PURPLE + "Yaratık Doğması", Arrays.asList(ChatColor.GRAY + "Adada doğal yaratık doğmasını", ChatColor.GRAY + "herkes için ayarlar."), 31));
        flagLayout.add(new FlagDisplayInfo(Flags.LAVA_FLOW, Material.LAVA_BUCKET, ChatColor.RED + "Lav Akışı", Arrays.asList(ChatColor.GRAY + "Adada lavın akıp akmayacağını", ChatColor.GRAY + "ayarlar."), 32));
        flagLayout.add(new FlagDisplayInfo(Flags.WATER_FLOW, Material.WATER_BUCKET, ChatColor.BLUE + "Su Akışı", Arrays.asList(ChatColor.GRAY + "Adada suyun akıp akmayacağını", ChatColor.GRAY + "ayarlar."), 33));
        flagLayout.add(new FlagDisplayInfo(Flags.FIRE_SPREAD, Material.FLINT_AND_STEEL, ChatColor.DARK_RED + "Ateş Yayılması", Arrays.asList(ChatColor.GRAY + "Adada ateşin yayılıp", ChatColor.GRAY + "yayılmayacağını ayarlar."), 34));
        flagLayout.add(new FlagDisplayInfo(Flags.LEAF_DECAY, Material.OAK_LEAVES, ChatColor.DARK_GREEN + "Yaprak Dökülmesi", Arrays.asList(ChatColor.GRAY + "Ağaç yapraklarının doğal", ChatColor.GRAY + "olarak dökülmesini ayarlar."), 35)); // Slotu düzelttim, çakışmasın.
        // Diğer bayrakları da bu listeye ekleyebilirsin.
    }

    // openFlagsGUI, addFlagItem, getStatusString, getStatusColor, getFlagNameKey metodları
    // bir önceki "FlagGUIManager tam halini atar mısın" başlıklı cevabımdaki gibi kalacak.
    // ÖNEMLİ: addFlagItem metodu artık FlagDisplayInfo'dan gelen bilgileri kullanacak.

    public void openFlagsGUI(Player player) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Bayraklarını düzenleyebileceğin bir adan yok!");
            return;
        }
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE); // 6 satır

        for (FlagDisplayInfo flagInfo : flagLayout) {
            // Bayrağın mevcut durumunu al
            StateFlag.State currentState = islandManager.getIslandFlagState(player.getUniqueId(), flagInfo.flag);
            String statusString = getStatusString(currentState);
            ChatColor statusColor = getStatusColor(currentState);

            Material itemMaterial;
            if (currentState == StateFlag.State.ALLOW) {
                itemMaterial = Material.LIME_WOOL;
            } else if (currentState == StateFlag.State.DENY) {
                itemMaterial = Material.RED_WOOL;
            } else {
                itemMaterial = Material.GRAY_WOOL;
            }

            ItemStack flagItem = new ItemStack(itemMaterial);
            ItemMeta itemMeta = flagItem.getItemMeta();

            if (itemMeta != null) {
                itemMeta.setDisplayName(flagInfo.displayName); // FlagDisplayInfo'dan al
                List<String> lore = new ArrayList<>(flagInfo.lore); // FlagDisplayInfo'dan al
                lore.add("");
                lore.add(ChatColor.YELLOW + "Mevcut Durum: " + statusColor + statusString);
                lore.add("");
                lore.add(ChatColor.AQUA + "Durumu değiştirmek için tıkla!");
                itemMeta.setLore(lore);
                itemMeta.getPersistentDataContainer().set(flagNameKey, PersistentDataType.STRING, flagInfo.flag.getName());
                flagItem.setItemMeta(itemMeta);
            }
            if (flagInfo.slot >= 0 && flagInfo.slot < gui.getSize()) {
                gui.setItem(flagInfo.slot, flagItem);
            } else {
                plugin.getLogger().warning("FlagGUI: Geçersiz slot ("+ flagInfo.slot +") için bayrak eklenemedi: " + flagInfo.displayName);
            }
        }
        player.openInventory(gui);
    }

    // getStatusString, getStatusColor, getFlagNameKey metodları aynı kalacak.
    private String getStatusString(StateFlag.State state) {
        if (state == StateFlag.State.ALLOW) return "İZİNLİ";
        if (state == StateFlag.State.DENY) return "YASAKLI";
        return "VARSAYILAN";
    }

    private ChatColor getStatusColor(StateFlag.State state) {
        if (state == StateFlag.State.ALLOW) return ChatColor.GREEN;
        if (state == StateFlag.State.DENY) return ChatColor.RED;
        return ChatColor.GRAY;
    }

    public NamespacedKey getFlagNameKey() {
        return flagNameKey;
    }
}