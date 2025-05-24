package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
// import com.sk89q.worldguard.protection.flags.RegionGroup; // KALDIRILDI
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
import net.kyori.adventure.text.Component;

public class FlagGUIListener implements Listener {

    private final SkyBlockProject plugin;
    private final FlagGUIManager flagGUIManager;
    private final IslandFlagManager islandFlagManager;
    private final NamespacedKey flagNameKey;
    // private final NamespacedKey flagGroupKey; // KALDIRILDI
    private final FlagRegistry flagRegistry;

    public FlagGUIListener(SkyBlockProject plugin, FlagGUIManager flagGUIManager) {
        this.plugin = plugin;
        this.flagGUIManager = flagGUIManager;
        this.islandFlagManager = plugin.getIslandFlagManager();
        this.flagNameKey = flagGUIManager.getFlagNameKey();
        // this.flagGroupKey = flagGUIManager.getFlagGroupKey(); // KALDIRILDI
        this.flagRegistry = WorldGuard.getInstance().getFlagRegistry();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Component viewTitle = event.getView().title();

        // Tek genel GUI başlığı ile kontrol
        if (!viewTitle.equals(FlagGUIManager.GUI_TITLE_COMPONENT)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        ItemMeta itemMeta = clickedItem.getItemMeta();
        // Sadece flagNameKey kontrolü yeterli
        if (itemMeta == null || !itemMeta.getPersistentDataContainer().has(flagNameKey, PersistentDataType.STRING)) {
            plugin.getLogger().warning("FlagGUIListener: Tıklanan öğede flagNameKey eksik. Oyuncu: " + player.getName());
            return;
        }

        String flagName = itemMeta.getPersistentDataContainer().get(flagNameKey, PersistentDataType.STRING);
        // String groupName = itemMeta.getPersistentDataContainer().get(flagGroupKey, PersistentDataType.STRING); // KALDIRILDI

        if (flagName == null) { // groupName kontrolü kaldırıldı
            plugin.getLogger().warning("FlagGUIListener: flagName null geldi. Oyuncu: " + player.getName());
            return;
        }

        // RegionGroup regionGroupContext; // KALDIRILDI
        // try {
        // regionGroupContext = RegionGroup.valueOf(groupName.toUpperCase());
        // } catch (IllegalArgumentException e) { /* ... */ return; }

        Flag<?> genericFlag = flagRegistry.get(flagName);
        if (genericFlag == null) {
            player.sendMessage(ChatColor.RED + "Bilinmeyen bayrak adı: " + flagName);
            return;
        }

        if (!(genericFlag instanceof StateFlag)) {
            player.sendMessage(ChatColor.RED + "'" + flagName + "' bayrağı AÇIK/KAPALI/VARSAYILAN yapılamaz (StateFlag değil).");
            return;
        }
        StateFlag stateFlag = (StateFlag) genericFlag;

        // Bayrak durumu grup olmadan, genel olarak alınır
        StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), stateFlag);
        StateFlag.State newState;

        if (currentState == null) {
            newState = StateFlag.State.ALLOW;
        } else if (currentState == StateFlag.State.ALLOW) {
            newState = StateFlag.State.DENY;
        } else { // currentState == StateFlag.State.DENY
            newState = null;
        }

        // Bayrak durumu grup olmadan, genel olarak ayarlanır
        boolean success = islandFlagManager.setIslandFlagState(player, player.getUniqueId(), stateFlag, newState);

        if (success) {
            String newStateString;
            ChatColor statusColor;

            if (newState == StateFlag.State.ALLOW) {
                newStateString = "İZİNLİ"; statusColor = ChatColor.GREEN;
            } else if (newState == StateFlag.State.DENY) {
                newStateString = "YASAKLI"; statusColor = ChatColor.RED;
            } else {
                newStateString = "VARSAYILAN"; statusColor = ChatColor.GRAY;
            }
            // Mesajdan grup bilgisi çıkarıldı
            player.sendMessage(ChatColor.GOLD + "'" + flagName + "' bayrağı (genel) " + statusColor + ChatColor.BOLD + newStateString + ChatColor.GOLD + " olarak ayarlandı.");
            // GUI grup parametresi olmadan yeniden açılır
            flagGUIManager.openFlagsGUI(player);
        } else {
            player.closeInventory();
        }
    }
}