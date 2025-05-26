// com/knemis/skyblock/skyblockcoreproject/shop/ShopManager.java
package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy; // Vault Economy importu

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ShopManager {

    private final SkyBlockProject plugin;
    private final ShopStorage shopStorage;
    private final Map<Location, Shop> activeShops;
    private final Map<Location, Shop> pendingShops;

    public ShopManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopStorage = new ShopStorage(plugin);
        this.activeShops = this.shopStorage.loadShops();
        this.pendingShops = new HashMap<>();
        if (this.activeShops != null && !this.activeShops.isEmpty()) {
            plugin.getLogger().info(this.activeShops.size() + " aktif mağaza ShopManager'a yüklendi.");
        } else if (this.activeShops != null) {
            plugin.getLogger().info("ShopManager'a yüklenecek aktif mağaza bulunamadı.");
        } else {
            plugin.getLogger().warning("ShopStorage.loadShops() null döndü, aktif mağaza yüklenemedi.");
        }
    }

    public Shop initiateShopCreation(Location location, Player player, ShopType initialShopType) {
        if (location == null || player == null) {
            plugin.getLogger().warning("initiateShopCreation: Konum veya oyuncu null geldi.");
            return null;
        }
        if (isShop(location)) {
            Shop existing = getActiveShop(location);
            if (existing == null) existing = getPendingShop(location);
            if (existing != null && existing.getOwnerUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Bu sandıkta zaten bir mağaza kurulumunuz var veya aktif bir mağazanız bulunuyor.");
                return existing;
            } else if (existing != null) {
                player.sendMessage(ChatColor.RED + "Bu sandık zaten başkasına ait bir mağaza olarak kullanılıyor.");
                return null;
            }
        }
        Shop newShop = new Shop(location, player.getUniqueId(), initialShopType);
        pendingShops.put(location, newShop);
        plugin.getLogger().info("Pending shop initiated at " + Shop.locationToString(location) + " by " + player.getName());
        return newShop;
    }

    public Shop getPendingShop(Location location) {
        if (location == null) return null;
        return pendingShops.get(location);
    }

    public void finalizeShopSetup(Location location, ItemStack templateItemStack, int itemQuantityForPrice, double price, Player actor, ItemStack initialStockItem) {
        if (location == null || templateItemStack == null || actor == null) {
            plugin.getLogger().severe("finalizeShopSetup: Null parametreler alındı!");
            if (actor != null) actor.sendMessage(ChatColor.RED + "Mağaza kurulumu sırasında beklenmedik bir hata oluştu (null parametre).");
            if (location != null) pendingShops.remove(location);
            return;
        }
        Shop shop = pendingShops.remove(location);
        if (shop == null) {
            plugin.getLogger().warning("Tamamlanacak bekleyen mağaza bulunamadı: " + Shop.locationToString(location));
            actor.sendMessage(ChatColor.RED + "Mağaza kurulum bilgisi bulunamadı, lütfen baştan başlayın.");
            return;
        }
        if (shop.getShopType() == null) {
            plugin.getLogger().warning("Mağaza türü belirlenmeden kurulum tamamlanamaz: " + Shop.locationToString(location));
            actor.sendMessage(ChatColor.RED + "Mağaza türü seçilmemiş! Kurulum iptal edildi.");
            return;
        }
        if (templateItemStack.getType() == Material.AIR || itemQuantityForPrice <= 0 || price < 0) {
            plugin.getLogger().warning("Geçersiz parametrelerle mağaza kurulumu tamamlanamaz: " + Shop.locationToString(location));
            actor.sendMessage(ChatColor.RED + "Geçersiz eşya, miktar veya fiyat! Kurulum iptal edildi.");
            return;
        }

        shop.setTemplateItemStack(templateItemStack.clone());
        shop.setItemQuantityForPrice(itemQuantityForPrice);
        shop.setPrice(price);
        shop.setSetupComplete(true);
        activeShops.put(location, shop);
        shopStorage.saveShop(shop);
        updateAttachedSign(shop);

        // Add initial stock to the chest
        if (initialStockItem != null && initialStockItem.getType() != Material.AIR) {
            Block shopBlock = location.getBlock();
            if (shopBlock.getState() instanceof Chest) {
                Chest chest = (Chest) shopBlock.getState();
                Inventory chestInventory = chest.getInventory();
                chestInventory.clear(); // Optional: Clear before adding
                chestInventory.addItem(initialStockItem.clone());
                plugin.getLogger().info("Initial stock of " + initialStockItem.getAmount() + "x " +
                        initialStockItem.getType() + " added to shop at " + Shop.locationToString(location));
            } else {
                plugin.getLogger().severe("Shop block at " + Shop.locationToString(location) + " is not a Chest. Cannot add initial stock.");
            }
        } else {
            plugin.getLogger().warning("Initial stock item was null or AIR for shop at " + Shop.locationToString(location) + ". No stock added.");
        }

        plugin.getLogger().info("Shop finalized by " + actor.getName() + ": " + Shop.locationToString(location) +
                " | Item: " + templateItemStack.getType() +
                " (Meta: " + (templateItemStack.hasItemMeta() ? "Evet" : "Hayır") + ")" +
                " Qty: " + itemQuantityForPrice + " Price: " + price +
                (initialStockItem != null && initialStockItem.getType() != Material.AIR ? " | Initial stock added: " + initialStockItem.getAmount() + "x " + initialStockItem.getType() : " | No initial stock specified"));
        actor.sendMessage(ChatColor.GREEN + "Mağazanız başarıyla kuruldu ve başlangıç stoku eklendi!");
    }

    public boolean isActiveShop(Location location) {
        if (location == null) return false;
        Shop shop = activeShops.get(location);
        return shop != null && shop.isSetupComplete();
    }

    public boolean isShop(Location location) {
        if (location == null) return false;
        return activeShops.containsKey(location) || pendingShops.containsKey(location);
    }

    public Shop getActiveShop(Location location) {
        if (location == null) return null;
        return activeShops.get(location);
    }

    public void removeShop(Location location, Player player) {
        if (location == null || player == null) return;
        Shop shopToRemove = getActiveShop(location);
        boolean wasPending = false;
        if (shopToRemove == null) {
            shopToRemove = getPendingShop(location);
            if (shopToRemove != null) wasPending = true;
        }
        if (shopToRemove == null) {
            player.sendMessage(ChatColor.RED + "Bu konumda kaldırılacak bir mağaza bulunmuyor.");
            return;
        }
        if (!shopToRemove.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("skyblock.admin.removeshop")) {
            player.sendMessage(ChatColor.RED + "Bu mağazayı kaldırma yetkiniz yok.");
            return;
        }
        if (!wasPending) activeShops.remove(location);
        else pendingShops.remove(location);
        shopStorage.removeShop(location);
        clearAttachedSign(location);
        player.sendMessage(ChatColor.GREEN + "Mağaza başarıyla " + (wasPending ? "kurulumdan " : "") + "kaldırıldı.");
        plugin.getLogger().info("Shop removed at " + Shop.locationToString(location) + " by " + player.getName());
    }

    public String shortenFormattedString(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength < 3) maxLength = 3;
        String cleanText = ChatColor.stripColor(text);
        if (cleanText.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.min(text.length(), maxLength - 3)) + "...";
    }

    public String shortenItemName(String name) {
        return shortenFormattedString(name, 15);
    }

    public String getItemDisplayNameForSign(ItemStack itemStack, int maxLength) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Boş";
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            try {
                Component displayNameComponent = meta.displayName();
                if (displayNameComponent != null) {
                    return shortenFormattedString(LegacyComponentSerializer.legacySection().serialize(displayNameComponent), maxLength);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.FINER, "Item özel adı (Component) alınırken/kısaltılırken hata (legacy denenecek): " + itemStack.getType(), e);
                if (meta.getDisplayName() != null && !meta.getDisplayName().isEmpty()) { // Bukkit'in eski getDisplayName'i
                    return shortenFormattedString(meta.getDisplayName(), maxLength);
                }
            }
        }
        String materialName = itemStack.getType().toString().toLowerCase().replace("_", " ");
        String[] words = materialName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return shortenFormattedString(sb.toString().trim(), maxLength);
    }

    public void updateAttachedSign(Shop shop) {
        if (shop == null || !shop.isSetupComplete() || shop.getLocation() == null || shop.getTemplateItemStack() == null) {
            return;
        }
        Block chestBlock = shop.getLocation().getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST) {
            return;
        }
        Sign signState = findOrCreateAttachedSign(chestBlock);
        if (signState == null) {
            plugin.getLogger().warning("Mağaza (" + Shop.locationToString(shop.getLocation()) + ") için uygun bir tabela yeri bulunamadı/oluşturulamadı.");
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        String ownerName = owner.getName() != null ? shortenFormattedString(owner.getName(), 14) : "Bilinmeyen";
        String itemName = getItemDisplayNameForSign(shop.getTemplateItemStack(), 15);

        String currencySymbol = "$"; // Varsayılan
        Economy econ = plugin.getEconomy();
        if (econ != null) {
            String singular = econ.currencyNameSingular();
            String plural = econ.currencyNamePlural();
            if (singular != null && !singular.isEmpty() && !Character.isLetterOrDigit(singular.charAt(0)) && singular.length() == 1) { // Tek karakterli sembol mü?
                currencySymbol = singular;
            } else if (plural != null && !plural.isEmpty()) {
                currencySymbol = " " + plural; // Dolar, Euro gibi
            } else if (singular != null && !singular.isEmpty()){
                currencySymbol = " " + singular;
            }
        }

        String priceInfo = shop.getItemQuantityForPrice() + "/" + String.format("%.1f", shop.getPrice()) + currencySymbol;
        if (ChatColor.stripColor(priceInfo).length() > 15) {
            priceInfo = shop.getItemQuantityForPrice() + "/" + String.format("%.0f", shop.getPrice()) + currencySymbol;
        }

        signState.line(0, Component.text("[Mağaza]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD));
        signState.line(1, Component.text(itemName, NamedTextColor.BLACK));
        signState.line(2, Component.text(priceInfo, NamedTextColor.DARK_GREEN));
        signState.line(3, Component.text(ownerName, NamedTextColor.DARK_PURPLE));
        signState.update(true);
    }

    private Sign findOrCreateAttachedSign(Block chestBlock) {
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        Material signMaterial = Material.OAK_WALL_SIGN;

        for (BlockFace face : facesToTry) {
            Block relative = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(relative.getType()) && relative.getState() instanceof Sign) {
                if (relative.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) relative.getBlockData();
                    if (wallSignData.getFacing().getOppositeFace() == face) {
                        Sign sign = (Sign) relative.getState();
                        if (sign.line(0).equals(Component.text("[Mağaza]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                            return sign;
                        }
                    }
                }
            }
        }
        for (BlockFace face : facesToTry) {
            Block potentialSignBlock = chestBlock.getRelative(face);
            if (potentialSignBlock.getType() == Material.AIR) {
                potentialSignBlock.setType(signMaterial, false);
                if (potentialSignBlock.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) potentialSignBlock.getBlockData();
                    wallSignData.setFacing(face.getOppositeFace());
                    potentialSignBlock.setBlockData(wallSignData, true);
                    return (Sign) potentialSignBlock.getState();
                } else {
                    potentialSignBlock.setType(Material.AIR);
                }
            }
        }
        return null;
    }

    private void clearAttachedSign(Location chestLocation) {
        if (chestLocation == null) return;
        Block chestBlock = chestLocation.getBlock();
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        for (BlockFace face : facesToTry) {
            Block signBlock = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(signBlock.getType()) && signBlock.getBlockData() instanceof WallSign) {
                WallSign wallSignData = (WallSign) signBlock.getBlockData();
                if (wallSignData.getFacing().getOppositeFace() == face) {
                    Sign signState = (Sign) signBlock.getState();
                    if (signState.line(0).equals(Component.text("[Mağaza]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                        signBlock.setType(Material.AIR);
                        plugin.getLogger().info("Mağaza tabelası (yana bitişik) temizlendi: " + Shop.locationToString(chestLocation));
                        return;
                    }
                }
            }
        }
    }

    public boolean hasEnoughSpace(Player player, ItemStack itemToReceive) {
        Inventory inv = player.getInventory();
        if (itemToReceive == null || itemToReceive.getType() == Material.AIR || itemToReceive.getAmount() <= 0) return true;
        int amountNeeded = itemToReceive.getAmount();
        for (ItemStack slotItem : inv.getStorageContents()) {
            if (amountNeeded <= 0) break;
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                amountNeeded -= itemToReceive.getMaxStackSize();
            } else if (slotItem.isSimilar(itemToReceive)) {
                amountNeeded -= (Math.max(0, itemToReceive.getMaxStackSize() - slotItem.getAmount()));
            }
        }
        return amountNeeded <= 0;
    }

    public boolean removeItemsFromChest(Chest chest, ItemStack templateItemToRemove, int amountToRemove) {
        if (chest == null || templateItemToRemove == null || templateItemToRemove.getType() == Material.AIR || amountToRemove <= 0) return false;
        Inventory chestInventory = chest.getInventory();
        if (countItemsInChest(chest, templateItemToRemove) < amountToRemove) {
            return false;
        }
        int removedCount = 0;
        ItemStack[] contents = chestInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemInSlot = contents[i];
            if (itemInSlot != null && itemInSlot.isSimilar(templateItemToRemove)) {
                int amountInSlot = itemInSlot.getAmount();
                int canRemoveFromSlot = Math.min(amountToRemove - removedCount, amountInSlot);
                itemInSlot.setAmount(amountInSlot - canRemoveFromSlot);
                removedCount += canRemoveFromSlot;
                if (itemInSlot.getAmount() <= 0) {
                    contents[i] = null;
                }
                if (removedCount >= amountToRemove) break;
            }
        }
        chestInventory.setContents(contents);
        return removedCount >= amountToRemove;
    }

    public int countItemsInChest(Chest chest, ItemStack templateItemToMatch) {
        int count = 0;
        if (chest == null || templateItemToMatch == null || templateItemToMatch.getType() == Material.AIR) return 0;
        Inventory chestInventory = chest.getInventory();
        for (ItemStack itemInSlot : chestInventory.getContents()) {
            if (itemInSlot != null && itemInSlot.isSimilar(templateItemToMatch)) {
                count += itemInSlot.getAmount();
            }
        }
        return count;
    }

    public ShopStorage getShopStorage() {
        return this.shopStorage;
    }

    public List<Shop> getShopsByOwner(UUID ownerUUID) {
        if (ownerUUID == null) return new ArrayList<>();
        if (activeShops == null) return new ArrayList<>(); // Null kontrolü
        return activeShops.values().stream()
                .filter(shop -> shop != null && shop.getOwnerUUID().equals(ownerUUID)) // shop null kontrolü
                .collect(Collectors.toList());
    }

    public int getTotalActiveShops() {
        return activeShops != null ? activeShops.size() : 0; // Null kontrolü
    }

    /**
     * Satın alma işlemini gerçekleştiren ana metod.
     * Bu metod, listener tarafından çağrılır ve gerekli tüm kontrolleri,
     * ekonomi işlemlerini ve envanter güncellemelerini yapar.
     * @return Satın alma başarılıysa true.
     */
    public boolean executePurchase(Player buyer, Shop shop, int bundlesToBuy) {
        if (shop == null || !shop.isSetupComplete() || buyer == null || bundlesToBuy <= 0 || shop.getTemplateItemStack() == null) {
            plugin.getLogger().warning("[ShopManager-Purchase] Geçersiz parametreler veya mağaza durumu.");
            if (buyer != null) buyer.sendMessage(ChatColor.RED + "Satın alma işlemi sırasında bir sorun oluştu.");
            return false;
        }

        int itemsPerBundle = shop.getItemQuantityForPrice();
        int totalItemsToBuy = bundlesToBuy * itemsPerBundle;
        double totalCost = bundlesToBuy * shop.getPrice();
        ItemStack templateItem = shop.getTemplateItemStack();

        if (totalItemsToBuy <= 0 || totalCost < 0) { // Fiyat 0 olabilir (ücretsiz)
            plugin.getLogger().warning("[ShopManager-Purchase] Geçersiz hesaplanmış miktar veya fiyat. Mağaza: " + Shop.locationToString(shop.getLocation()));
            buyer.sendMessage(ChatColor.RED + "Mağaza ayarlarında bir sorun var.");
            return false;
        }

        String formattedItemName = getItemDisplayNameForSign(templateItem, 30);
        String currencySymbol = getCurrencySymbol();


        if (!EconomyManager.isEconomyAvailable()) {
            buyer.sendMessage(ChatColor.RED + "Ekonomi sistemi mevcut değil.");
            return false;
        }
        if (EconomyManager.getBalance(buyer) < totalCost) {
            buyer.sendMessage(ChatColor.RED + "Yetersiz bakiye! Gereken: " + String.format("%.2f", totalCost) + currencySymbol);
            return false;
        }

        Block shopBlock = shop.getLocation().getBlock();
        if (!(shopBlock.getState() instanceof Chest)) {
            buyer.sendMessage(ChatColor.RED + "Mağaza sandığı bulunamadı!");
            return false;
        }
        Chest chest = (Chest) shopBlock.getState();
        if (countItemsInChest(chest, templateItem) < totalItemsToBuy) {
            buyer.sendMessage(ChatColor.RED + "Mağazada yeterli stok yok (" + totalItemsToBuy + " adet " + formattedItemName + " gerekli).");
            updateAttachedSign(shop);
            return false;
        }

        ItemStack itemsToReceive = templateItem.clone();
        itemsToReceive.setAmount(totalItemsToBuy);
        if (!hasEnoughSpace(buyer, itemsToReceive)) {
            buyer.sendMessage(ChatColor.RED + "Envanterinizde " + totalItemsToBuy + " adet " + formattedItemName + " için yeterli yer yok!");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());

        if (!EconomyManager.withdraw(buyer, totalCost)) {
            buyer.sendMessage(ChatColor.RED + "Para çekme işlemi başarısız oldu.");
            return false;
        }

        if (!EconomyManager.deposit(owner, totalCost)) {
            EconomyManager.deposit(buyer, totalCost); // Alıcıya iade et
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Satıcıya para aktarılamadı. Paranız iade edildi!");
            plugin.getLogger().severe("[ShopManager-Purchase] KRİTİK: Sahip " + (owner.getName() != null ? owner.getName() : owner.getUniqueId()) + " hesabına para yatırılamadı. Alıcı " + buyer.getName() + " parası iade edildi.");
            return false;
        }

        if (!removeItemsFromChest(chest, templateItem, totalItemsToBuy)) {
            EconomyManager.withdraw(owner, totalCost); // Satıcıdan geri al
            EconomyManager.deposit(buyer, totalCost);  // Alıcıya iade et
            buyer.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "KRİTİK HATA: " + ChatColor.RESET + ChatColor.RED + "Eşyalar mağazadan alınamadı. Paranız iade edildi!");
            plugin.getLogger().severe("[ShopManager-Purchase] KRİTİK: Sandıktan eşya çekilemedi. Para transferleri geri alındı. Mağaza: " + Shop.locationToString(shop.getLocation()));
            return false;
        }

        buyer.getInventory().addItem(itemsToReceive.clone());
        shop.recordTransaction(totalItemsToBuy, totalCost);
        shopStorage.saveShop(shop);

        buyer.sendMessage(ChatColor.GREEN + "Başarıyla " + ChatColor.AQUA + totalItemsToBuy + " adet " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.GREEN + " satın aldınız (" + ChatColor.GOLD + String.format("%.2f", totalCost) + currencySymbol + ChatColor.GREEN + ").");
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(ChatColor.GOLD + buyer.getName() + ChatColor.YELLOW + " mağazanızdan " +
                    ChatColor.AQUA + totalItemsToBuy + " adet " + ChatColor.LIGHT_PURPLE + formattedItemName + ChatColor.YELLOW + " satın aldı.");
        }
        updateAttachedSign(shop);
        return true;
    }

    /**
     * Vault ekonomisinden para birimi sembolünü veya adını alır.
     * @return Para birimi sembolü/adı veya varsayılan "$".
     */
    public String getCurrencySymbol() {
        Economy econ = plugin.getEconomy();
        if (econ != null) {
            String singular = econ.currencyNameSingular();
            // Genellikle semboller tek karakter olur ($) veya para birimi adı (USD)
            if (singular != null && !singular.isEmpty()) {
                if (singular.length() == 1 && !Character.isLetterOrDigit(singular.charAt(0))) {
                    return singular; // Sadece sembol ise ($)
                }
                return " " + singular; // Dolar, Euro gibi ise boşluklu
            }
            String plural = econ.currencyNamePlural();
            if (plural != null && !plural.isEmpty()) {
                return " " + plural;
            }
        }
        return "$"; // Varsayılan
    }
}