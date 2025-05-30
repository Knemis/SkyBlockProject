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
import org.bukkit.event.inventory.InventoryType; // Added for Anvil
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

    // GUI Titles
    public static final Component SHOP_TYPE_TITLE = Component.text("Mağaza Türünü Seç", Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD));
    public static final Component ITEM_SELECT_TITLE = Component.text("Mağaza İçin Eşya Seç", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD));
    public static final Component ANVIL_TITLE_COMPONENT = Component.text("Set Price & Quantity", NamedTextColor.GOLD, TextDecoration.BOLD); // New Anvil Title
    public static final Component CONFIRMATION_TITLE = Component.text("Mağaza Kurulumunu Onayla", Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));

    // Potentially obsolete titles if chat input for price/quantity is fully replaced by Anvil
    public static final Component QUANTITY_INPUT_TITLE = Component.text("Miktar Belirle (OLD)", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD));
    public static final Component PRICE_INPUT_TITLE = Component.text("Fiyat Belirle (Sohbet - OLD)", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD));


    public enum InputType {
        PRICE, // For chat-based price input (may be obsolete)
        // QUANTITY, // Separate quantity chat input (likely obsolete)
        ANVIL_PRICE_QUANTITY // For the new Anvil GUI
        // CONFIRMATION // If needed to distinguish confirmation GUI state
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
        }

        Inventory gui = Bukkit.createInventory(player, 27, SHOP_TYPE_TITLE);

        ItemStack ownerSellsItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta ownerSellsMeta = ownerSellsItem.getItemMeta();
        ownerSellsMeta.displayName(Component.text("Dükkan Satış Yapar", NamedTextColor.GREEN));
        List<Component> ownerSellsLore = new ArrayList<>();
        ownerSellsLore.add(Component.text("Oyuncular bu dükkandan eşya alır.", NamedTextColor.GRAY));
        ownerSellsMeta.lore(ownerSellsLore);
        ownerSellsItem.setItemMeta(ownerSellsMeta);
        gui.setItem(11, ownerSellsItem);

        ItemStack ownerBuysItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta ownerBuysMeta = ownerBuysItem.getItemMeta();
        ownerBuysMeta.displayName(Component.text("Dükkan Alım Yapar", NamedTextColor.YELLOW));
        List<Component> ownerBuysLore = new ArrayList<>();
        ownerBuysLore.add(Component.text("Oyuncular bu dükkana eşya satar.", NamedTextColor.GRAY));
        ownerBuysMeta.lore(ownerBuysLore);
        ownerBuysItem.setItemMeta(ownerBuysMeta);
        gui.setItem(13, ownerBuysItem);

        ItemStack dualFunctionItem = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta dualFunctionMeta = dualFunctionItem.getItemMeta();
        dualFunctionMeta.displayName(Component.text("Dükkan Hem Alır Hem Satar", NamedTextColor.AQUA));
        List<Component> dualFunctionLore = new ArrayList<>();
        dualFunctionLore.add(Component.text("Oyuncular hem alabilir hem satabilir.", NamedTextColor.GRAY));
        dualFunctionMeta.lore(dualFunctionLore);
        dualFunctionItem.setItemMeta(dualFunctionMeta);
        gui.setItem(15, dualFunctionItem);

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
        lore.add(Component.text("Dükkanında işlem görecek eşyayı", NamedTextColor.GRAY));
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
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW+"Price input via chat is being phased out. Please use the Anvil GUI.");
        shopManager.cancelShopSetup(player.getUniqueId());
    }

    public void openConfirmationMenu(Player player, Shop pendingShop, double buyPrice, double sellPrice) {
        ShopSetupSession session = getPlayerSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Error: Shop setup session not found. Please start over.");
            plugin.getLogger().severe("ShopSetupGUIManager.openConfirmationMenu: No session for " + player.getName());
            return;
        }
        if (pendingShop == null || pendingShop.getTemplateItemStack() == null || pendingShop.getBundleAmount() <=0) {
            player.sendMessage(ChatColor.RED + "Error: Invalid shop data for confirmation. Please restart setup.");
            plugin.getLogger().severe("ShopSetupGUIManager.openConfirmationMenu: Invalid pendingShop data for " + player.getName());
            shopManager.cancelShopSetup(player.getUniqueId());
            return;
        }

        Inventory gui = Bukkit.createInventory(player, 27, CONFIRMATION_TITLE);
        ItemStack itemForShop = pendingShop.getTemplateItemStack();
        int quantity = pendingShop.getBundleAmount();

        ItemStack infoItem = itemForShop.clone();
        infoItem.setAmount(quantity);
        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Shop Details", NamedTextColor.YELLOW, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            String itemName = itemForShop.getType().toString().replace("_", " ").toLowerCase();
            itemName = Character.toUpperCase(itemName.charAt(0)) + itemName.substring(1);
            lore.add(Component.text("Item: ", NamedTextColor.GRAY).append(Component.text(quantity + "x " + itemName, NamedTextColor.WHITE)));

            if (session.isIntentToAllowPlayerBuy()) {
                lore.add(Component.text("Players Buy For: ", NamedTextColor.GRAY)
                        .append(buyPrice >= 0 ? Component.text(String.format("%.2f", buyPrice), NamedTextColor.GREEN) : Component.text("Disabled", NamedTextColor.RED)));
            }
            if (session.isIntentToAllowPlayerSell()) {
                lore.add(Component.text("Players Sell For: ", NamedTextColor.GRAY)
                        .append(sellPrice >= 0 ? Component.text(String.format("%.2f", sellPrice), NamedTextColor.GREEN) : Component.text("Disabled", NamedTextColor.RED)));
            }
            meta.lore(lore);
            infoItem.setItemMeta(meta);
        }
        gui.setItem(13, infoItem);

        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("Confirm & Create Shop", NamedTextColor.GREEN, TextDecoration.BOLD));
        confirmButton.setItemMeta(confirmMeta);
        gui.setItem(11, confirmButton);

        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel Setup", NamedTextColor.RED, TextDecoration.BOLD));
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
        player.sendMessage(ChatColor.YELLOW + "Quantity is now set in the Anvil GUI with the price. Restarting item selection.");
        openItemSelectionMenu(player, pendingShop);
    }
}