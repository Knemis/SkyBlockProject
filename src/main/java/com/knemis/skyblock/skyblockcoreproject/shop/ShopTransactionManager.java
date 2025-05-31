package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopTransactionManager {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;

    public ShopTransactionManager(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    public boolean executePurchase(Player buyer, Shop shop, int bundlesToBuy) {
        if (shop == null || !shop.isSetupComplete() || buyer == null || bundlesToBuy <= 0 || shop.getTemplateItemStack() == null) {
            this.plugin.getLogger().warning("[ShopTransactionManager-Purchase] Invalid parameters or shop state for purchase attempt by " + (buyer != null ? buyer.getName() : "UnknownPlayer"));
            if (buyer != null) buyer.sendMessage(Component.text("Satın alma işlemi sırasında bir hata oluştu.", NamedTextColor.RED));
            return false;
        }

        int itemsPerBundle = shop.getBundleAmount();
        int totalItemsToBuy = bundlesToBuy * itemsPerBundle;

        if (shop.getBuyPrice() < 0) {
            buyer.sendMessage(Component.text("Bu dükkan şu anda ürün satmıyor.", NamedTextColor.RED));
            return false;
        }
        double totalCost = bundlesToBuy * shop.getBuyPrice();
        ItemStack templateItem = shop.getTemplateItemStack();

        if (totalItemsToBuy <= 0 || totalCost < 0) {
            this.plugin.getLogger().warning("[ShopTransactionManager-Purchase] Geçersiz hesaplanmış miktar veya fiyat. Dükkan: " + Shop.locationToString(shop.getLocation()));
            buyer.sendMessage(Component.text("Dükkan ayarlarında bir sorun var.", NamedTextColor.RED));
            return false;
        }

        String formattedItemNameLegacy = this.shopManager.getShopSignManager().getItemNameForMessages(templateItem, 30);
        Component formattedItemName = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedItemNameLegacy);
        String currencySymbolLegacy = this.shopManager.getCurrencySymbol();
        // Component currencySymbol = LegacyComponentSerializer.legacyAmpersand().deserialize(currencySymbolLegacy); // Not needed if only used in String.format

        if (!EconomyManager.isEconomyAvailable()) {
            buyer.sendMessage(Component.text("Ekonomi sistemi mevcut değil.", NamedTextColor.RED));
            return false;
        }
        if (EconomyManager.getBalance(buyer) < totalCost) {
            buyer.sendMessage(Component.text("Yetersiz bakiye! Gereken: " + String.format("%.2f%s", totalCost, currencySymbolLegacy), NamedTextColor.RED));
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(Component.text("Dükkan sandığı bulunamadı!", NamedTextColor.RED));
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (ShopInventoryManager.countItemsInChest(chest, templateItem) < totalItemsToBuy) {
            buyer.sendMessage(Component.text("Dükkanda yeterli stok yok (" + totalItemsToBuy + " ", NamedTextColor.RED).append(formattedItemName).append(Component.text(" gerekli).", NamedTextColor.RED)));
            this.shopManager.getShopSignManager().updateAttachedSign(shop, currencySymbolLegacy);
            return false;
        }

        ItemStack itemsToReceive = templateItem.clone();
        itemsToReceive.setAmount(totalItemsToBuy);
        if (!ShopInventoryManager.hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(Component.text("Envanterinizde " + totalItemsToBuy + " ", NamedTextColor.RED).append(formattedItemName).append(Component.text(" için yeterli yer yok!", NamedTextColor.RED)));
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        if (!EconomyManager.withdraw(buyer, totalCost)) {
            buyer.sendMessage(Component.text("Ödeme çekme işlemi başarısız oldu.", NamedTextColor.RED));
            return false;
        }

        if (!EconomyManager.deposit(owner, totalCost)) {
            EconomyManager.deposit(buyer, totalCost); // Refund buyer
            buyer.sendMessage(Component.text("KRİTİK HATA: ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("Satıcıya para transfer edilemedi. Paranız iade edildi!", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
            this.plugin.getLogger().severe("[ShopTransactionManager-Purchase] KRİTİK: Para sahibi " + (owner.getName() != null ? owner.getName() : owner.getUniqueId()) + " hesabına yatırılamadı. Alıcı " + buyer.getName() + " iade edildi.");
            return false;
        }

        if (!ShopInventoryManager.removeItemsFromChest(chest, templateItem, totalItemsToBuy)) {
            EconomyManager.withdraw(owner, totalCost);
            EconomyManager.deposit(buyer, totalCost);
            buyer.sendMessage(Component.text("KRİTİK HATA: ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("Dükkandan ürünler alınamadı. Paranız iade edildi!", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
            this.plugin.getLogger().severe("[ShopTransactionManager-Purchase] KRİTİK: Sandıktan ürünler çekilemedi. Para transferleri geri alındı. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        buyer.getInventory().addItem(itemsToReceive.clone());
        shop.recordTransaction(totalItemsToBuy, totalCost);
        this.shopManager.saveShop(shop);

        buyer.sendMessage(Component.text("Başarıyla ", NamedTextColor.GREEN)
                .append(Component.text(totalItemsToBuy + " ", NamedTextColor.AQUA))
                .append(formattedItemName.colorIfAbsent(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" satın aldınız, fiyat: ", NamedTextColor.GREEN))
                .append(Component.text(String.format("%.2f%s", totalCost, currencySymbolLegacy), NamedTextColor.GOLD))
                .append(Component.text(".", NamedTextColor.GREEN)));
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(Component.text(buyer.getName(), NamedTextColor.GOLD)
                    .append(Component.text(" dükkanınızdan ", NamedTextColor.YELLOW))
                    .append(Component.text(totalItemsToBuy + " ", NamedTextColor.AQUA))
                    .append(formattedItemName.colorIfAbsent(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(" satın aldı.", NamedTextColor.YELLOW)));
        }
        return true;
    }

    public boolean executeSellToShop(Player seller, Shop shop, int bundlesToSell) {
        if (shop == null || !shop.isSetupComplete() || seller == null || bundlesToSell <= 0 || shop.getTemplateItemStack() == null) {
            this.plugin.getLogger().warning("[ShopTransactionManager-SellToShop] Geçersiz parametreler veya dükkan durumu.");
            if (seller != null) seller.sendMessage(Component.text("Satış işlemi sırasında bir sorun oluştu.", NamedTextColor.RED));
            return false;
        }

        if (shop.getSellPrice() < 0) {
            seller.sendMessage(Component.text("Bu dükkan şu anda ürün almıyor.", NamedTextColor.RED));
            return false;
        }

        int itemsPerBundle = shop.getItemQuantityForPrice();
        int totalItemsToSell = bundlesToSell * itemsPerBundle;
        double totalPaymentToPlayer = bundlesToSell * shop.getSellPrice();
        ItemStack templateItem = shop.getTemplateItemStack();
        String formattedItemNameLegacy = this.shopManager.getShopSignManager().getItemNameForMessages(templateItem, 30);
        Component formattedItemName = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedItemNameLegacy);
        String currencySymbolLegacy = this.shopManager.getCurrencySymbol();

        if (totalItemsToSell <= 0 || totalPaymentToPlayer < 0) {
            this.plugin.getLogger().warning("[ShopTransactionManager-SellToShop] Geçersiz hesaplanmış miktar veya ödeme. Dükkan: " + Shop.locationToString(shop.getLocation()));
            seller.sendMessage(Component.text("Bu ürün için dükkan yapılandırma hatası.", NamedTextColor.RED));
            return false;
        }

        if (ShopInventoryManager.countItemsInInventory(seller, templateItem) < totalItemsToSell) {
            seller.sendMessage(Component.text("Satmak için yeterli ", NamedTextColor.RED)
                    .append(formattedItemName.colorIfAbsent(NamedTextColor.AQUA))
                    .append(Component.text(" ürününüz yok. " + totalItemsToSell + " adet gerekli.", NamedTextColor.RED)));
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            seller.sendMessage(Component.text("Dükkan sandığı bulunamadı!", NamedTextColor.RED));
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] Dükkan bloğu (" + Shop.locationToString(shop.getLocation()) + ") sandık değil.");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (!ShopInventoryManager.hasEnoughSpaceInChest(chest, templateItem, totalItemsToSell)) {
            seller.sendMessage(Component.text("Dükkanda ", NamedTextColor.RED)
                    .append(Component.text(totalItemsToSell + " ", NamedTextColor.AQUA))
                    .append(formattedItemName.colorIfAbsent(NamedTextColor.AQUA))
                    .append(Component.text(" için yeterli yer yok.", NamedTextColor.RED)));
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        if (!EconomyManager.isEconomyAvailable()) {
            seller.sendMessage(Component.text("Ekonomi sistemi mevcut değil.", NamedTextColor.RED));
            return false;
        }
        if (EconomyManager.getBalance(owner) < totalPaymentToPlayer) {
            seller.sendMessage(Component.text("Dükkan sahibinin ürünlerinizi alacak kadar parası yok.", NamedTextColor.RED));
            if (owner.isOnline() && owner.getPlayer() != null) {
                owner.getPlayer().sendMessage(Component.text(Shop.locationToString(shop.getLocation()) + " konumundaki dükkanınız, " + seller.getName() + " adlı oyuncudan " + totalItemsToSell + " ", NamedTextColor.RED)
                        .append(formattedItemName.colorIfAbsent(NamedTextColor.AQUA))
                        .append(Component.text(" alacak kadar paraya sahip değildi.", NamedTextColor.RED)));
            }
            return false;
        }

        if (!EconomyManager.withdraw(owner, totalPaymentToPlayer)) {
            seller.sendMessage(Component.text("Dükkan sahibinden ödeme işlenirken hata oluştu. Lütfen tekrar deneyin.", NamedTextColor.RED));
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] Sahip " + owner.getName() + " hesabından " + totalPaymentToPlayer + " çekilemedi. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        if (!EconomyManager.deposit(seller, totalPaymentToPlayer)) {
            EconomyManager.deposit(owner, totalPaymentToPlayer); // Refund owner
            seller.sendMessage(Component.text("KRİTİK HATA: ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("Hesabınıza para yatırılamadı. Sahip iade edildi.", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] KRİTİK: Satıcı " + seller.getName() + " hesabına " + totalPaymentToPlayer + " yatırılamadı. Sahip " + owner.getName() + " iade edildi. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        if (!ShopInventoryManager.removeItemsFromInventory(seller, templateItem, totalItemsToSell)) {
            EconomyManager.withdraw(seller, totalPaymentToPlayer);
            EconomyManager.deposit(owner, totalPaymentToPlayer);
            seller.sendMessage(Component.text("KRİTİK HATA: ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("Envanterinizden ürünler kaldırılamadı. İşlem geri alındı.", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] KRİTİK: " + seller.getName() + " envanterinden " + totalItemsToSell + " adet " + templateItem.getType() + " kaldırılamadı. İşlem geri alındı. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        ItemStack itemsToAdd = templateItem.clone();
        itemsToAdd.setAmount(totalItemsToSell);
        chest.getInventory().addItem(itemsToAdd.clone());

        shop.recordPlayerSaleToShop(totalItemsToSell, totalPaymentToPlayer);
        this.shopManager.saveShop(shop);

        seller.sendMessage(Component.text("Başarıyla ", NamedTextColor.GREEN)
                .append(Component.text(totalItemsToSell + " ", NamedTextColor.AQUA))
                .append(formattedItemName.colorIfAbsent(NamedTextColor.AQUA))
                .append(Component.text(" dükkana sattınız, kazanç: ", NamedTextColor.GREEN))
                .append(Component.text(String.format("%.2f%s", totalPaymentToPlayer, currencySymbolLegacy), NamedTextColor.GOLD))
                .append(Component.text(".", NamedTextColor.GREEN)));
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(Component.text(Shop.locationToString(shop.getLocation()) + " konumundaki dükkanınız ", NamedTextColor.YELLOW)
                    .append(Component.text(seller.getName(), NamedTextColor.GOLD))
                    .append(Component.text(" adlı oyuncudan ", NamedTextColor.YELLOW))
                    .append(Component.text(totalItemsToSell + " ", NamedTextColor.AQUA))
                    .append(formattedItemName.colorIfAbsent(NamedTextColor.AQUA))
                    .append(Component.text(" satın aldı, ödenen: ", NamedTextColor.YELLOW))
                    .append(Component.text(String.format("%.2f%s", totalPaymentToPlayer, currencySymbolLegacy), NamedTextColor.GOLD))
                    .append(Component.text(".", NamedTextColor.YELLOW)));
        }
        return true;
    }
}
