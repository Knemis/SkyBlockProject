package com.knemis.skyblock.skyblockcoreproject.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopListener implements Listener {

    private final ShopManager shopManager;
    private final JavaPlugin plugin;

    public ShopListener(JavaPlugin plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Block clicked = event.getClickedBlock();
        if (clicked.getType() != Material.CHEST) return;

        Chest chest = (Chest) clicked.getState();
        Player player = event.getPlayer();

        // Shift + Sağ Tık → Mağaza oluştur
        if (player.isSneaking()) {
            if (!shopManager.isShop(chest.getLocation())) {
                // Başlangıç için sabit fiyat belirleyelim (örneğin 10.0)
                shopManager.createShop(chest.getLocation(), player.getUniqueId().toString(), 10.0);
                player.sendMessage(ChatColor.GREEN + "Bu sandık artık bir mağaza!");
            } else {
                player.sendMessage(ChatColor.RED + "Bu sandık zaten bir mağaza.");
            }
            event.setCancelled(true);
            return;
        }

        // Eğer bir mağazaysa ve sahibi bu oyuncu DEĞİLSE → Satın alma
        if (shopManager.isShop(chest.getLocation())) {
            String owner = shopManager.getOwner(chest.getLocation());
            if (!owner.equals(player.getUniqueId().toString())) {
                double price = shopManager.getPrice(chest.getLocation());
                Inventory inv = chest.getBlockInventory();

                ItemStack item = inv.getItem(0); // Sadece 0. slot satılık

                if (item == null || item.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Bu mağazada satılık bir şey yok.");
                    return;
                }

                if (EconomyManager.getBalance(player) < price) {
                    player.sendMessage(ChatColor.RED + "Yeterli paran yok. Fiyat: " + price);
                    return;
                }

                // Para işlemleri
                if (EconomyManager.withdraw(player, price)) {
                    EconomyManager.deposit(Bukkit.getOfflinePlayer(owner), price);
                    inv.removeItem(item.clone());
                    player.getInventory().addItem(item.clone());
                    player.sendMessage(ChatColor.YELLOW + "Satın alındı: " + item.getType() + " - " + price + " coin");
                } else {
                    player.sendMessage(ChatColor.RED + "Ödeme başarısız.");
                }

                event.setCancelled(true);
            }
        }
    }
}
