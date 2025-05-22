package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
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
    private final IslandManager islandManager;
    private final NamespacedKey flagNameKey;
    private final FlagRegistry flagRegistry;

    public FlagGUIListener(SkyBlockProject plugin, FlagGUIManager flagGUIManager, IslandManager islandManager) {
        this.plugin = plugin;
        this.flagGUIManager = flagGUIManager;
        this.islandManager = islandManager;
        this.flagNameKey = flagGUIManager.getFlagNameKey();
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
        if (flagName == null) return;

        Flag<?> genericFlag = flagRegistry.get(flagName);
        if (genericFlag == null) {
            player.sendMessage(ChatColor.RED + "Bilinmeyen bayrak adı: " + flagName);
            return;
        }

        if (!(genericFlag instanceof StateFlag)) {
            player.sendMessage(ChatColor.RED + "'" + flagName + "' bayrağı AÇIK/KAPALI yapılamaz.");
            return;
        }
        StateFlag stateFlag = (StateFlag) genericFlag;

        StateFlag.State currentState = islandManager.getIslandFlagState(player.getUniqueId(), stateFlag);
        StateFlag.State newState;

        // Döngüsel Değişim: VARSAYILAN -> İZİNLİ -> YASAKLI -> VARSAYILAN ...
        if (currentState == null) { // Şu an VARSAYILAN (null) ise İZİNLİ yap
            newState = StateFlag.State.ALLOW;
        } else if (currentState == StateFlag.State.ALLOW) { // Şu an İZİNLİ ise YASAKLI yap
            newState = StateFlag.State.DENY;
        } else { // Şu an YASAKLI ise VARSAYILAN yap (bayrağı temizle, yani null ata)
            newState = null;
        }
        // Basit AÇ/KAPA için:
        // if (currentState == StateFlag.State.ALLOW) newState = StateFlag.State.DENY;
        // else newState = StateFlag.State.ALLOW;


        boolean success = islandManager.setIslandFlagState(player.getUniqueId(), stateFlag, newState);

        if (success) {
            String newStateString;
            if (newState == StateFlag.State.ALLOW) newStateString = ChatColor.GREEN + "İZİNLİ";
            else if (newState == StateFlag.State.DENY) newStateString = ChatColor.RED + "YASAKLI";
            else newStateString = ChatColor.GRAY + "VARSAYILAN";

            player.sendMessage(ChatColor.YELLOW + "'" + flagName + "' bayrağı " + newStateString + ChatColor.YELLOW + " olarak ayarlandı.");
            flagGUIManager.openFlagsGUI(player); // GUI'yi güncel durumla yeniden aç
        } else {
            player.sendMessage(ChatColor.RED + "'" + flagName + "' bayrağı ayarlanırken bir sorun oluştu.");
            player.closeInventory();
        }
    }
}
