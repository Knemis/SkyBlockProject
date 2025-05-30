package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import com.knemis.skyblock.skyblockcoreproject.shop.ShopManager;
import com.knemis.skyblock.skyblockcoreproject.shop.setup.ShopSetupSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopSetupGUIManager {

    private final SkyBlockProject plugin;
    private final ShopManager shopManager;
    private final Map<UUID, ShopSetupSession> playerSessions = new HashMap<>();

    // GUI Titles (using Components for modern support)
    public static final Component SHOP_TYPE_TITLE = Component.text("Mağaza Türünü Seç", Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD));
    public static final Component ITEM_SELECT_TITLE = Component.text("Mağaza İçin Eşya Seç", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD));
    public static final Component QUANTITY_INPUT_TITLE = Component.text("Miktar Belirle", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD));
    public static final Component PRICE_INPUT_TITLE = Component.text("Fiyat Belirle (Sohbete Yaz)", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)); // This title might not be shown if chat is used directly
    public static final Component ANVIL_TITLE_COMPONENT = Component.text("Set Price & Quantity", NamedTextColor.GOLD, TextDecoration.BOLD);
    public static final Component CONFIRMATION_TITLE = Component.text("Mağaza Kurulumunu Onayla", Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));


    public enum InputType {
        PRICE,
        QUANTITY, // If you ever need a separate quantity chat input
        ANVIL_PRICE_QUANTITY // For the new Anvil GUI
        // Other types as needed
    }

    public ShopSetupGUIManager(SkyBlockProject plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
    }

    public ShopSetupSession createSession(Player player, org.bukkit.Location chestLocation, Shop pendingShop, ItemStack initialStockItem) {
        ShopSetupSession session = new ShopSetupSession(player.getUniqueId(), chestLocation, pendingShop, initialStockItem);
        playerSessions.put(player.getUniqueId(), session);
        plugin.getLogger().info("ShopSetupSession created for player " + player.getName() + " for shop at " + Shop.locationToString(chestLocation));
        return session;
    }

    public ShopSetupSession getPlayerSession(UUID playerId) {
        return playerSessions.get(playerId);
    }

    public ShopSetupSession removeSession(UUID playerId) {
        plugin.getLogger().info("ShopSetupSession removed for player " + playerId + ".");
        return playerSessions.remove(playerId);
    }

    public void openShopTypeSelectionMenu(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            session = createSession(player, pendingShop.getLocation(), pendingShop, null);
            plugin.getLogger().warning("ShopSetupGUIManager: New session created in openShopTypeSelectionMenu for " + player.getName() + ". This might be unexpected if a session was assumed.");
        }

        Inventory gui = Bukkit.createInventory(player, 27, SHOP_TYPE_TITLE);

        ItemStack buyShopItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta buyShopMeta = buyShopItem.getItemMeta();
        buyShopMeta.displayName(Component.text("Satın Alma Dükkanı", NamedTextColor.GREEN));
        List<Component> buyLore = new ArrayList<>();
        buyLore.add(Component.text("Oyuncular bu dükkandan eşya satın alır.", NamedTextColor.GRAY));
        buyLore.add(Component.text("Sen eşya satarsın.", NamedTextColor.GRAY));
        buyShopMeta.lore(buyLore);
        buyShopItem.setItemMeta(buyShopMeta);
        gui.setItem(11, buyShopItem);

        ItemStack sellShopItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta sellShopMeta = sellShopItem.getItemMeta();
        sellShopMeta.displayName(Component.text("Satış Yapma Dükkanı", NamedTextColor.YELLOW));
        List<Component> sellLore = new ArrayList<>();
        sellLore.add(Component.text("Oyuncular bu dükkana eşya satar.", NamedTextColor.GRAY));
        sellLore.add(Component.text("Sen eşya alırsın.", NamedTextColor.GRAY));
        sellShopMeta.lore(sellLore);
        sellShopItem.setItemMeta(sellShopMeta);
        gui.setItem(13, sellShopItem);

        ItemStack buySellShopItem = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta buySellShopMeta = buySellShopItem.getItemMeta();
        buySellShopMeta.displayName(Component.text("Alış & Satış Dükkanı", NamedTextColor.AQUA));
        List<Component> buySellLore = new ArrayList<>();
        buySellLore.add(Component.text("Oyuncular hem eşya alabilir hem de satabilir.", NamedTextColor.GRAY));
        buySellShopMeta.lore(buySellLore);
        buySellShopItem.setItemMeta(buySellShopMeta);
        gui.setItem(15, buySellShopItem);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        session.setCurrentGuiTitle(LegacyComponentSerializer.legacySection().serialize(SHOP_TYPE_TITLE));
        session.setExpectedInputType(null);
        player.openInventory(gui);
    }


    public void openItemSelectionMenu(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Error: Shop setup session not found. Please start over.");
            plugin.getLogger().severe("ShopSetupGUIManager.openItemSelectionMenu: No session for " + player.getName());
            return;
        }
        Inventory gui = Bukkit.createInventory(player, 27, ITEM_SELECT_TITLE);

        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.displayName(Component.text("Eşyayı Buraya Koy", NamedTextColor.YELLOW, TextDecoration.ITALIC));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Dükkanında satmak/almak istediğin eşyayı", NamedTextColor.GRAY));
        lore.add(Component.text("bu slota yerleştir.", NamedTextColor.GRAY));
        meta.lore(lore);
        placeholder.setItemMeta(meta);
        gui.setItem(13, placeholder);

        session.setCurrentGuiTitle(LegacyComponentSerializer.legacySection().serialize(ITEM_SELECT_TITLE));
        session.setExpectedInputType(null);
        player.openInventory(gui);
    }

    public void openPriceQuantityAnvilGUI(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null || pendingShop == null) {
            player.sendMessage(ChatColor.RED + "Shop setup session or pending shop not found. Please restart setup.");
            plugin.getLogger().severe("openPriceQuantityAnvilGUI: Session or pendingShop is null for " + player.getName());
            if (player.getOpenInventory().getTopInventory() != player.getInventory()) player.closeInventory();
            return;
        }

        ItemStack templateItem = pendingShop.getTemplateItemStack();
        if (templateItem == null || templateItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "No item selected for the shop. Please select an item first.");
            plugin.getLogger().warning("openPriceQuantityAnvilGUI: templateItem is null/AIR for " + player.getName() + ". Reverting to item selection.");
            openItemSelectionMenu(player, pendingShop);
            return;
        }

        ItemStack anvilPromptItem = templateItem.clone();
        anvilPromptItem.setAmount(1);

        Inventory anvilGui = Bukkit.createInventory(player, InventoryType.ANVIL, LegacyComponentSerializer.legacySection().serialize(ANVIL_TITLE_COMPONENT));
        anvilGui.setItem(0, anvilPromptItem);

        session.setCurrentGuiTitle(LegacyComponentSerializer.legacySection().serialize(ANVIL_TITLE_COMPONENT));
        session.setExpectedInputType(InputType.ANVIL_PRICE_QUANTITY);

        player.openInventory(anvilGui);
    }

    public void openPriceInputPrompt(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Error: Shop setup session not found. Please start over.");
            return;
        }

        ItemStack itemForShop = pendingShop.getTemplateItemStack();
        int quantity = pendingShop.getBundleAmount();

        if (itemForShop == null || itemForShop.getType() == Material.AIR || quantity <= 0) {
            player.sendMessage(ChatColor.RED + "Kurulum hatası: Eşya veya miktar geçerli değil.");
            shopManager.cancelShopSetup(player.getUniqueId());
            return;
        }

        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "Mağaza için fiyat belirle.");
        player.sendMessage(ChatColor.GRAY + "Satılacak eşya: " + ChatColor.AQUA + quantity + "x " + itemForShop.getType());

        if (session.isIntentToAllowPlayerBuy() && session.isIntentToAllowPlayerSell()) {
            player.sendMessage(ChatColor.YELLOW + "Hem alış hem de satış fiyatı için sohbete şunu yazın:");
            player.sendMessage(ChatColor.GREEN + "ALIS_FIYATI:SATIS_FIYATI " + ChatColor.GRAY + "(Örn: 100:80)");
            player.sendMessage(ChatColor.GRAY + "Birini devre dışı bırakmak için -1 kullanın (örn: -1:80 veya 100:-1).");
        } else if (session.isIntentToAllowPlayerBuy()) {
            player.sendMessage(ChatColor.YELLOW + "Oyuncuların bu eşyayı senden alacağı fiyatı sohbete yaz:");
            player.sendMessage(ChatColor.GREEN + "FIYAT " + ChatColor.GRAY + "(Örn: 100)");
        } else if (session.isIntentToAllowPlayerSell()) {
            player.sendMessage(ChatColor.YELLOW + "Oyuncuların bu eşyayı sana satacağı fiyatı sohbete yaz:");
            player.sendMessage(ChatColor.GREEN + "FIYAT " + ChatColor.GRAY + "(Örn: 80)");
        }

        player.sendMessage(ChatColor.RED + "'iptal' " + ChatColor.GRAY + "yazarak kurulumu sonlandırın.");
        player.sendMessage(ChatColor.GOLD + "------------------------------------------");

        session.setExpectedInputType(InputType.PRICE);
        session.setCurrentGuiTitle(LegacyComponentSerializer.legacySection().serialize(PRICE_INPUT_TITLE));
    }

    public void openConfirmationMenu(Player player, Shop pendingShop, double buyPrice, double sellPrice) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Error: Shop setup session not found. Please start over.");
            return;
        }

        Inventory gui = Bukkit.createInventory(player, 27, CONFIRMATION_TITLE);
        ItemStack itemForShop = pendingShop.getTemplateItemStack();
        int quantity = pendingShop.getBundleAmount();

        ItemStack infoItem = itemForShop.clone();
        infoItem.setAmount(quantity);
        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Onay Detayları", NamedTextColor.YELLOW));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Eşya: ", NamedTextColor.GRAY).append(Component.text(quantity + "x " + itemForShop.getType().toString().toLowerCase().replace("_"," "), NamedTextColor.WHITE)));

            if (session.isIntentToAllowPlayerBuy()) {
                lore.add(Component.text("Oyuncu Alış Fiyatı: ", NamedTextColor.GRAY)
                        .append(buyPrice >= 0 ? Component.text(String.format("%.2f", buyPrice), NamedTextColor.GREEN) : Component.text("Devre Dışı", NamedTextColor.RED)));
            }
            if (session.isIntentToAllowPlayerSell()) {
                lore.add(Component.text("Oyuncu Satış Fiyatı: ", NamedTextColor.GRAY)
                        .append(sellPrice >= 0 ? Component.text(String.format("%.2f", sellPrice), NamedTextColor.GREEN) : Component.text("Devre Dışı", NamedTextColor.RED)));
            }
            meta.lore(lore);
            infoItem.setItemMeta(meta);
        }
        gui.setItem(13, infoItem);

        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("Onayla & Dükkanı Kur", NamedTextColor.GREEN, TextDecoration.BOLD));
        confirmButton.setItemMeta(confirmMeta);
        gui.setItem(11, confirmButton);

        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("İptal Et", NamedTextColor.RED, TextDecoration.BOLD));
        cancelButton.setItemMeta(cancelMeta);
        gui.setItem(15, cancelButton);

        session.setCurrentGuiTitle(LegacyComponentSerializer.legacySection().serialize(CONFIRMATION_TITLE));
        session.setExpectedInputType(null);
        player.openInventory(gui);
    }

    public void openQuantityInputMenu(Player player, Shop pendingShop) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null || pendingShop.getTemplateItemStack() == null) {
            player.sendMessage(ChatColor.RED + "Error: Shop setup session or template item not found.");
            if(session != null) shopManager.cancelShopSetup(player.getUniqueId());
            return;
        }

        Inventory gui = Bukkit.createInventory(player, 45, QUANTITY_INPUT_TITLE);
        ItemStack templateItem = pendingShop.getTemplateItemStack();

        ItemStack instructionItem = new ItemStack(Material.BOOK);
        ItemMeta instructionMeta = instructionItem.getItemMeta();
        instructionMeta.displayName(Component.text("Miktar Belirleme", NamedTextColor.YELLOW));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Aşağıdaki slota, dükkanında tek seferde", NamedTextColor.GRAY));
        lore.add(Component.text("satılacak/alınacak miktarda eşya koy.", NamedTextColor.GRAY));
        lore.add(Component.text("Örn: 16 Taş = Her işlem 16 Taş üzerinden fiyatlandırılır.", NamedTextColor.AQUA));
        lore.add(Component.text("Sonra aşağıdaki ONAYLA butonuna tıkla.", NamedTextColor.GRAY));
        instructionMeta.lore(lore);
        instructionItem.setItemMeta(instructionMeta);
        gui.setItem(4, instructionItem);

        ItemStack quantitySlotItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta quantitySlotMeta = quantitySlotItem.getItemMeta();
        quantitySlotMeta.displayName(Component.text("Eşyaları Buraya Koy (" + templateItem.getType().toString().toLowerCase() + ")", NamedTextColor.YELLOW));
        quantitySlotItem.setItemMeta(quantitySlotMeta);
        gui.setItem(22, quantitySlotItem);

        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("Miktarı Onayla", NamedTextColor.GREEN));
        confirmButton.setItemMeta(confirmMeta);
        gui.setItem(31, confirmButton);

        session.setCurrentGuiTitle(LegacyComponentSerializer.legacySection().serialize(QUANTITY_INPUT_TITLE));
        session.setExpectedInputType(null);
        player.openInventory(gui);
    }
}