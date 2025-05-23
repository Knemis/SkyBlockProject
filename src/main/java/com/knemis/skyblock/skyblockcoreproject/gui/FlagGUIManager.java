package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import com.sk89q.worldguard.protection.flags.Flag; // Değişiklik
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.inventory.ItemFlag;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
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
import java.util.Collections; // EKLENDİ

import java.util.Arrays;
import java.util.List;

public class FlagGUIManager {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;
    private final IslandFlagManager islandFlagManager;

    public static final String GUI_TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Ada Bayrakları Yönetimi";
    private final NamespacedKey flagNameKey;

    private static class FlagUIData {
        StateFlag flag;
        Material iconMaterial; // Bayrağı temsil eden ana ikon
        String baseDisplayName; // Renksiz temel görünen ad
        List<String> description;
        int slot;

        FlagUIData(StateFlag flag, Material iconMaterial, String baseDisplayName, List<String> description, int slot) {
            this.flag = flag;
            this.iconMaterial = iconMaterial; // Bu materyal artık ana ikon olarak kullanılacak
            this.baseDisplayName = baseDisplayName;
            this.description = description;
            this.slot = slot;
        }
    }

    private final List<FlagUIData> flagLayout = new ArrayList<>();

    public FlagGUIManager(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.islandFlagManager = plugin.getIslandFlagManager();

        if (this.islandFlagManager == null) {
            plugin.getLogger().severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().severe("!!! KRİTİK HATA: IslandFlagManager, FlagGUIManager içinde NULL !!!");
            plugin.getLogger().severe("!!! SkyBlockProject.java içindeki başlatmayı kontrol edin! !!!");
            plugin.getLogger().severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        this.flagNameKey = new NamespacedKey(plugin, "skyblock_flag_name_key_v3"); // Gerekirse anahtar adını güncelleyin
        initializeFlagLayout();
    }

    private void initializeFlagLayout() {
        flagLayout.clear();

        // --- Oyuncu Etkileşim Bayrakları ---
        flagLayout.add(new FlagUIData(Flags.BUILD, Material.DIAMOND_PICKAXE, "İnşa Etme (Build)", Arrays.asList(ChatColor.DARK_AQUA + "Adada blok kırma/koyma izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 10));
        flagLayout.add(new FlagUIData(Flags.INTERACT, Material.LEVER, "Etkileşim (Interact)", Arrays.asList(ChatColor.DARK_AQUA + "Kapı, düğme, şalter vb. kullanım izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 11));
        flagLayout.add(new FlagUIData(Flags.CHEST_ACCESS, Material.CHEST, "Sandık Erişimi", Arrays.asList(ChatColor.DARK_AQUA + "Sandık, fırın, huni vb. envanterlerine", ChatColor.DARK_AQUA + "erişim izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 12));
        flagLayout.add(new FlagUIData(Flags.USE, Material.BUCKET, "Eşya Kullanımı (Use)", Arrays.asList(ChatColor.DARK_AQUA + "Kova, çakmaktaşı, zırh askısı vb.", ChatColor.DARK_AQUA + "kullanım izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 13));
        flagLayout.add(new FlagUIData(Flags.ITEM_DROP, Material.DROPPER, "Eşya Atma (Drop)", Collections.singletonList(ChatColor.DARK_AQUA + "Adaya eşya atılabilme izni."), 14)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.ITEM_PICKUP, Material.HOPPER, "Eşya Toplama", Collections.singletonList(ChatColor.DARK_AQUA + "Adadan eşya toplanabilme izni."), 15)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.TRAMPLE_BLOCKS, Material.FARMLAND, "Ekin Ezme", Arrays.asList(ChatColor.DARK_AQUA + "Ekinlerin üzerinde zıplayarak", ChatColor.DARK_AQUA + "bozulmasını etkiler."), 16));
        flagLayout.add(new FlagUIData(Flags.RIDE, Material.SADDLE, "Binme (Ride)", Arrays.asList(ChatColor.DARK_AQUA + "At, domuz gibi binek hayvanlarına", ChatColor.DARK_AQUA + "binme izni."), 17));

        // --- Hasar ve Savaş Bayrakları ---
        flagLayout.add(new FlagUIData(Flags.PVP, Material.DIAMOND_SWORD, "PVP", Arrays.asList(ChatColor.DARK_AQUA + "Adada oyuncuların birbirine", ChatColor.DARK_AQUA + "hasar verme durumu."), 19));
        flagLayout.add(new FlagUIData(Flags.DAMAGE_ANIMALS, Material.LEAD, "Hayvanlara Hasar", Arrays.asList(ChatColor.DARK_AQUA + "Adadaki hayvanlara hasar verme durumu."), 20));
        flagLayout.add(new FlagUIData(Flags.TNT, Material.TNT, "TNT Patlaması", Collections.singletonList(ChatColor.DARK_AQUA + "TNT'nin bloklara hasar vermesi."), 21)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.CREEPER_EXPLOSION, Material.CREEPER_HEAD, "Creeper Patlaması", Arrays.asList(ChatColor.DARK_AQUA + "Creeper patlamalarının bloklara", ChatColor.DARK_AQUA + "hasar vermesi."), 22));
        flagLayout.add(new FlagUIData(Flags.OTHER_EXPLOSION, Material.FIRE_CHARGE, "Diğer Patlamalar", Arrays.asList(ChatColor.DARK_AQUA + "Ghast ateşi, Wither vb. patlamaların", ChatColor.DARK_AQUA + "bloklara hasar vermesi."), 23));
        flagLayout.add(new FlagUIData(Flags.ENDERPEARL, Material.ENDER_PEARL, "Ender Pearl", Collections.singletonList(ChatColor.DARK_AQUA + "Adada ender pearl atılabilmesi."), 24)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.POTION_SPLASH, Material.SPLASH_POTION, "İksir Sıçraması", Arrays.asList(ChatColor.DARK_AQUA + "Sıçrayan iksirlerin (örn: zarar)", ChatColor.DARK_AQUA + "oyuncuları etkilemesi."), 25));

        // --- Çevresel ve Dünya Bayrakları ---
        flagLayout.add(new FlagUIData(Flags.FIRE_SPREAD, Material.FLINT_AND_STEEL, "Ateş Yayılması", Collections.singletonList(ChatColor.DARK_AQUA + "Ateşin bloklara yayılması."), 28)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.LAVA_FLOW, Material.LAVA_BUCKET, "Lav Akışı", Collections.singletonList(ChatColor.DARK_AQUA + "Lavın adada akması."), 29)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.WATER_FLOW, Material.WATER_BUCKET, "Su Akışı", Collections.singletonList(ChatColor.DARK_AQUA + "Suyun adada akması."), 30)); // Düzeltildi
        flagLayout.add(new FlagUIData(Flags.MOB_SPAWNING, Material.PIG_SPAWN_EGG, "Yaratık Doğması", Arrays.asList(ChatColor.DARK_AQUA + "Adada doğal yaratıkların doğması."), 31));
        flagLayout.add(new FlagUIData(Flags.LEAF_DECAY, Material.OAK_LEAVES, "Yaprak Dökülmesi", Arrays.asList(ChatColor.DARK_AQUA + "Ağaç yapraklarının doğal dökülmesi."), 32));
        flagLayout.add(new FlagUIData(Flags.LIGHTNING, Material.BEACON, "Şimşek Çakması", Arrays.asList(ChatColor.DARK_AQUA + "Adada şimşek çakması ve etkileri."), 33));
        flagLayout.add(new FlagUIData(Flags.SNOW_FALL, Material.SNOW_BLOCK, "Kar Yağışı (Blok)", Arrays.asList(ChatColor.DARK_AQUA + "Kar yağdığında yerde", ChatColor.DARK_AQUA + "kar katmanı oluşması."), 34));
    }

    public void openFlagsGUI(Player player) {
        plugin.getLogger().info("[FlagGUIManager DEBUG] openFlagsGUI çağrıldı."); // EKLENDİ
        plugin.getLogger().info("[FlagGUIManager DEBUG] flagLayout.size(): " + flagLayout.size()); // EKLENDİ

        Island island = islandManager.getIsland(player);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Bayraklarını düzenleyebileceğin bir adan yok!");
            return;
        }

        int numRows = Math.max(3, Math.min(6, ((flagLayout.size() + 8) / 9))); // En az 3, en fazla 6 satır
        int guiSize = numRows * 9;
        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE);

        // Ayırıcılar için genel bir item
        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.setDisplayName(" ");
            separator.setItemMeta(sepMeta);
        }

        // Ayırıcıları yerleştir (isteğe bağlı, daha iyi bir düzen için)
        // Örneğin, her satırın başını ve sonunu doldurabilir veya kategoriler arasına koyabiliriz.
        // Şimdilik basitçe ilk ve son satırı dolduralım (eğer 5 satırdan fazlaysa)
        if (guiSize >= 45) { // 5 satır veya daha fazla ise
            for (int i = 0; i < 9; i++) gui.setItem(i, separator.clone()); // İlk satır
            for (int i = guiSize - 9; i < guiSize; i++) gui.setItem(i, separator.clone()); // Son satır
        }


        for (FlagUIData flagData : flagLayout) {
            if (flagData.flag == null) continue;

            StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), flagData.flag);

            Material itemMaterial; // Duruma göre yün rengi
            ChatColor statusColor;
            String statusName;
            String nextAction;
            ChatColor nextActionColorPrefix;

            if (currentState == StateFlag.State.ALLOW) {
                itemMaterial = Material.LIME_WOOL;
                statusColor = ChatColor.GREEN;
                statusName = "İZİNLİ";
                nextAction = ChatColor.RED + "YASAKLI" + ChatColor.GRAY + " Yap";
            } else if (currentState == StateFlag.State.DENY) {
                itemMaterial = Material.RED_WOOL;
                statusColor = ChatColor.RED;
                statusName = "YASAKLI";
                nextAction = ChatColor.GRAY + "VARSAYILAN" + ChatColor.GRAY + " Yap";
            } else { // null (VARSAYILAN)
                itemMaterial = Material.GRAY_WOOL;
                statusColor = ChatColor.GRAY;
                statusName = "VARSAYILAN";
                nextAction = ChatColor.GREEN + "İZİNLİ" + ChatColor.GRAY + " Yap";
            }

            ItemStack flagItem = new ItemStack(itemMaterial); // Ana ikon olarak bayrağın kendi materyali
            ItemMeta itemMeta = flagItem.getItemMeta();

            if (itemMeta != null) {
                itemMeta.setDisplayName(statusColor + "" + ChatColor.BOLD + flagData.baseDisplayName); // İsim de renklendi

                List<String> lore = new ArrayList<>();
                for (String descLine : flagData.description) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', descLine));
                }
                lore.add(" "); // Boşluk
                lore.add(ChatColor.YELLOW + "➢ Mevcut Durum: " + statusColor + ChatColor.BOLD + statusName);
                if (currentState == null) { // Eğer VARSAYILAN ise, gerçekte ne olduğunu göster
                    StateFlag.State actualDefaultState = islandFlagManager.getDefaultStateFor(flagData.flag);
                    String actualDefaultString = (actualDefaultState == null) ? "Belirsiz (WG Geneli)" : actualDefaultState.name();
                    lore.add(ChatColor.DARK_GRAY + "  (Gerçek Değer: " + actualDefaultString + ")");
                }
                lore.add(ChatColor.AQUA + "➢ Tıkla: " + nextAction);
                lore.add(" ");
                lore.add(ChatColor.DARK_PURPLE + "Bayrak ID: " + ChatColor.LIGHT_PURPLE + flagData.flag.getName());

                itemMeta.setLore(lore);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // İksir etkileri gibi şeyleri gizle
                itemMeta.getPersistentDataContainer().set(flagNameKey, PersistentDataType.STRING, flagData.flag.getName());
                flagItem.setItemMeta(itemMeta);
            }

            if (flagData.slot >= 0 && flagData.slot < gui.getSize()) {
                gui.setItem(flagData.slot, flagItem);
            } else {
                plugin.getLogger().warning("FlagGUI: Geçersiz slot ("+ flagData.slot +") için bayrak eklenemedi: " + flagData.baseDisplayName);
            }
        }
        player.openInventory(gui);
    }

    public NamespacedKey getFlagNameKey() {
        return flagNameKey;
    }
}