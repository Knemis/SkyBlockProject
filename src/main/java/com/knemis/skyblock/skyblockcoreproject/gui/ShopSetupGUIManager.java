// com/knemis/skyblock/skyblockcoreproject/gui/ShopSetupGUIManager.java
package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager; // ShopManager importu eklendi
// import com.knemis.skyblock.skyblockcoreproject.shop.ShopType; // Zaten import edilmiş durumda

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Import the new session class
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;

public class ShopSetupGUIManager {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager; 
    private final Map<UUID, ShopSetupSession> activeSetupSessions = new HashMap<>();

    public static final Component ITEM_SELECT_TITLE = Component.text("Mağaza İçin Eşya Seç", Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD));
    public static final Component QUANTITY_INPUT_TITLE = Component.text("Miktar Belirle (Birim Başına)", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD));
    public static final Component PRICE_INPUT_TITLE = Component.text("Fiyat Belirle (Alış:Satış)", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD));
    public static final Component CONFIRMATION_TITLE = Component.text("Mağaza Kurulumunu Onayla", Style.style(NamedTextColor.RED, TextDecoration.BOLD));
    public static final Component SHOP_TYPE_TITLE = Component.text("Mağaza Türünü Seç", Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));


    public ShopSetupGUIManager(SkyBlockProject plugin, ShopManager shopManager) { // shopManager parametresi eklendi
        this.plugin = plugin;
        this.shopManager = shopManager; 
    }

    // Session Management Methods
    public ShopSetupSession getPlayerSession(UUID playerId) {
        return activeSetupSessions.get(playerId);
    }

    public ShopSetupSession createSession(Player player, Location chestLocation, Shop pendingShop, ItemStack initialStockItem) {
        ShopSetupSession session = new ShopSetupSession(player.getUniqueId(), chestLocation, pendingShop, initialStockItem);
        activeSetupSessions.put(player.getUniqueId(), session);
        plugin.getLogger().info("ShopSetupSession created for player " + player.getName() + " at " + chestLocation);
        return session;
    }

    public void removeSession(UUID playerId) {
        ShopSetupSession removedSession = activeSetupSessions.remove(playerId);
        if (removedSession != null) {
            plugin.getLogger().info("ShopSetupSession removed for player " + playerId + " from location " + removedSession.getChestLocation());
        } else {
            plugin.getLogger().warning("Attempted to remove a ShopSetupSession for player " + playerId + ", but no active session was found.");
        }
    }

    // Updated getPendingShop methods
    public Shop getPendingShop(Player player) {
        if (player == null) return null;
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        return (session != null) ? session.getPendingShop() : null;
    }

    public Shop getPendingShop(UUID playerId) {
        if (playerId == null) return null;
        ShopSetupSession session = getPlayerSession(playerId);
        return (session != null) ? session.getPendingShop() : null;
    }
    
    // GUI Opening Methods
    public void openShopTypeSelectionMenu(Player player) {
        // When opening the first menu in a potential setup, ensure no old session is lingering
        // or rely on the calling context (e.g. ShopListener) to manage session creation appropriately.
        // For now, this method doesn't directly interact with sessions itself, ShopListener will.
        Inventory gui = Bukkit.createInventory(player, 9, SHOP_TYPE_TITLE);

        ItemStack buyShopItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta buyMeta = buyShopItem.getItemMeta();
        if (buyMeta != null) {
            buyMeta.displayName(Component.text("Satın Alma Dükkanı", NamedTextColor.YELLOW));
            List<Component> buyLore = new ArrayList<>();
            buyLore.add(Component.text("Oyuncular bu dükkandan eşya satın alabilir.", NamedTextColor.GRAY));
            buyMeta.lore(buyLore);
            buyShopItem.setItemMeta(buyMeta);
        }

        ItemStack sellShopItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta sellMeta = sellShopItem.getItemMeta();
        if (sellMeta != null) {
            sellMeta.displayName(Component.text("Satış Dükkanı", NamedTextColor.AQUA));
            List<Component> sellLore = new ArrayList<>();
            sellLore.add(Component.text("Oyuncular bu dükkana eşya satabilir.", NamedTextColor.GRAY));
            sellMeta.lore(sellLore);
            sellShopItem.setItemMeta(sellMeta);
        }

        ItemStack buySellShopItem = new ItemStack(Material.DIAMOND);
        ItemMeta buySellMeta = buySellShopItem.getItemMeta();
        if (buySellMeta != null) {
            buySellMeta.displayName(Component.text("Alış/Satış Dükkanı", NamedTextColor.LIGHT_PURPLE));
            List<Component> buySellLore = new ArrayList<>();
            buySellLore.add(Component.text("Oyuncular hem satın alabilir hem de satabilir.", NamedTextColor.GRAY));
            buySellMeta.lore(buySellLore);
            buySellShopItem.setItemMeta(buySellMeta);
        }

        gui.setItem(2, buyShopItem);
        gui.setItem(4, sellShopItem);
        gui.setItem(6, buySellShopItem);

        player.openInventory(gui);
    }

    public void openItemSelectionMenu(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            plugin.getLogger().severe("Cannot open item selection menu for " + player.getName() + ": No setup session found.");
            player.sendMessage(ChatColor.RED + "Kurulum oturumu bulunamadı. Lütfen tekrar deneyin.");
            return;
        }
        session.setCurrentGuiTitle(ITEM_SELECT_TITLE.toString()); // Optional: track current GUI
        Inventory gui = Bukkit.createInventory(player, 54, ITEM_SELECT_TITLE);
        player.openInventory(gui);
    }

    public void openQuantityInputMenu(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            plugin.getLogger().severe("Cannot open quantity input menu for " + player.getName() + ": No setup session found.");
            player.sendMessage(ChatColor.RED + "Kurulum oturumu bulunamadı. Lütfen tekrar deneyin.");
            return;
        }
        session.setCurrentGuiTitle(QUANTITY_INPUT_TITLE.toString());
        // session.setExpectedInputType(InputType.QUANTITY); // Set if direct input is expected, or rely on click/close events.

        Inventory gui = Bukkit.createInventory(player, 27, QUANTITY_INPUT_TITLE);
        ItemStack templateItem = pendingShop.getTemplateItemStack();

        if (templateItem == null) {
            player.sendMessage(ChatColor.RED + "Önce bir eşya seçmelisiniz! Kurulum iptal ediliyor.");
            shopManager.cancelShopSetup(player.getUniqueId()); // Use shopManager instance
            return;
        }

        int[] quantities = {1, 8, 16, 32, 64};
        for (int i = 0; i < quantities.length; i++) {
            ItemStack quantityItem = templateItem.clone();
            quantityItem.setAmount(quantities[i]);

            ItemMeta meta = quantityItem.getItemMeta();
            if (meta != null) {
                Component currentDisplayName = meta.hasDisplayName() ? meta.displayName() : Component.translatable(templateItem.getType().getKey().toString());
                meta.displayName(Component.text(quantities[i] + " adet ", NamedTextColor.AQUA).append(currentDisplayName));
                List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
                if(lore == null) lore = new ArrayList<>();
                lore.add(Component.text(" "));
                lore.add(Component.text("Birim fiyat bu miktar için geçerli olacak.", NamedTextColor.YELLOW));
                lore.add(Component.text("Seçmek için tıkla.", NamedTextColor.GREEN));
                meta.lore(lore);
                quantityItem.setItemMeta(meta);
            }
            gui.setItem(10 + i, quantityItem);
        }
        ItemStack instructionItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta instructionMeta = instructionItem.getItemMeta();
        if (instructionMeta != null) {
            instructionMeta.displayName(Component.text("Özel Miktar Belirle", NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("İstediğin miktarda eşyayı (template ile aynı türde)", NamedTextColor.GRAY));
            lore.add(Component.text("aşağıdaki boş slota sürükle ve GUI'yi kapat.", NamedTextColor.GRAY));
            lore.add(Component.text("Eşyanın miktarı birim olarak kullanılacaktır.", NamedTextColor.YELLOW));
            instructionMeta.lore(lore);
            instructionItem.setItemMeta(instructionMeta);
        }
        gui.setItem(4, instructionItem);
        player.openInventory(gui);
    }

    public void openPriceInputPrompt(Player player, Shop pendingShop) {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "===== Fiyat Belirleme =====");

        // getItemNameForMessages benzeri bir metot ShopSetupListener'da var, onu kullanabiliriz veya burada da oluşturabiliriz.
        // Şimdilik basit bir gösterim:
        String itemName = pendingShop.getTemplateItemStack().getType().toString();
        if(pendingShop.getTemplateItemStack().hasItemMeta() && pendingShop.getTemplateItemStack().getItemMeta().hasDisplayName()){
            itemName = LegacyComponentSerializer.legacySection().serialize(pendingShop.getTemplateItemStack().getItemMeta().displayName());
        }

        int bundleAmount = pendingShop.getBundleAmount();

        player.sendMessage(ChatColor.YELLOW + "Satılacak Eşya: " + ChatColor.WHITE + itemName +
                ChatColor.YELLOW + " (Paket Miktarı: " + ChatColor.WHITE + bundleAmount + " adet" + ChatColor.YELLOW + ")");

        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            plugin.getLogger().severe("Could not find setup session for " + player.getName() + " when prompting for price.");
            player.sendMessage(ChatColor.RED + "Oturum hatası, fiyat girilemiyor.");
            // Attempt to cancel via ShopManager if possible, though without a session, it might be limited.
            // If ShopManager's cancelShopSetup now relies on session, this becomes problematic.
            // However, ShopSetupListener should ensure session exists before calling this.
            shopManager.cancelShopSetup(player.getUniqueId()); 
            return;
        }

        if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
            player.sendMessage(ChatColor.GRAY + "Lütfen bu paket için ALIŞ ve SATIŞ fiyatlarını chat'e girin.");
            player.sendMessage(ChatColor.GRAY + "Format: <oyuncunun_ALIŞ_fiyati>:<oyuncunun_SATIŞ_fiyati>");
            player.sendMessage(ChatColor.GRAY + "Örnek: " + ChatColor.WHITE + "100:80" + ChatColor.GRAY + " (Oyuncular 100'e alır, 80'e satar)");
            player.sendMessage(ChatColor.GRAY + "Sadece satış (oyuncu alamaz): " + ChatColor.WHITE + "-1:80");
            player.sendMessage(ChatColor.GRAY + "Sadece alış (oyuncu satamaz): " + ChatColor.WHITE + "100:-1");
        } else if (session.isIntentToAllowPlayerBuy()) {
            player.sendMessage(ChatColor.GRAY + "Lütfen bu paket için oyuncuların sizden SATIN ALACAĞI fiyatı girin.");
            player.sendMessage(ChatColor.GRAY + "Örnek: " + ChatColor.WHITE + "100");
            player.sendMessage(ChatColor.GRAY + "Bu dükkan oyunculara satış yapmayacak.");
        } else if (session.isIntentToAllowPlayerSell()) {
            player.sendMessage(ChatColor.GRAY + "Lütfen bu paket için oyuncuların size SATACAĞI fiyatı girin.");
            player.sendMessage(ChatColor.GRAY + "Örnek: " + ChatColor.WHITE + "80");
            player.sendMessage(ChatColor.GRAY + "Bu dükkan oyunculardan ürün almayacak.");
        } else {
            // This case should ideally not be reached if session intents are set correctly from type selection.
            player.sendMessage(ChatColor.RED + "Dükkanın alış veya satış amacı belirlenmemiş. Kurulum iptal ediliyor.");
            shopManager.cancelShopSetup(player.getUniqueId());
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Kurulumu iptal etmek için '" + ChatColor.RED + "iptal" + ChatColor.YELLOW + "' yazın.");
        
        session.setExpectedInputType(InputType.PRICE);
        session.setCurrentGuiTitle(null); // Chat input, no specific GUI title
        plugin.getLogger().info("Player " + player.getName() + " is now expected to input PRICE. Session updated with intents: Buy=" + session.isIntentToAllowPlayerBuy() + ", Sell=" + session.isIntentToAllowPlayerSell());
    }

    public enum InputType {
        PRICE, QUANTITY
    }

    public void openConfirmationMenu(Player player, Shop pendingShop, double buyPrice, double sellPrice) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            plugin.getLogger().severe("Cannot open confirmation menu for " + player.getName() + ": No setup session found.");
            player.sendMessage(ChatColor.RED + "Kurulum oturumu bulunamadı. Lütfen tekrar deneyin.");
            return;
        }
        session.setCurrentGuiTitle(CONFIRMATION_TITLE.toString());
        session.setExpectedInputType(null); // No further direct input expected after this GUI, interaction is via clicks.

        Inventory gui = Bukkit.createInventory(player, 27, CONFIRMATION_TITLE);
        ItemStack templateItem = pendingShop.getTemplateItemStack().clone();
        int bundleAmount = pendingShop.getBundleAmount();
        templateItem.setAmount(bundleAmount);

        ItemMeta meta = templateItem.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            if (meta.hasLore() && meta.lore() != null) {
                lore.addAll(meta.lore());
            }
            lore.add(Component.text(" "));
            lore.add(Component.text("Mağaza Modu: ", NamedTextColor.GRAY) // ShopType yerine ShopMode
                    .append(Component.text(pendingShop.getShopMode().toString(), NamedTextColor.WHITE)));
            lore.add(Component.text("Paket Miktarı: ", NamedTextColor.GRAY)
                    .append(Component.text(bundleAmount + " adet", NamedTextColor.WHITE)));

            String currencyName = shopManager.getCurrencySymbol().trim(); // ShopManager üzerinden para birimi sembolü/adı
            if (currencyName.isEmpty() || currencyName.equals("$")) currencyName = plugin.getEconomy().currencyNameSingular(); // Daha iyi bir isim için Vault'tan al
            if (currencyName == null || currencyName.isEmpty()) currencyName = "$";


            if (buyPrice >= 0) {
                lore.add(Component.text("Oyuncular Satın Alır: ", NamedTextColor.GREEN)
                        .append(Component.text(String.format("%.2f %s", buyPrice, currencyName), NamedTextColor.WHITE)));
            } else {
                lore.add(Component.text("Oyuncular Satın Alamaz", NamedTextColor.RED));
            }

            if (sellPrice >= 0) {
                lore.add(Component.text("Oyuncular Satar: ", NamedTextColor.AQUA)
                        .append(Component.text(String.format("%.2f %s", sellPrice, currencyName), NamedTextColor.WHITE)));
            } else {
                lore.add(Component.text("Oyuncular Satamaz", NamedTextColor.RED));
            }
            meta.lore(lore);
            templateItem.setItemMeta(meta);
        }
        gui.setItem(13, templateItem);

        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text("Onayla ve Mağazayı Kur", NamedTextColor.GREEN, TextDecoration.BOLD));
            confirmItem.setItemMeta(confirmMeta);
        }
        gui.setItem(11, confirmItem);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(Component.text("İptal Et", NamedTextColor.RED, TextDecoration.BOLD));
            cancelItem.setItemMeta(cancelMeta);
        }
        gui.setItem(15, cancelItem);

        player.openInventory(gui);
    }
}