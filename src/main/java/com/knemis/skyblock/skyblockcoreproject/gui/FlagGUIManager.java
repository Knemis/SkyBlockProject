package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island; //
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // New import
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldguard.protection.flags.Flags;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections; //
import java.util.List;

public class FlagGUIManager {

    private final SkyBlockProject plugin;
    // IslandManager is replaced by IslandDataHandler for getting island specific data
    private final IslandDataHandler islandDataHandler;
    private final IslandFlagManager islandFlagManager; // Now passed via constructor

    public static final String GUI_TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Ada Bayrakları Yönetimi"; //
    private final NamespacedKey flagNameKey;

    private static class FlagUIData {
        StateFlag flag;
        Material iconMaterial; //
        String baseDisplayName; //
        List<String> description; //
        int slot; //

        FlagUIData(StateFlag flag, Material iconMaterial, String baseDisplayName, List<String> description, int slot) { //
            this.flag = flag; //
            this.iconMaterial = iconMaterial; //
            this.baseDisplayName = baseDisplayName; //
            this.description = description; //
            this.slot = slot; //
        }
    }

    private final List<FlagUIData> flagLayout = new ArrayList<>(); //

    // Constructor updated to take IslandDataHandler and IslandFlagManager
    public FlagGUIManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandFlagManager islandFlagManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler; // Store IslandDataHandler
        this.islandFlagManager = islandFlagManager; // Store IslandFlagManager directly

        if (this.islandFlagManager == null) { //
            plugin.getLogger().severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //
            plugin.getLogger().severe("!!! KRİTİK HATA: IslandFlagManager, FlagGUIManager içinde NULL !!!"); //
            plugin.getLogger().severe("!!! SkyBlockProject.java içindeki başlatmayı kontrol edin! !!!"); //
            plugin.getLogger().severe("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //
        }
        this.flagNameKey = new NamespacedKey(plugin, "skyblock_flag_name_key_v3"); //
        initializeFlagLayout(); //
    }

    private void initializeFlagLayout() {
        flagLayout.clear(); //
        // --- Player Interaction Flags ---
        flagLayout.add(new FlagUIData(Flags.BUILD, Material.DIAMOND_PICKAXE, "İnşa Etme (Build)", Arrays.asList(ChatColor.DARK_AQUA + "Adada blok kırma/koyma izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 10)); //
        flagLayout.add(new FlagUIData(Flags.INTERACT, Material.LEVER, "Etkileşim (Interact)", Arrays.asList(ChatColor.DARK_AQUA + "Kapı, düğme, şalter vb. kullanım izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 11)); //
        flagLayout.add(new FlagUIData(Flags.CHEST_ACCESS, Material.CHEST, "Sandık Erişimi", Arrays.asList(ChatColor.DARK_AQUA + "Sandık, fırın, huni vb. envanterlerine", ChatColor.DARK_AQUA + "erişim izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 12)); //
        flagLayout.add(new FlagUIData(Flags.USE, Material.BUCKET, "Eşya Kullanımı (Use)", Arrays.asList(ChatColor.DARK_AQUA + "Kova, çakmaktaşı, zırh askısı vb.", ChatColor.DARK_AQUA + "kullanım izni.", ChatColor.DARK_GRAY + "(Genellikle ziyaretçiler için)"), 13)); //
        flagLayout.add(new FlagUIData(Flags.ITEM_DROP, Material.DROPPER, "Eşya Atma (Drop)", Collections.singletonList(ChatColor.DARK_AQUA + "Adaya eşya atılabilme izni."), 14)); //
        flagLayout.add(new FlagUIData(Flags.ITEM_PICKUP, Material.HOPPER, "Eşya Toplama", Collections.singletonList(ChatColor.DARK_AQUA + "Adadan eşya toplanabilme izni."), 15)); //
        flagLayout.add(new FlagUIData(Flags.TRAMPLE_BLOCKS, Material.FARMLAND, "Ekin Ezme", Arrays.asList(ChatColor.DARK_AQUA + "Ekinlerin üzerinde zıplayarak", ChatColor.DARK_AQUA + "bozulmasını etkiler."), 16)); //
        flagLayout.add(new FlagUIData(Flags.RIDE, Material.SADDLE, "Binme (Ride)", Arrays.asList(ChatColor.DARK_AQUA + "At, domuz gibi binek hayvanlarına", ChatColor.DARK_AQUA + "binme izni."), 17)); //
        // --- Damage and Combat Flags ---
        flagLayout.add(new FlagUIData(Flags.PVP, Material.DIAMOND_SWORD, "PVP", Arrays.asList(ChatColor.DARK_AQUA + "Adada oyuncuların birbirine", ChatColor.DARK_AQUA + "hasar verme durumu."), 19)); //
        flagLayout.add(new FlagUIData(Flags.DAMAGE_ANIMALS, Material.LEAD, "Hayvanlara Hasar", Arrays.asList(ChatColor.DARK_AQUA + "Adadaki hayvanlara hasar verme durumu."), 20)); //
        flagLayout.add(new FlagUIData(Flags.TNT, Material.TNT, "TNT Patlaması", Collections.singletonList(ChatColor.DARK_AQUA + "TNT'nin bloklara hasar vermesi."), 21)); //
        flagLayout.add(new FlagUIData(Flags.CREEPER_EXPLOSION, Material.CREEPER_HEAD, "Creeper Patlaması", Arrays.asList(ChatColor.DARK_AQUA + "Creeper patlamalarının bloklara", ChatColor.DARK_AQUA + "hasar vermesi."), 22)); //
        flagLayout.add(new FlagUIData(Flags.OTHER_EXPLOSION, Material.FIRE_CHARGE, "Diğer Patlamalar", Arrays.asList(ChatColor.DARK_AQUA + "Ghast ateşi, Wither vb. patlamaların", ChatColor.DARK_AQUA + "bloklara hasar vermesi."), 23)); //
        flagLayout.add(new FlagUIData(Flags.ENDERPEARL, Material.ENDER_PEARL, "Ender Pearl", Collections.singletonList(ChatColor.DARK_AQUA + "Adada ender pearl atılabilmesi."), 24)); //
        flagLayout.add(new FlagUIData(Flags.POTION_SPLASH, Material.SPLASH_POTION, "İksir Sıçraması", Arrays.asList(ChatColor.DARK_AQUA + "Sıçrayan iksirlerin (örn: zarar)", ChatColor.DARK_AQUA + "oyuncuları etkilemesi."), 25)); //
        // --- Environmental and World Flags ---
        flagLayout.add(new FlagUIData(Flags.FIRE_SPREAD, Material.FLINT_AND_STEEL, "Ateş Yayılması", Collections.singletonList(ChatColor.DARK_AQUA + "Ateşin bloklara yayılması."), 28)); //
        flagLayout.add(new FlagUIData(Flags.LAVA_FLOW, Material.LAVA_BUCKET, "Lav Akışı", Collections.singletonList(ChatColor.DARK_AQUA + "Lavın adada akması."), 29)); //
        flagLayout.add(new FlagUIData(Flags.WATER_FLOW, Material.WATER_BUCKET, "Su Akışı", Collections.singletonList(ChatColor.DARK_AQUA + "Suyun adada akması."), 30)); //
        flagLayout.add(new FlagUIData(Flags.MOB_SPAWNING, Material.PIG_SPAWN_EGG, "Yaratık Doğması", Arrays.asList(ChatColor.DARK_AQUA + "Adada doğal yaratıkların doğması."), 31)); //
        flagLayout.add(new FlagUIData(Flags.LEAF_DECAY, Material.OAK_LEAVES, "Yaprak Dökülmesi", Arrays.asList(ChatColor.DARK_AQUA + "Ağaç yapraklarının doğal dökülmesi."), 32)); //
        flagLayout.add(new FlagUIData(Flags.LIGHTNING, Material.BEACON, "Şimşek Çakması", Arrays.asList(ChatColor.DARK_AQUA + "Adada şimşek çakması ve etkileri."), 33)); //
        flagLayout.add(new FlagUIData(Flags.SNOW_FALL, Material.SNOW_BLOCK, "Kar Yağışı (Blok)", Arrays.asList(ChatColor.DARK_AQUA + "Kar yağdığında yerde", ChatColor.DARK_AQUA + "kar katmanı oluşması."), 34)); //
    }

    public void openFlagsGUI(Player player) {
        plugin.getLogger().info("[FlagGUIManager DEBUG] openFlagsGUI çağrıldı. Oyuncu: " + player.getName()); //
        plugin.getLogger().info("[FlagGUIManager DEBUG] flagLayout.size(): " + flagLayout.size()); //

        // Use IslandDataHandler to get the island
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());

        if (island == null) { //
            player.sendMessage(ChatColor.RED + "Bayraklarını düzenleyebileceğin bir adan yok!"); //
            return; //
        }

        int guiSize = 54; // Fixed size for 6 rows //
        plugin.getLogger().info("[FlagGUIManager DEBUG] Kullanılan sabit GUI Boyutu: " + guiSize); //
        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE); //

        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE); //
        ItemMeta sepMeta = separator.getItemMeta(); //
        if (sepMeta != null) { //
            sepMeta.setDisplayName(" "); //
            separator.setItemMeta(sepMeta); //
        }

        // Place separators
        for (int i = 0; i < 9; i++) gui.setItem(i, separator.clone()); // First row //
        // Middle separators for a 54 slot GUI (visual grouping)
        gui.setItem(18, separator.clone()); // Start of 3rd content row (index after 2 data rows)
        gui.setItem(26, separator.clone()); // End of 3rd content row
        gui.setItem(36, separator.clone()); // Start of 5th content row
        gui.setItem(44, separator.clone()); // End of 5th content row
        for (int i = guiSize - 9; i < guiSize; i++) gui.setItem(i, separator.clone()); // Last row //


        for (FlagUIData flagData : flagLayout) { //
            if (flagData.flag == null) { //
                plugin.getLogger().warning("[FlagGUIManager] flagData.flag null, atlanıyor. Slot: " + flagData.slot); //
                continue; //
            }
            // Use the IslandFlagManager instance directly
            StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), flagData.flag); //

            Material itemMaterial; //
            ChatColor statusColor; //
            String statusName; //
            String nextAction; //
            if (currentState == StateFlag.State.ALLOW) { //
                itemMaterial = Material.LIME_WOOL; //
                statusColor = ChatColor.GREEN; //
                statusName = "İZİNLİ"; //
                nextAction = ChatColor.RED + "YASAKLI" + ChatColor.GRAY + " Yap"; //
            } else if (currentState == StateFlag.State.DENY) { //
                itemMaterial = Material.RED_WOOL; //
                statusColor = ChatColor.RED; //
                statusName = "YASAKLI"; //
                nextAction = ChatColor.GRAY + "VARSAYILAN" + ChatColor.GRAY + " Yap"; //
            } else { // currentState == null (DEFAULT)
                itemMaterial = Material.GRAY_WOOL; //
                statusColor = ChatColor.GRAY; //
                statusName = "VARSAYILAN"; //
                nextAction = ChatColor.GREEN + "İZİNLİ" + ChatColor.GRAY + " Yap"; //
            }

            ItemStack flagItem = new ItemStack(itemMaterial); //
            ItemMeta itemMeta = flagItem.getItemMeta(); //

            if (itemMeta != null) { //
                itemMeta.setDisplayName(statusColor + "" + ChatColor.BOLD + flagData.baseDisplayName); //
                List<String> lore = new ArrayList<>(); //
                for (String descLine : flagData.description) { //
                    lore.add(ChatColor.translateAlternateColorCodes('&', descLine)); //
                }
                lore.add(" "); //
                lore.add(ChatColor.YELLOW + "➢ Mevcut Durum: " + statusColor + ChatColor.BOLD + statusName); //
                if (currentState == null) { // If current state is DEFAULT, show actual inherited/default value
                    StateFlag.State actualDefaultState = islandFlagManager.getDefaultStateFor(flagData.flag); //
                    String actualDefaultString = (actualDefaultState == null) ? "Belirsiz (WG Geneli)" : actualDefaultState.name(); //
                    lore.add(ChatColor.DARK_GRAY + "  (Gerçek Değer: " + actualDefaultString + ")"); //
                }
                lore.add(ChatColor.AQUA + "➢ Tıkla: " + nextAction); //
                lore.add(" "); //
                lore.add(ChatColor.DARK_PURPLE + "Bayrak ID: " + ChatColor.LIGHT_PURPLE + flagData.flag.getName()); //

                itemMeta.setLore(lore); //
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); //
                itemMeta.getPersistentDataContainer().set(flagNameKey, PersistentDataType.STRING, flagData.flag.getName()); //
                flagItem.setItemMeta(itemMeta); //
            }

            if (flagData.slot >= 0 && flagData.slot < gui.getSize()) { //
                gui.setItem(flagData.slot, flagItem); //
            } else {
                if(plugin.getConfig().getBoolean("logging.detailed-flag-changes", false)) { //
                    plugin.getLogger().warning("[FlagGUIManager] Geçersiz slot ("+ flagData.slot +") için bayrak '" + flagData.baseDisplayName + "' eklenemedi. GUI Boyutu: " + gui.getSize()); //
                }
            }
        }
        player.openInventory(gui); //
    }

    public NamespacedKey getFlagNameKey() {
        return flagNameKey; //
    }
}