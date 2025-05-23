package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class FlagGUIListener implements Listener {

    private final SkyBlockProject plugin;
    private final FlagGUIManager flagGUIManager;
    private final IslandFlagManager islandFlagManager; // YENİ: Bayrak işlemleri için
    private final NamespacedKey flagNameKey;
    private final FlagRegistry flagRegistry;

    public FlagGUIListener(SkyBlockProject plugin, FlagGUIManager flagGUIManager /*, IslandManager islandManager - kaldırıldı */) {
        this.plugin = plugin;
        this.flagGUIManager = flagGUIManager;
        this.islandFlagManager = plugin.getIslandFlagManager(); // SkyBlockProject'ten al
        this.flagNameKey = flagGUIManager.getFlagNameKey(); // FlagGUIManager'dan anahtarı al
        this.flagRegistry = WorldGuard.getInstance().getFlagRegistry();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(FlagGUIManager.GUI_TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null || !itemMeta.getPersistentDataContainer().has(flagNameKey, PersistentDataType.STRING)) return;

        String flagName = itemMeta.getPersistentDataContainer().get(flagNameKey, PersistentDataType.STRING);
        if (flagName == null) {
            plugin.getLogger().warning("FlagGUIListener: Tıklanan öğeden flagName null geldi. Oyuncu: " + player.getName());
            return;
        }

        Flag<?> genericFlag = flagRegistry.get(flagName);
        if (genericFlag == null) {
            player.sendMessage(ChatColor.RED + "Bilinmeyen bayrak adı: " + flagName);
            plugin.getLogger().warning("FlagGUIListener: Bilinmeyen bayrak adı: " + flagName + ". Oyuncu: " + player.getName());
            return;
        }

        if (!(genericFlag instanceof StateFlag)) {
            player.sendMessage(ChatColor.RED + "'" + flagName + "' bayrağı AÇIK/KAPALI/VARSAYILAN yapılamaz (StateFlag değil).");
            return;
        }
        StateFlag stateFlag = (StateFlag) genericFlag;

        // Mevcut durumu IslandFlagManager üzerinden al
        StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), stateFlag);
        StateFlag.State newState;

        // Döngüsel Değişim: VARSAYILAN (null) -> İZİNLİ (ALLOW) -> YASAKLI (DENY) -> VARSAYILAN (null) ...
        if (currentState == null) {
            newState = StateFlag.State.ALLOW;
        } else if (currentState == StateFlag.State.ALLOW) {
            newState = StateFlag.State.DENY;
        } else { // currentState == StateFlag.State.DENY
            newState = null; // Varsayılana döndür
        }

        // Yeni durumu IslandFlagManager üzerinden ayarla
        // setIslandFlagState metodu artık 'changer' (değişikliği yapan oyuncu) parametresini de alıyor.
        boolean success = islandFlagManager.setIslandFlagState(player, player.getUniqueId(), stateFlag, newState);

        if (success) {
            String newStateString;
            ChatColor statusColor;

            if (newState == StateFlag.State.ALLOW) {
                newStateString = "İZİNLİ";
                statusColor = ChatColor.GREEN;
            } else if (newState == StateFlag.State.DENY) {
                newStateString = "YASAKLI";
                statusColor = ChatColor.RED;
            } else {
                newStateString = "VARSAYILAN";
                statusColor = ChatColor.GRAY;
            }
            player.sendMessage(ChatColor.GOLD + "'" + flagName + "' bayrağı " + statusColor + ChatColor.BOLD + newStateString + ChatColor.GOLD + " olarak ayarlandı.");
            flagGUIManager.openFlagsGUI(player); // GUI'yi güncel durumla yeniden aç
        } else {
            // islandFlagManager.setIslandFlagState zaten oyuncuya bir hata mesajı göndermiş olmalı.
            // Ek olarak log da tutmuş olmalı.
            // player.sendMessage(ChatColor.RED + "'" + flagName + "' bayrağı ayarlanırken bir sorun oluştu."); // Bu satır artık IslandFlagManager'da olabilir.
            player.closeInventory(); // Hata durumunda envanteri kapatabiliriz.
        }
    }
}
