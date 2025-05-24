package com.knemis.skyblock.skyblockcoreproject.listeners;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandFlagManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.ChatColor; // Oyuncuya mesaj gönderirken hala kullanılabilir
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

// Adventure API importu gerekebilir eğer Component ile doğrudan işlem yapacaksak,
// ama burada sadece equals için kullanıyoruz, FlagGUIManager'dan gelen Component ile.
// import net.kyori.adventure.text.Component;


public class FlagGUIListener implements Listener {

    private final SkyBlockProject plugin;
    private final FlagGUIManager flagGUIManager;
    private final IslandFlagManager islandFlagManager;
    private final NamespacedKey flagNameKey;
    private final FlagRegistry flagRegistry;

    public FlagGUIListener(SkyBlockProject plugin, FlagGUIManager flagGUIManager) {
        this.plugin = plugin;
        this.flagGUIManager = flagGUIManager;
        this.islandFlagManager = plugin.getIslandFlagManager();
        this.flagNameKey = flagGUIManager.getFlagNameKey();
        this.flagRegistry = WorldGuard.getInstance().getFlagRegistry();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // DÜZELTME: getTitle() yerine title() kullanıldı ve GUI_TITLE_COMPONENT ile karşılaştırıldı.
        if (!event.getView().title().equals(FlagGUIManager.GUI_TITLE_COMPONENT)) return;

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

        StateFlag.State currentState = islandFlagManager.getIslandFlagState(player.getUniqueId(), stateFlag);
        StateFlag.State newState;

        if (currentState == null) {
            newState = StateFlag.State.ALLOW;
        } else if (currentState == StateFlag.State.ALLOW) {
            newState = StateFlag.State.DENY;
        } else { // currentState == StateFlag.State.DENY
            newState = null;
        }

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
            // Oyuncuya gönderilen mesajlar şimdilik ChatColor ile kalabilir veya Component'e çevrilebilir.
            player.sendMessage(ChatColor.GOLD + "'" + flagName + "' bayrağı " + statusColor + ChatColor.BOLD + newStateString + ChatColor.GOLD + " olarak ayarlandı.");
            flagGUIManager.openFlagsGUI(player);
        } else {
            player.closeInventory();
        }
    }
}