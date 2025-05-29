package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// Assuming EconomyManager and ShopInventoryManager are in the same package or imported correctly
// Assuming ShopSignManager will be accessible via ShopManager

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
            if (buyer != null) buyer.sendMessage(ChatColor.RED + "Satın alma işlemi sırasında bir hata oluştu.");
            return false;
        }

        int itemsPerBundle = shop.getBundleAmount();
        int totalItemsToBuy = bundlesToBuy * itemsPerBundle;

        if (shop.getBuyPrice() < 0) {
            buyer.sendMessage(ChatColor.RED + "Bu dükkan şu anda ürün satmıyor.");
            return false;
        }
        double totalCost = bundlesToBuy * shop.getBuyPrice();
        ItemStack templateItem = shop.getTemplateItemStack();

        if (totalItemsToBuy <= 0 || totalCost < 0) {
            this.plugin.getLogger().warning("[ShopTransactionManager-Purchase] Geçersiz hesaplanmış miktar veya fiyat. Dükkan: " + Shop.locationToString(shop.getLocation()));
            buyer.sendMessage(ChatColor.RED + "Dükkan ayarlarında bir sorun var.");
            return false;
        }

        String formattedItemName = this.shopManager.getShopSignManager().getItemNameForMessages(templateItem, 30);
        String currencySymbol = this.shopManager.getCurrencySymbol();

        if (!EconomyManager.isEconomyAvailable()) {
            buyer.sendMessage(ChatColor.RED + "Ekonomi sistemi mevcut değil.");
            return false;
        }
        if (EconomyManager.getBalance(buyer) < totalCost) {
            buyer.sendMessage(ChatColor.RED + "Yetersiz bakiye! Gereken: " + String.format("%.2f%s", totalCost, currencySymbol));
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(ChatColor.RED + "Dükkan sandığı bulunamadı!");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (ShopInventoryManager.countItemsInChest(chest, templateItem) < totalItemsToBuy) {
            buyer.sendMessage(ChatColor.RED + "Dükkanda yeterli stok yok (" + totalItemsToBuy + " " + formattedItemName + " gerekli).");
            this.shopManager.getShopSignManager().updateAttachedSign(shop, currencySymbol);
            return false;
        }

        ItemStack itemsToReceive = templateItem.clone();
        itemsToReceive.setAmount(totalItemsToBuy);
        if (!ShopInventoryManager.hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(ChatColor.RED + "Envanterinizde " + totalItemsToBuy + " " + formattedItemName + " için yeterli yer yok!");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        if (!EconomyManager.withdraw(buyer, totalCost)) {
            buyer.sendMessage(ChatColor.RED + "Ödeme çekme işlemi başarısız oldu.");
            return false;
        }

        if (!EconomyManager.deposit(owner, totalCost)) {
            EconomyManager.deposit(buyer, totalCost); // Refund buyer
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Satıcıya para transfer edilemedi. Paranız iade edildi!");
            this.plugin.getLogger().severe("[ShopTransactionManager-Purchase] KRİTİK: Para sahibi " + (owner.getName() != null ? owner.getName() : owner.getUniqueId()) + " hesabına yatırılamadı. Alıcı " + buyer.getName() + " iade edildi.");
            return false;
        }

        if (!ShopInventoryManager.removeItemsFromChest(chest, templateItem, totalItemsToBuy)) {
            EconomyManager.withdraw(owner, totalCost); // Revert owner's deposit
            EconomyManager.deposit(buyer, totalCost);  // Refund buyer
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Dükkandan ürünler alınamadı. Paranız iade edildi!");
            this.plugin.getLogger().severe("[ShopTransactionManager-Purchase] KRİTİK: Sandıktan ürünler çekilemedi. Para transferleri geri alındı. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        buyer.getInventory().addItem(itemsToReceive.clone());
        shop.recordTransaction(totalItemsToBuy, totalCost);
        this.shopManager.saveShop(shop); // Calls internal save which also updates sign via ShopSignManager

        buyer.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + totalItemsToBuy + " " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.GREEN + " satın aldınız, fiyat: " + ChatColor.GOLD + String.format("%.2f%s", totalCost, currencySymbol) + ChatColor.GREEN + ".");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.GOLD + buyer.getName() + ChatColor.YELLOW + " dükkanınızdan " +
                    ChatColor.AQUA + totalItemsToBuy + " " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.YELLOW + " satın aldı.");
        }
        // saveShop already calls updateAttachedSign
        return true;
    }

    public boolean executeSellToShop(Player seller, Shop shop, int bundlesToSell) {
        if (shop == null || !shop.isSetupComplete() || seller == null || bundlesToSell <= 0 || shop.getTemplateItemStack() == null) {
            this.plugin.getLogger().warning("[ShopTransactionManager-SellToShop] Geçersiz parametreler veya dükkan durumu.");
            if (seller != null) seller.sendMessage(ChatColor.RED + "Satış işlemi sırasında bir sorun oluştu.");
            return false;
        }

        if (shop.getSellPrice() < 0) {
            seller.sendMessage(ChatColor.RED + "Bu dükkan şu anda ürün almıyor.");
            return false;
        }

        int itemsPerBundle = shop.getItemQuantityForPrice();
        int totalItemsToSell = bundlesToSell * itemsPerBundle;
        double totalPaymentToPlayer = bundlesToSell * shop.getSellPrice();
        ItemStack templateItem = shop.getTemplateItemStack();
        String formattedItemName = this.shopManager.getShopSignManager().getItemNameForMessages(templateItem, 30);
        String currencySymbol = this.shopManager.getCurrencySymbol();

        if (totalItemsToSell <= 0 || totalPaymentToPlayer < 0) { // Allow 0 payment for free items if ever needed, but not negative.
            this.plugin.getLogger().warning("[ShopTransactionManager-SellToShop] Geçersiz hesaplanmış miktar veya ödeme. Dükkan: " + Shop.locationToString(shop.getLocation()));
            seller.sendMessage(ChatColor.RED + "Bu ürün için dükkan yapılandırma hatası.");
            return false;
        }

        if (ShopInventoryManager.countItemsInInventory(seller, templateItem) < totalItemsToSell) {
            seller.sendMessage(ChatColor.RED + "Satmak için yeterli " + ChatColor.AQUA + formattedItemName + ChatColor.RED + " ürününüz yok. " + totalItemsToSell + " adet gerekli.");
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            seller.sendMessage(ChatColor.RED + "Dükkan sandığı bulunamadı!");
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] Dükkan bloğu (" + Shop.locationToString(shop.getLocation()) + ") sandık değil.");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (!ShopInventoryManager.hasEnoughSpaceInChest(chest, templateItem, totalItemsToSell)) {
            seller.sendMessage(ChatColor.RED + "Dükkanda " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName + ChatColor.RED + " için yeterli yer yok.");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        if (!EconomyManager.isEconomyAvailable()) {
            seller.sendMessage(ChatColor.RED + "Ekonomi sistemi mevcut değil.");
            return false;
        }
        if (EconomyManager.getBalance(owner) < totalPaymentToPlayer) {
            seller.sendMessage(ChatColor.RED + "Dükkan sahibinin ürünlerinizi alacak kadar parası yok.");
            if (owner.isOnline() && owner.getPlayer() != null) {
                owner.getPlayer().sendMessage(ChatColor.RED + Shop.locationToString(shop.getLocation()) + " konumundaki dükkanınız, " + seller.getName() + " adlı oyuncudan " + totalItemsToSell + " " + formattedItemName + " alacak kadar paraya sahip değildi.");
            }
            return false;
        }

        if (!EconomyManager.withdraw(owner, totalPaymentToPlayer)) {
            seller.sendMessage(ChatColor.RED + "Dükkan sahibinden ödeme işlenirken hata oluştu. Lütfen tekrar deneyin.");
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] Sahip " + owner.getName() + " hesabından " + totalPaymentToPlayer + " çekilemedi. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        if (!EconomyManager.deposit(seller, totalPaymentToPlayer)) {
            EconomyManager.deposit(owner, totalPaymentToPlayer); // Refund owner
            seller.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Hesabınıza para yatırılamadı. Sahip iade edildi.");
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] KRİTİK: Satıcı " + seller.getName() + " hesabına " + totalPaymentToPlayer + " yatırılamadı. Sahip " + owner.getName() + " iade edildi. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        if (!ShopInventoryManager.removeItemsFromInventory(seller, templateItem, totalItemsToSell)) {
            EconomyManager.withdraw(seller, totalPaymentToPlayer); // Revert seller's deposit
            EconomyManager.deposit(owner, totalPaymentToPlayer);   // Refund owner
            seller.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Envanterinizden ürünler kaldırılamadı. İşlem geri alındı.");
            this.plugin.getLogger().severe("[ShopTransactionManager-SellToShop] KRİTİK: " + seller.getName() + " envanterinden " + totalItemsToSell + " adet " + templateItem.getType() + " kaldırılamadı. İşlem geri alındı. Dükkan: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        ItemStack itemsToAdd = templateItem.clone();
        itemsToAdd.setAmount(totalItemsToSell);
        chest.getInventory().addItem(itemsToAdd.clone());

        shop.recordPlayerSaleToShop(totalItemsToSell, totalPaymentToPlayer);
        this.shopManager.saveShop(shop); // Calls internal save which also updates sign

        seller.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + totalItemsToSell + " " + formattedItemName +
                ChatColor.GREEN + " dükkana sattınız, kazanç: " + ChatColor.GOLD + String.format("%.2f%s", totalPaymentToPlayer, currencySymbol) + ChatColor.GREEN + ".");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.YELLOW + Shop.locationToString(shop.getLocation()) + " konumundaki dükkanınız " +
                    ChatColor.GOLD + seller.getName() + ChatColor.YELLOW + " adlı oyuncudan " +
                    ChatColor.AQUA + totalItemsToSell + " " + formattedItemName +
                    ChatColor.YELLOW + " satın aldı, ödenen: " + ChatColor.GOLD + String.format("%.2f%s", totalPaymentToPlayer, currencySymbol) + ChatColor.YELLOW + ".");
        }
        // saveShop already calls updateAttachedSign
        return true;
    }
}
