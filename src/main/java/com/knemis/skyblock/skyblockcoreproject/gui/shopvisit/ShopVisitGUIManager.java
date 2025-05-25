package com.knemis.skyblock.skyblockcoreproject.gui.shopvisit;

import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ShopVisitGUIManager {

    public static final Component SHOP_VISIT_TITLE = Component.text("Mağazadan Satın Al", NamedTextColor.GOLD);

    /**
     * Müşteri için mağaza alışveriş GUI'si açar.
     * @param player Ziyaret eden oyuncu.
     * @param shop Mağaza nesnesi.
     */
    public void openShopVisitMenu(Player player, Shop shop) {
        Inventory gui = Bukkit.createInventory(null, 27, SHOP_VISIT_TITLE);

        ItemStack saleItem = new ItemStack(shop.getItemType(), shop.getItemQuantityForPrice());
        ItemMeta meta = saleItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Satın Al", NamedTextColor.GREEN));
            meta.lore(Collections.singletonList(Component.text("Fiyat: " + shop.getPrice() + " coins", NamedTextColor.YELLOW)));
            saleItem.setItemMeta(meta);
        }

        gui.setItem(13, saleItem); // Ortadaki slotta satış için ürün

        player.openInventory(gui);
    }
}
