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

        Player player = (Player) event.getWhoClicked();
        Component viewTitle = event.getView().title();

        // Tek genel GUI başlığı ile kontrol
        if (!viewTitle.equals(FlagGUIManager.GUI_TITLE_COMPONENT)) {
            return;
        }
        plugin.getLogger().info(String.format("Player %s clicked in Flag GUI: %s, Slot: %d, Item: %s",
                player.getName(), viewTitle, event.getSlot(), event.getCurrentItem() != null ? event.getCurrentItem().getType().name() : "null"));

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            plugin.getLogger().warning(String.format("FlagGUIListener: Clicked item was null or had no meta for player %s in GUI %s, slot %d",
                    player.getName(), viewTitle, event.getSlot()));
            return;
        }
        ItemMeta itemMeta = clickedItem.getItemMeta();

        if (!itemMeta.getPersistentDataContainer().has(flagNameKey, PersistentDataType.STRING)) {
            // Bu durum, GUI'deki boşluklara veya dekoratif öğelere tıklanırsa oluşabilir, bu bir uyarı olmayabilir.
            // Ancak, bir bayrak öğesi olması bekleniyorsa ve anahtar yoksa, bu bir sorundur.
            // Şimdilik, tıklanan öğenin bir bayrak öğesi olup olmadığını belirlemek için daha fazla bağlam olmadan bunu bir uyarı olarak bırakıyoruz.
            plugin.getLogger().warning(String.format("FlagGUIListener: Clicked item for player %s in GUI %s (Item: %s) is missing flagNameKey.",
                    player.getName(), viewTitle, clickedItem.getType().name()));
            return;
        }

        String flagName = itemMeta.getPersistentDataContainer().get(flagNameKey, PersistentDataType.STRING);

        if (flagName == null) {
            plugin.getLogger().warning(String.format("FlagGUIListener: Invalid (null) flag name extracted for player %s. Item: %s",
                    player.getName(), (itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : clickedItem.getType().name()) ));
            return;
        }

        if (!plugin.getIslandDataHandler().playerHasIsland(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bu işlemi yapabilmek için bir adanız olmalı!");
            plugin.getLogger().warning(String.format("FlagGUIListener: Player %s has no island, cannot modify flag %s.", player.getName(), flagName));
            player.closeInventory();
            return;
        }

        Flag<?> genericFlag = flagRegistry.get(flagName);
        if (genericFlag == null) {
            player.sendMessage(ChatColor.RED + "Bilinmeyen bayrak adı: " + flagName);
            plugin.getLogger().warning(String.format("FlagGUIListener: Unknown flag name '%s' clicked by player %s.", flagName, player.getName()));
            return;
        }

        if (!(genericFlag instanceof StateFlag)) {
            player.sendMessage(ChatColor.RED + "'" + flagName + "' bayrağı AÇIK/KAPALI/VARSAYILAN yapılamaz (StateFlag değil).");
            plugin.getLogger().warning(String.format("FlagGUIListener: Flag '%s' is not a StateFlag, clicked by player %s.", flagName, player.getName()));
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
            newState = null; // Varsayılana (parent'tan miras) döner
        }

        boolean success = islandFlagManager.setIslandFlagState(player, player.getUniqueId(), stateFlag, newState);

        if (success) {
            String newStateString;
            ChatColor statusColor;

            if (newState == StateFlag.State.ALLOW) {
                newStateString = "İZİNLİ"; statusColor = ChatColor.GREEN;
            } else if (newState == StateFlag.State.DENY) {
                newStateString = "YASAKLI"; statusColor = ChatColor.RED;
            } else { // newState == null (VARSAYILAN)
                newStateString = "VARSAYILAN"; statusColor = ChatColor.GRAY;
            }
            player.sendMessage(ChatColor.GOLD + "'" + flagName + "' bayrağı (genel) " + statusColor + ChatColor.BOLD + newStateString + ChatColor.GOLD + " olarak ayarlandı.");
            plugin.getLogger().info(String.format("Player %s successfully toggled flag %s to %s for their island.", player.getName(), flagName, (newState == null ? "DEFAULT" : newState.name())));
            flagGUIManager.openFlagsGUI(player); // Refresh GUI
        } else {
            // islandFlagManager.setIslandFlagState already sends a message and logs on failure
            player.closeInventory(); // Close inventory if setting failed for some reason not caught by manager
            plugin.getLogger().warning(String.format("FlagGUIListener: islandFlagManager.setIslandFlagState returned false for player %s, flag %s, new state %s, but no specific error message was sent from there.",
                    player.getName(), flagName, (newState == null ? "DEFAULT" : newState.name())));
        }
    }
}