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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ShopManager {

    private final SkyBlockProject plugin;
    private final ShopStorage shopStorage;
    private final Map<Location, Shop> activeShops; // Tamamlanmış ve aktif mağazalar
    private final Map<Location, Shop> pendingShops; // Kurulumu devam eden mağazalar

    public ShopManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.shopStorage = new ShopStorage(plugin);
        this.activeShops = this.shopStorage.loadShops();
        this.pendingShops = new HashMap<>();
        plugin.getLogger().info(activeShops.size() + " aktif mağaza ShopManager'a yüklendi.");
    }

    /**
     * Yeni bir mağaza kurulum sürecini başlatır.
     * ShopType başlangıçta null olabilir, GUI'de oyuncu tarafından seçilecektir.
     */
    public Shop initiateShopCreation(Location location, Player player, ShopType initialShopType) {
        // Zaten bir mağaza veya kurulumu devam eden bir mağaza var mı kontrol et
        if (isShop(location)) { // Hem aktif hem de pending kontrolü yapar
            Shop existing = activeShops.get(location);
            if (existing == null) existing = pendingShops.get(location);
            return existing; // Mevcut mağazayı veya pending olanı döndür
        }
        Shop newShop = new Shop(location, player.getUniqueId(), initialShopType);
        pendingShops.put(location, newShop);
        plugin.getLogger().info("Pending shop initiated at " + Shop.locationToString(location) + " by " + player.getName());
        return newShop;
    }

    /**
     * Kurulumu devam eden bir mağazayı konumundan alır.
     */
    public Shop getPendingShop(Location location) {
        return pendingShops.get(location);
    }

    /**
     * Mağaza kurulumunu tamamlar: pending listesinden aktif listesine taşır,
     * verilerini ayarlar, kaydeder ve tabelasını günceller.
     */
    public void finalizeShopSetup(Location location, Material itemType, int itemQuantityForPrice, double price) {
        Shop shop = pendingShops.remove(location);
        if (shop == null) {
            plugin.getLogger().warning("Tamamlanacak bekleyen mağaza bulunamadı: " + Shop.locationToString(location));
            return;
        }
        if (shop.getShopType() == null) {
            plugin.getLogger().warning("Mağaza türü belirlenmeden kurulum tamamlanamaz: " + Shop.locationToString(location));
            // Oyuncuya mesaj gönderilebilir ve kurulum iptal edilebilir.
            // shopStorage.removeShop(location); // Eğer pending shop'lar da storage'da tutulsaydı.
            return;
        }
        shop.setItemType(itemType);
        shop.setItemQuantityForPrice(itemQuantityForPrice);
        shop.setPrice(price);
        shop.setSetupComplete(true); // Kurulumu tamamlandı olarak işaretle
        activeShops.put(location, shop); // Aktif mağazalar listesine ekle
        shopStorage.saveShop(shop); // Kalıcı olarak kaydet
        updateShopSign(shop); // Tabelayı oluştur/güncelle
        plugin.getLogger().info("Shop finalized: " + Shop.locationToString(location) + " | Item: " + itemType + " Qty: " + itemQuantityForPrice + " Price: " + price);
    }

    /**
     * Bir konumda aktif (kurulumu tamamlanmış) bir mağaza olup olmadığını kontrol eder.
     * Bu metod `ShopListener` gibi yerlerde kullanılabilir.
     */
    public boolean isActiveShop(Location location) {
        Shop shop = activeShops.get(location);
        return shop != null && shop.isSetupComplete();
    }

    /**
     * Bir konumda herhangi bir mağaza (aktif veya kurulumda) olup olmadığını kontrol eder.
     */
    public boolean isShop(Location location) {
        return activeShops.containsKey(location) || pendingShops.containsKey(location);
    }

    /**
     * Aktif bir mağazayı konumundan alır.
     */
    public Shop getActiveShop(Location location) {
        return activeShops.get(location);
    }

    /**
     * Mağazayı ve ilişkili verileri (tabela vb.) kaldırır.
     */
    public void removeShop(Location location, Player player) {
        Shop shopToRemove = activeShops.remove(location);
        boolean wasPending = false;
        if (shopToRemove == null) {
            shopToRemove = pendingShops.remove(location);
            if (shopToRemove != null) wasPending = true;
        }

        if (shopToRemove == null) {
            player.sendMessage(ChatColor.RED + "Bu konumda kaldırılacak bir mağaza bulunmuyor.");
            return;
        }
        if (!shopToRemove.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("skyblock.admin.removeshop")) {
            // Eğer mağaza silinemezse, geri ekleyebiliriz (duruma göre)
            if (wasPending) pendingShops.put(location, shopToRemove);
            else activeShops.put(location, shopToRemove);
            player.sendMessage(ChatColor.RED + "Bu mağazayı kaldırma yetkiniz yok.");
            return;
        }

        shopStorage.removeShop(location); // Kalıcı hafızadan sil
        clearShopSign(location); // Tabelayı temizle/kaldır
        player.sendMessage(ChatColor.GREEN + "Mağaza başarıyla " + (wasPending ? "kurulumdan " : "") + "kaldırıldı.");
        plugin.getLogger().info("Shop removed at " + Shop.locationToString(location) + " by " + player.getName());
    }

    private String shortenItemName(String name, int maxLength) {
        name = name.toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        String formattedName = sb.toString().trim();
        if (formattedName.length() <= maxLength) {
            return formattedName;
        }
        return formattedName.substring(0, maxLength - 3) + "...";
    }
    private String shortenItemName(String name) { // Tabela için varsayılan uzunluk
        return shortenItemName(name, 15);
    }


    /**
     * Mağaza tabelasını sandığın ÜSTÜNE günceller veya oluşturur.
     */
    public void updateShopSign(Shop shop) {
        if (shop == null || !shop.isSetupComplete() || shop.getLocation() == null) return;

        Block chestBlock = shop.getLocation().getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST) {
            return;
        }

        Block signBlock = chestBlock.getRelative(BlockFace.UP);

        if (signBlock.getType() != Material.AIR && !(signBlock.getState() instanceof Sign)) {
            plugin.getLogger().fine("Mağaza (" + Shop.locationToString(shop.getLocation()) + ") için sandığın üzeri tabela için uygun değil.");
            return;
        }

        if (!(signBlock.getState() instanceof Sign)) {
            signBlock.setType(Material.OAK_SIGN, false); // Ayakta duran tabela
        }

        if (signBlock.getState() instanceof Sign) {
            Sign signState = (Sign) signBlock.getState();
            org.bukkit.block.data.BlockData blockData = signBlock.getBlockData();

            if (blockData instanceof org.bukkit.block.data.type.Sign) {
                org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) blockData;
                signData.setRotation(BlockFace.SOUTH); // Tabelanın ön yüzü Güney'e bakacak
                signBlock.setBlockData(signData, false);
            } else {
                plugin.getLogger().warning("Blok ayakta duran bir tabela türüne dönüştürülemedi: " + signBlock.getType() + " at " + Shop.locationToString(signBlock.getLocation()));
                return;
            }

            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
            String ownerName = owner.getName() != null ? shortenItemName(owner.getName(), 14) : "Bilinmeyen";

            String itemName = shortenItemName(shop.getItemType().toString());
            String priceInfo = shop.getItemQuantityForPrice() + "/" + String.format("%.1f", shop.getPrice()) + "$";
            if (priceInfo.length() > 15) priceInfo = shop.getItemQuantityForPrice() + "/" + String.format("%.0f", shop.getPrice()) + "$";


            signState.line(0, Component.text("[Mağaza]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD));
            signState.line(1, Component.text(itemName, NamedTextColor.BLACK));
            signState.line(2, Component.text(priceInfo, NamedTextColor.DARK_GREEN));
            signState.line(3, Component.text(ownerName, NamedTextColor.DARK_PURPLE));
            signState.update(true);
        } else {
            plugin.getLogger().warning("Tabela state'i alınamadı: "+ Shop.locationToString(signBlock.getLocation()));
        }
    }

    /**
     * Sandığın üzerindeki mağaza tabelasını temizler/kaldırır.
     */
    private void clearShopSign(Location chestLocation) {
        if (chestLocation == null) return;
        Block signBlock = chestLocation.getBlock().getRelative(BlockFace.UP);
        if (Tag.SIGNS.isTagged(signBlock.getType()) && signBlock.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
            org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) signBlock.getBlockData();
            if (!(signData instanceof WallSign)) { // Sadece ayakta duran tabelaları kaldır
                signBlock.setType(Material.AIR);
                plugin.getLogger().info("Mağaza tabelası (üste) temizlendi: " + Shop.locationToString(chestLocation));
            }
        }
    }

    /**
     * Oyuncunun envanterinde belirli bir item için yeterli yer olup olmadığını kontrol eder.
     * Bu metod artık `ShopVisitListener` tarafından kullanılacak.
     */
    public boolean hasEnoughSpace(Player player, ItemStack itemToReceive) {
        Inventory inv = player.getInventory();
        if (itemToReceive == null || itemToReceive.getType() == Material.AIR || itemToReceive.getAmount() <= 0) return true;
        int amountNeeded = itemToReceive.getAmount();
        for (ItemStack slotItem : inv.getStorageContents()) {
            if (amountNeeded <= 0) break;
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                amountNeeded -= itemToReceive.getMaxStackSize();
            } else if (slotItem.isSimilar(itemToReceive)) {
                amountNeeded -= (slotItem.getMaxStackSize() - slotItem.getAmount());
            }
        }
        return amountNeeded <= 0;
    }

    /**
     * Sandıktan belirtilen miktarda ve türde itemi çeker.
     * Bu metod artık `ShopVisitListener` tarafından kullanılacak.
     */
    public boolean removeItemsFromChest(Chest chest, Material itemType, int amountToRemove) {
        if (chest == null) return false;
        Inventory chestInventory = chest.getInventory();
        if (countItemsInChest(chest, itemType) < amountToRemove) {
            return false;
        }
        int removedCount = 0;
        ItemStack[] contents = chestInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == itemType) {
                int amountInSlot = item.getAmount();
                int canRemoveFromSlot = Math.min(amountToRemove - removedCount, amountInSlot);
                item.setAmount(amountInSlot - canRemoveFromSlot);
                removedCount += canRemoveFromSlot;
                if (item.getAmount() <= 0) {
                    contents[i] = null;
                }
                if (removedCount >= amountToRemove) break;
            }
        }
        chestInventory.setContents(contents);
        return removedCount >= amountToRemove;
    }

    /**
     * Sandıktaki belirli bir türdeki toplam item sayısını verir.
     * Bu metod artık `ShopVisitListener` veya `ShopVisitGUIManager` tarafından kullanılacak.
     */
    public int countItemsInChest(Chest chest, Material itemType) {
        int count = 0;
        if (chest == null) return 0; // chest.getInventory() null olamaz eğer chest geçerli bir Chest state ise
        Inventory chestInventory = chest.getInventory();
        for (ItemStack item : chestInventory.getContents()) {
            if (item != null && item.getType() == itemType) {
                count += item.getAmount();
            }
        }
        return count;
    }

    // processPurchase, executeTradeShopPurchase, executeBankShopPurchase metodları
    // ana mantıklarıyla birlikte ShopVisitListener'a taşınacak.
    // ShopManager bu işlemleri doğrudan yapmayacak, sadece yardımcı metodlar sunacak.
    // Bu nedenle bu metodlar buradan kaldırıldı.
}