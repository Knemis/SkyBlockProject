package com.knemis.skyblock.skyblockcoreproject.shop.admin;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey; // Added import
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdminShopGUIManager {

    private final SkyBlockProject plugin;
    private AdminShopListener listener; // Reference to the listener, set via setListener()
    private FileConfiguration adminShopConfig;
    private File adminShopFile;
    private NamespacedKey itemKeyPDC; // Added field

    private String mainShopTitle = "&2Admin Shop"; // Default
    private int categoryRows = 3;
    private int itemRows = 3;
    private boolean fillEmptySlots = true;
    private Material fillerPaneMaterial = Material.BLACK_STAINED_GLASS_PANE;

    private final List<ShopCategory> categories = new ArrayList<>();
    private final Map<String, String> messages = new HashMap<>();

    private String currencySymbol = "$"; // Default currency symbol

    /**
     * Manages the Admin Shop GUIs, configuration, and interactions.
     * @param plugin The main SkyBlockProject plugin instance.
     */
    public AdminShopGUIManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.itemKeyPDC = new NamespacedKey(plugin, "admin_shop_item_internal_name"); // Initialize in constructor
        loadConfiguration(); // Load config immediately
    }

    /**
     * Sets the listener for this GUI manager. Used to inform the listener about GUI events.
     * @param listener The AdminShopListener instance.
     */
    public void setListener(AdminShopListener listener) {
        this.listener = listener;
    }

    /**
     * Represents a purchasable/sellable item within a shop category.
     */
    public static class ShopItem {
        String internalName;
        Material material;
        String displayName;
        String nbtData;
        double buyPrice;
        double sellPrice;
        int guiSlot;
        String permissionBuy;
        String permissionSell;
        ShopCategory parentCategory;
        ItemStack sourceItem;

        /**
         * Constructs a new ShopItem.
         * @param internalName Internal identifier for the item.
         * @param material The Bukkit Material for this item.
         * @param displayName The display name (already color-translated).
         * @param nbtData Raw NBT string (primarily for enchanted books at this stage).
         * @param buyPrice The price at which players can buy this item (-1 if not buyable).
         * @param sellPrice The price at which players can sell this item (-1 if not sellable).
         * @param guiSlot Preferred GUI slot (-1 for auto-placement).
         * @param permissionBuy Optional permission required to buy.
         * @param permissionSell Optional permission required to sell.
         * @param parentCategory The category this item belongs to.
         * @param sourceItem A pre-configured ItemStack representing this item (used for cloning).
         */
        public ShopItem(String internalName, Material material, String displayName, String nbtData,
                        double buyPrice, double sellPrice, int guiSlot, String permissionBuy,
                        String permissionSell, ShopCategory parentCategory, ItemStack sourceItem) {
            this.internalName = internalName;
            this.material = material;
            this.displayName = displayName;
            this.nbtData = nbtData;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.guiSlot = guiSlot;
            this.permissionBuy = permissionBuy;
            this.permissionSell = permissionSell;
            this.parentCategory = parentCategory;
            this.sourceItem = sourceItem;
        }

        // Getters
        public String getInternalName() { return internalName; }
        public Material getMaterial() { return material; }
        public String getDisplayName() { return displayName; }
        public String getNbtData() { return nbtData; }
        public double getBuyPrice() { return buyPrice; }
        public double getSellPrice() { return sellPrice; }
        public int getGuiSlot() { return guiSlot; }
        public String getPermissionBuy() { return permissionBuy; }
        public String getPermissionSell() { return permissionSell; }
        public ShopCategory getParentCategory() { return parentCategory; }
        public ItemStack getSourceItem() { return sourceItem; }
    }

    /**
     * Represents a category of items in the admin shop.
     */
    public static class ShopCategory {
        String internalName;
        String displayName;
        Material guiMaterial;
        List<ShopItem> items = new ArrayList<>();
        Map<Integer, ShopItem> itemsBySlot = new HashMap<>();
        ItemStack categoryIcon;

        /**
         * Constructs a new ShopCategory.
         * @param internalName Internal identifier for the category.
         * @param displayName Display name for the category (already color-translated).
         * @param guiMaterial Material for the category's icon in the main shop GUI.
         */
        public ShopCategory(String internalName, String displayName, Material guiMaterial) {
            this.internalName = internalName;
            this.displayName = displayName;
            this.guiMaterial = guiMaterial;
        }

        /**
         * Adds a ShopItem to this category.
         * @param item The ShopItem to add.
         */
        public void addItem(ShopItem item) {
            this.items.add(item);
            if (item.getGuiSlot() != -1 && item.getGuiSlot() < 54) {
                this.itemsBySlot.put(item.getGuiSlot(), item);
            }
        }

        public void setCategoryIcon(ItemStack icon) { this.categoryIcon = icon; }
        public ItemStack getCategoryIcon() { return this.categoryIcon; }

        // Getters
        public String getInternalName() { return internalName; }
        public String getDisplayName() { return displayName; }
        public Material getGuiMaterial() { return guiMaterial; }
        public List<ShopItem> getItems() { return items; }
        public Map<Integer, ShopItem> getItemsBySlot() { return itemsBySlot; }
    }

    /**
     * Loads the admin shop configuration from adminshop.yml.
     * This includes global settings, messages, categories, and items.
     */
    public void loadConfiguration() {
        adminShopFile = new File(plugin.getDataFolder(), "adminshop.yml");
        if (!adminShopFile.exists()) {
            plugin.saveResource("adminshop.yml", false);
            plugin.getLogger().info("adminshop.yml created successfully.");
        }

        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);
        InputStream defaultConfigStream = plugin.getResource("adminshop.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            adminShopConfig.setDefaults(defaultConfig);
        }

        // Load global settings
        ConfigurationSection settingsSection = adminShopConfig.getConfigurationSection("admin_shop_settings");
        if (settingsSection == null) {
            plugin.getLogger().severe("AdminShop Error: 'admin_shop_settings' section is missing in adminshop.yml! Using defaults.");
            settingsSection = adminShopConfig.createSection("admin_shop_settings");
        }

        mainShopTitle = ChatUtils.translateAlternateColorCodes(settingsSection.getString("title", "&2Admin Shop"));
        categoryRows = Math.max(1, Math.min(6, settingsSection.getInt("category_rows", 3)));
        itemRows = Math.max(1, Math.min(6, settingsSection.getInt("item_rows", 3)));
        fillEmptySlots = settingsSection.getBoolean("fill_empty_slots_with_pane", true);

        String paneMaterialName = settingsSection.getString("pane_material", "BLACK_STAINED_GLASS_PANE");
        try {
            fillerPaneMaterial = Material.valueOf(paneMaterialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("AdminShop Config: Invalid pane_material '" + paneMaterialName + "'. Defaulting to BLACK_STAINED_GLASS_PANE.");
            fillerPaneMaterial = Material.BLACK_STAINED_GLASS_PANE;
        }

        // Load currency symbol from main plugin config, similar to ShopManager if it exists, or default.
        // For simplicity, we'll use a config path within adminshop.yml or a hardcoded default.
        // A more robust approach might involve a shared config or EconomyManager.getCurrencySymbol().
        currencySymbol = ChatUtils.translateAlternateColorCodes(settingsSection.getString("currency_symbol", "$"));


        messages.clear();
        ConfigurationSection messagesSection = settingsSection.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, ChatUtils.translateAlternateColorCodes(messagesSection.getString(key)));
            }
        } else {
            plugin.getLogger().warning("AdminShop Config: 'admin_shop_settings.messages' section is missing. Using default messages.");
        }
        // Ensure essential messages have defaults
        messages.putIfAbsent("no_permission_buy", "&cYou do not have permission to buy this item.");
        messages.putIfAbsent("no_permission_sell", "&cYou do not have permission to sell this item.");
        messages.putIfAbsent("item_not_buyable", "&cThis item cannot be bought.");
        messages.putIfAbsent("item_not_sellable", "&cThis item cannot be sold.");
        messages.putIfAbsent("insufficient_funds", "&cYou do not have enough {currency}{price} to buy this.");
        messages.putIfAbsent("inventory_full", "&cYour inventory is full.");
        messages.putIfAbsent("buy_success", "&aBought {amount}x {item_name} &afor {currency}{price}.");
        messages.putIfAbsent("sell_success", "&aSold {amount}x {item_name} &afor {currency}{price}.");
        messages.putIfAbsent("insufficient_items_to_sell", "&cYou don't have {amount}x {item_name} &cto sell.");
        messages.putIfAbsent("buy_failed_transaction", "&cPurchase failed due to a transaction error.");
        messages.putIfAbsent("sell_failed_transaction", "&cSale failed due to a transaction error. Your items were not taken (or returned if possible).");
        messages.putIfAbsent("sell_failed_item_return_inventory_full", "&cSale failed. Could not return your items as inventory is full. They were dropped.");
        messages.putIfAbsent("sell_error_removing_items", "&cCould not remove items from your inventory for selling.");
        messages.putIfAbsent("invalid_page", "&cInvalid page number.");
        messages.putIfAbsent("shop_is_empty", "&cThis shop category is currently empty.");
        messages.putIfAbsent("default_error", "&cAn unexpected error occurred. Please contact an admin.");
        messages.putIfAbsent("buy_price_format", "&aBuy: &f{price}{currency}"); // Example for lore
        messages.putIfAbsent("sell_price_format", "&cSell: &f{price}{currency}"); // Example for lore
        messages.putIfAbsent("buy_no_permission_format", "&aBuy: &m{price}{currency}&r &c(No Perm)");
        messages.putIfAbsent("sell_no_permission_format", "&cSell: &m{price}{currency}&r &c(No Perm)");


        categories.clear();
        ConfigurationSection categoriesSection = adminShopConfig.getConfigurationSection("shop_categories");
        if (categoriesSection != null) {
            for (String categoryKey : categoriesSection.getKeys(false)) {
                ConfigurationSection catConfig = categoriesSection.getConfigurationSection(categoryKey);
                if (catConfig == null) {
                    plugin.getLogger().warning("AdminShop Error: Category '" + categoryKey + "' has invalid configuration. Skipping.");
                    continue;
                }

                String catDisplayName = ChatUtils.translateAlternateColorCodes(catConfig.getString("display_name", categoryKey));
                String catMaterialName = catConfig.getString("gui_material", "GRASS_BLOCK");
                Material catMaterial;
                try {
                    catMaterial = Material.valueOf(catMaterialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("AdminShop Error: Invalid gui_material '" + catMaterialName + "' for category '" + categoryKey + "'. Defaulting to GRASS_BLOCK.");
                    catMaterial = Material.GRASS_BLOCK;
                }

                ShopCategory category = new ShopCategory(categoryKey, catDisplayName, catMaterial);

                ConfigurationSection itemsSection = catConfig.getConfigurationSection("items");
                if (itemsSection != null) {
                    for (String itemKey : itemsSection.getKeys(false)) {
                        ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                        if (itemConfig == null) {
                            plugin.getLogger().warning("AdminShop Error: Item '" + itemKey + "' in category '" + categoryKey + "' has invalid config. Skipping.");
                            continue;
                        }

                        String itemMaterialName = itemConfig.getString("material");
                        if (itemMaterialName == null) {
                            plugin.getLogger().warning("AdminShop Error: 'material' missing for item '" + itemKey + "' in category '" + categoryKey + "'. Skipping item.");
                            continue;
                        }
                        Material itemMaterial;
                        try {
                            itemMaterial = Material.valueOf(itemMaterialName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("AdminShop Error: Invalid material '" + itemMaterialName + "' for item '" + itemKey + "'. Skipping item.");
                            continue;
                        }

                        String itemDisplayName = itemConfig.getString("display_name");
                        boolean hasCustomDisplayName = itemDisplayName != null;
                        if (hasCustomDisplayName) {
                            itemDisplayName = ChatUtils.translateAlternateColorCodes(itemDisplayName);
                        } else {
                            itemDisplayName = ChatColor.RESET + capitalizeFully(itemMaterial.name().replace("_", " "));
                        }

                        String nbt = itemConfig.getString("nbt");
                        double buyPrice = itemConfig.getDouble("buy", -1.0);
                        double sellPrice = itemConfig.getDouble("sell", -1.0);
                        int guiSlot = itemConfig.getInt("gui_slot", -1);
                        String permBuy = itemConfig.getString("permission_buy");
                        String permSell = itemConfig.getString("permission_sell");

                        // Create a representative ItemStack for the ShopItem (used for NBT and other meta)
                        ItemStack sourceStack = new ItemStack(itemMaterial);
                        ItemMeta sourceMeta = sourceStack.getItemMeta();
                        if (sourceMeta != null) {
                            if (hasCustomDisplayName) { // Only set if custom, otherwise it's default
                                sourceMeta.setDisplayName(itemDisplayName);
                            }
                            // Apply NBT to this sourceStack if possible (simplified for enchanted books)
                            if (itemMaterial == Material.ENCHANTED_BOOK && nbt != null && !nbt.isEmpty()) {
                                if (sourceMeta instanceof EnchantmentStorageMeta) {
                                    EnchantmentStorageMeta esm = (EnchantmentStorageMeta) sourceMeta;
                                    Pattern p = Pattern.compile("\\{id:\"(?:minecraft:)?(\\w+)\",lvl:(\\d+)s?}");
                                    Matcher m = p.matcher(nbt);
                                    while (m.find()) {
                                        try {
                                            String enchantId = m.group(1);
                                            int level = Integer.parseInt(m.group(2));
                                            Enchantment ench = Enchantment.getByName(enchantId.toUpperCase());
                                             if (ench == null) ench = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchantId));
                                            if (ench != null) esm.addStoredEnchant(ench, level, true);
                                            else plugin.getLogger().warning("AdminShop: Unknown enchantment ID '" + enchantId + "' in NBT for item " + itemKey);
                                        } catch (Exception e) { /* log error */ }
                                    }
                                }
                            }
                            sourceStack.setItemMeta(sourceMeta);
                        }

                        ShopItem shopItem = new ShopItem(itemKey, itemMaterial, itemDisplayName, nbt, buyPrice, sellPrice, guiSlot, permBuy, permSell, category, sourceStack);
                        category.addItem(shopItem);
                    }
                }
                categories.add(category);
            }
        } else {
            plugin.getLogger().warning("AdminShop Error: 'shop_categories' section is missing in adminshop.yml. No items or categories loaded.");
        }
        plugin.getLogger().info("AdminShop: Loaded " + categories.size() + " categories and " + categories.stream().mapToInt(c -> c.items.size()).sum() + " items.");
    }

    private String capitalizeFully(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Pattern.compile("\\b(.)(.*?)\\b")
                      .matcher(str.toLowerCase())
                      .replaceAll(m -> m.group(1).toUpperCase() + m.group(2));
    }

    /**
     * Retrieves a formatted message from the configuration.
     * @param key The key of the message in the 'messages' section of the config.
     * @param placeholders A map of placeholders to replace in the message (e.g., "{player_name}" -> "Steve"). Can be null.
     * @return The formatted message, or a default error message if the key is not found.
     */
    public String getMessage(String key, @Nullable Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, messages.getOrDefault("default_error", "&cMessage key '" + key + "' not found."));
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                // Ensure placeholder keys are in the format {key} in the config string
                message = message.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "null" : entry.getValue());
            }
        }
        return ChatUtils.translateAlternateColorCodes(message); // Ensure color codes are always translated
    }

    /**
     * Opens the main admin shop GUI for the player, displaying categories.
     * @param player The player to open the GUI for.
     */
    public void openMainShopGUI(Player player) {
        int numCategories = categories.size();
        int guiSize = Math.max(9, (int) Math.ceil(numCategories / 9.0) * 9); // Ensure at least 1 row
        guiSize = Math.min(guiSize, categoryRows * 9); // Cap by configured rows
        guiSize = Math.min(guiSize, 54); // Absolute cap

        Inventory gui = Bukkit.createInventory(player, guiSize, mainShopTitle);

        for (int i = 0; i < numCategories; i++) {
            if (i >= guiSize) break;
            ShopCategory category = categories.get(i);

            ItemStack categoryIcon = new ItemStack(category.getGuiMaterial());
            ItemMeta meta = categoryIcon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(category.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatUtils.translateAlternateColorCodes("&7Click to view items in this category."));
                meta.setLore(lore);
                categoryIcon.setItemMeta(meta);
            }
            category.setCategoryIcon(categoryIcon); // Store the icon
            gui.setItem(i, categoryIcon);
        }

        if (fillEmptySlots) {
            ItemStack filler = new ItemStack(fillerPaneMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < guiSize; i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, filler);
                }
            }
        }
        player.openInventory(gui);
    }

    public void openCategoryGUI(Player player, ShopCategory category, int page) {
        int itemsPerPage = Math.max(1, itemRows * 9 - 9); // Max items, leave 1 row for navigation
        if (itemRows == 1 && category.items.size() > 9) itemsPerPage = 9; // Full row if only 1 row total
        else if (itemRows == 1) itemsPerPage = itemRows * 9;


        int totalItems = category.getItems().size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1; // At least one page, even if empty

        if (page < 1 || page > totalPages) {
            player.sendMessage(getMessage("invalid_page", null));
            // Optionally, open page 1 or close inventory
            // openCategoryGUI(player, category, 1);
            return;
        }

        int guiSize = itemRows * 9;
        guiSize = Math.min(guiSize, 54); // Cap at 54

        Inventory gui = Bukkit.createInventory(player, guiSize, category.getDisplayName());

        List<ShopItem> pageItems = category.getItems().stream()
                                           .skip((long)(page - 1) * itemsPerPage)
                                           .limit(itemsPerPage)
                                           .collect(Collectors.toList());

        if (totalItems == 0) {
            // Optional: Display a "Shop is Empty" item
            ItemStack emptyShopItem = new ItemStack(Material.BARRIER);
            ItemMeta emptyMeta = emptyShopItem.getItemMeta();
            if (emptyMeta != null) {
                emptyMeta.setDisplayName(getMessage("shop_is_empty", null));
                emptyShopItem.setItemMeta(emptyMeta);
            }
            gui.setItem(guiSize / 2, emptyShopItem); // Place in the middle
        } else {
            int currentSlot = 0;
            for (ShopItem shopItem : pageItems) {
                 if (currentSlot >= itemsPerPage) break; // Should not happen with limit() but good practice
                ItemStack displayStack = createDisplayItem(shopItem, player);
                if (shopItem.getGuiSlot() != -1 && shopItem.getGuiSlot() < itemsPerPage && gui.getItem(shopItem.getGuiSlot()) == null) {
                     // Ensure slot is within the item display area (not nav row)
                    gui.setItem(shopItem.getGuiSlot(), displayStack);
                } else {
                    // Auto-place if slot is -1, taken, or out of bounds for items
                    while(currentSlot < itemsPerPage && gui.getItem(currentSlot) != null) {
                        currentSlot++;
                    }
                    if (currentSlot < itemsPerPage) {
                        gui.setItem(currentSlot, displayStack);
                    }
                }
                currentSlot++; // Increment even if placed by gui_slot to ensure next auto-placement is correct
            }
        }


        // Navigation Items (bottom row: guiSize - 9 to guiSize - 1)
        int navRowStartSlot = guiSize - 9;

        // Back Button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&e&lBack to Categories"));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(navRowStartSlot, backButton); // Bottom-left

        // Page Info / Prev / Next
        if (totalPages > 1) {
            ItemStack prevButton = new ItemStack(Material.PAPER); // Or ARROW
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (page > 1) {
                if (prevMeta != null) prevMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&a&lPrevious Page"));
            } else {
                if (prevMeta != null) prevMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&7Previous Page"));
            }
            if (prevMeta != null) prevButton.setItemMeta(prevMeta);
            gui.setItem(navRowStartSlot + 3, prevButton);

            ItemStack pageInfo = new ItemStack(Material.BOOK);
            ItemMeta pageMeta = pageInfo.getItemMeta();
            if (pageMeta != null) {
                pageMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&fPage " + page + "/" + totalPages));
                pageInfo.setItemMeta(pageMeta);
            }
            gui.setItem(navRowStartSlot + 4, pageInfo);

            ItemStack nextButton = new ItemStack(Material.PAPER); // Or ARROW
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (page < totalPages) {
                if (nextMeta != null) nextMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&a&lNext Page"));
            } else {
                if (nextMeta != null) nextMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&7Next Page"));
            }
             if (nextMeta != null) nextButton.setItemMeta(nextMeta);
            gui.setItem(navRowStartSlot + 5, nextButton);
        }

        if (fillEmptySlots) {
            ItemStack filler = new ItemStack(fillerPaneMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }
            // Fill all slots first, then overwrite with items/nav
            for (int i = 0; i < guiSize; i++) {
                 if (gui.getItem(i) == null) {
                    gui.setItem(i, filler.clone());
                }
            }
        }
        player.openInventory(gui);
        // No need to call listener.playerOpenedCategoryView here, this is the main menu
    }

    /**
     * Opens a specific category's GUI for the player, displaying items on a given page.
     * @param player The player to open the GUI for.
     * @param category The ShopCategory to display.
     * @param page The page number to display (1-indexed).
     */
    public void openCategoryGUI(Player player, ShopCategory category, int page) {
        int itemsPerPage = Math.max(1, itemRows * 9 - 9); // Max items, leave 1 row for navigation
        if (itemRows == 1) { // If only 1 row total, no space for nav, use all slots for items
             itemsPerPage = itemRows * 9;
        }


        int totalItems = category.getItems().size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));

        // Validate page number
        page = Math.max(1, Math.min(page, totalPages));

        int guiSize = itemRows * 9;

        Inventory gui = Bukkit.createInventory(player, guiSize, category.getDisplayName());

        // Get items for the current page
        List<ShopItem> pageItems = category.getItems().stream()
                                           .skip((long)(page - 1) * itemsPerPage)
                                           .limit(itemsPerPage)
                                           .collect(Collectors.toList());

        // Fill items
        if (totalItems == 0) {
            ItemStack emptyShopItem = new ItemStack(Material.BARRIER);
            ItemMeta emptyMeta = emptyShopItem.getItemMeta();
            emptyMeta.setDisplayName(getMessage("shop_is_empty", null));
            emptyShopItem.setItemMeta(emptyMeta);
            gui.setItem(guiSize / 2, emptyShopItem); // Place in the middle-ish
        } else {
            // Attempt to place items with specific gui_slot first, then fill remaining sequentially
            boolean[] slotFilled = new boolean[itemsPerPage]; // Track filled slots in the item display area

            // Place items with defined gui_slot if they are on this page and slot is valid
            for(ShopItem item : pageItems) {
                if(item.getGuiSlot() != -1 && item.getGuiSlot() < itemsPerPage) {
                    gui.setItem(item.getGuiSlot(), createDisplayItem(item, player));
                    slotFilled[item.getGuiSlot()] = true;
                }
            }
            // Place remaining items (those without specific slot or whose slot was taken/invalid)
            int currentAutoSlot = 0;
            for(ShopItem item : pageItems) {
                if(item.getGuiSlot() == -1 || item.getGuiSlot() >= itemsPerPage || slotFilled[item.getGuiSlot()] == false && gui.getItem(item.getGuiSlot()) != null ) { // if slot was defined but taken by another specifically slotted item (edge case)
                    while(currentAutoSlot < itemsPerPage && slotFilled[currentAutoSlot]) {
                        currentAutoSlot++;
                    }
                    if(currentAutoSlot < itemsPerPage) {
                        gui.setItem(currentAutoSlot, createDisplayItem(item, player));
                        slotFilled[currentAutoSlot] = true;
                    }
                }
            }
        }

        // Navigation Items (only if more than 1 row for items, or if specifically configured)
        if (itemRows > 1 || (itemRows == 1 && totalPages > 1) ) { // Show nav if multiple pages even on 1 item row
            int navRowStartSlot = guiSize - 9;

            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            backMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&e&lBack to Categories"));
            backButton.setItemMeta(backMeta);
            gui.setItem(navRowStartSlot, backButton);

            if (totalPages > 1) {
                ItemStack prevButton = new ItemStack(Material.PAPER);
                ItemMeta prevMeta = prevButton.getItemMeta();
                prevMeta.setDisplayName(page > 1 ? ChatUtils.translateAlternateColorCodes("&a&lPrevious Page") : ChatUtils.translateAlternateColorCodes("&7Previous Page"));
                prevButton.setItemMeta(prevMeta);
                gui.setItem(navRowStartSlot + 3, prevButton);

                ItemStack pageInfo = new ItemStack(Material.BOOK);
                ItemMeta pageMeta = pageInfo.getItemMeta();
                pageMeta.setDisplayName(ChatUtils.translateAlternateColorCodes("&fPage " + page + "/" + totalPages));
                pageInfo.setItemMeta(pageMeta);
                gui.setItem(navRowStartSlot + 4, pageInfo);

                ItemStack nextButton = new ItemStack(Material.PAPER);
                ItemMeta nextMeta = nextButton.getItemMeta();
                nextMeta.setDisplayName(page < totalPages ? ChatUtils.translateAlternateColorCodes("&a&lNext Page") : ChatUtils.translateAlternateColorCodes("&7Next Page"));
                nextButton.setItemMeta(nextMeta);
                gui.setItem(navRowStartSlot + 5, nextButton);
            }
        }

        if (fillEmptySlots) {
            ItemStack filler = new ItemStack(fillerPaneMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
            for (int i = 0; i < guiSize; i++) {
                 if (gui.getItem(i) == null) {
                    gui.setItem(i, filler.clone()); // Use clone for safety
                }
            }
        }
        player.openInventory(gui);
        if (listener != null) {
            listener.playerOpenedCategoryView(player, category, page);
        }
    }

    /**
     * Creates the ItemStack to be displayed in the shop GUI for a given ShopItem.
     * This includes the material, display name, lore (with prices and permissions), and NBT.
     * @param shopItem The ShopItem definition.
     * @param playerContext The player viewing the shop (for permission checks).
     * @return The ItemStack to display.
     */
    private ItemStack createDisplayItem(ShopItem shopItem, Player playerContext) {
        ItemStack item = shopItem.getSourceItem().clone(); // Clone the source item
        item.setAmount(1); // Display item is always 1
        ItemMeta meta = item.getItemMeta(); // Get meta from the cloned, potentially NBT-rich item

        if (meta == null) {
            plugin.getLogger().warning("AdminShop: Could not get ItemMeta for material " + shopItem.getMaterial());
            ItemStack fallback = new ItemStack(shopItem.getMaterial()); // Create a fresh stack if meta is null
            meta = fallback.getItemMeta();
            if (meta == null) return fallback; // Should be impossible for normal materials
        }

        // Ensure display name from config is applied if it exists, otherwise keep source item's name
        // The sourceItem's display name should already be translated if it was custom.
        // If no custom name in config, sourceItem has default material name.
        // If shopItem.displayName (from config) is different than sourceItem's default, use it.
        if (shopItem.getDisplayName() != null && !shopItem.getDisplayName().equals(ChatColor.RESET + capitalizeFully(shopItem.getMaterial().name().replace("_", " ")))) {
             meta.setDisplayName(shopItem.getDisplayName());
        } else if (shopItem.getDisplayName() == null && item.getType().isItem()) { // Only set if null AND it's an item that can have a name
            meta.setDisplayName(ChatColor.RESET + capitalizeFully(shopItem.getMaterial().name().replace("_", " ")));
        }
        // If sourceItem already has a display name (e.g. from NBT like a spawner), it will be kept unless overwritten by config.


        List<String> lore = new ArrayList<>();
        // Add original lore if exists and configured to do so, or clear it for shop-specific lore.
        // For now, we overwrite with shop lore.
        // if (meta.hasLore()) { lore.addAll(meta.getLore()); lore.add(""); } // Option to keep original lore

        boolean canBuy = shopItem.getBuyPrice() >= 0;
        boolean canSell = shopItem.getSellPrice() >= 0;
        boolean hasBuyPerm = shopItem.getPermissionBuy() == null || playerContext.hasPermission(shopItem.getPermissionBuy());
        boolean hasSellPerm = shopItem.getPermissionSell() == null || playerContext.hasPermission(shopItem.getPermissionSell());

        if (canBuy) {
            String buyMsgKey = hasBuyPerm ? "buy_price_format" : "buy_no_permission_format";
            Map<String,String> buyPlaceholders = new HashMap<>();
            buyPlaceholders.put("price", String.format("%.2f", shopItem.getBuyPrice()));
            buyPlaceholders.put("currency", currencySymbol);
            lore.add(getMessage(buyMsgKey, buyPlaceholders));
            if(!hasBuyPerm) lore.add(getMessage("no_permission_buy",null));

        } else {
            lore.add(getMessage("item_not_buyable", null));
        }

        if (canSell) {
             String sellMsgKey = hasSellPerm ? "sell_price_format" : "sell_no_permission_format";
            Map<String,String> sellPlaceholders = new HashMap<>();
            sellPlaceholders.put("price", String.format("%.2f", shopItem.getSellPrice()));
            sellPlaceholders.put("currency", currencySymbol);
            lore.add(getMessage(sellMsgKey, sellPlaceholders));
            if(!hasSellPerm) lore.add(getMessage("no_permission_sell",null));
        } else {
            lore.add(getMessage("item_not_sellable", null));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if (meta != null) { // Re-check meta as it could be a new one from fallback
            meta.getPersistentDataContainer().set(this.itemKeyPDC, org.bukkit.persistence.PersistentDataType.STRING, shopItem.getInternalName());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates an ItemStack for a shop transaction (e.g., when a player buys an item).
     * This method takes the ShopItem definition and an amount to create a tangible ItemStack.
     * It applies NBT data and ensures the correct display name.
     *
     * @param shopItem The ShopItem definition from the configuration.
     * @param amount   The number of items for this stack.
     * @return A new ItemStack instance configured as per the ShopItem, with the specified amount.
     */
    public ItemStack createItemStackForShopItem(ShopItem shopItem, int amount) {
        // Start with a clone of the sourceItem, which should have material and pre-parsed NBT (like enchantments)
        ItemStack item = shopItem.getSourceItem().clone();
        item.setAmount(amount); // Set the correct amount for this transaction

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            // This case should be rare for valid materials.
            // If meta is null, we might not be able to set display name or other properties.
            // Log a warning if it happens.
            plugin.getLogger().warning("AdminShop: Could not get ItemMeta for creating shop transaction item: " + shopItem.getMaterial());
            return item; // Return the item as is, with amount set.
        }

        // Ensure the display name from the ShopItem definition is used.
        // shopItem.getDisplayName() is already color-translated from the config.
        // The sourceItem might have a default name or a name derived from its NBT (e.g. spawners).
        // We want the shop's configured display name to take precedence for the item given to the player.
        if (shopItem.getDisplayName() != null && !shopItem.getDisplayName().isEmpty()) {
            meta.setDisplayName(shopItem.getDisplayName());
        }
        // Else, it will retain the name from sourceItem (which could be default material name or from NBT)

        // NBT Application:
        // The sourceItem in ShopItem should ideally already have its NBT applied during loadConfiguration.
        // For EnchantedBooks, StoredEnchantments were applied to sourceItem.
        // If more generic NBT needs to be applied here from shopItem.getNbtData() string,
        // it would require a more robust NBT parsing utility (e.g., using a library like NBT-API, or more detailed manual parsing).
        // For now, we rely on sourceItem being correctly pre-configured.
        // Example: if shopItem.getNbtData() was a full JSON string for more complex items, here's where you'd parse and apply it.
        // Bukkit's API for direct string NBT application is limited without NMS.

        // Lore: The lore for the item given to the player usually doesn't need the shop's buy/sell price info.
        // It should have its natural lore (e.g., enchantments on an enchanted book).
        // The sourceItem already has its enchantments (which contribute to lore on enchanted books).
        // If other custom lore needs to be on the *transacted* item, it would be applied here.
        // For now, we assume the sourceItem's inherent lore (if any) is sufficient.
        // If shopItem.getNbtData() implies specific lore (beyond enchantments), that's part of the NBT application.

        item.setItemMeta(meta);
        return item;
    }


    // --- Getters for Listener and Command ---

    /**
     * Gets the main title of the admin shop GUI.
     * @return The main shop title, color-translated.
     */
    public String getMainShopTitle() { return mainShopTitle; }

    /**
     * Gets the currency symbol used in the shop.
     * @return The currency symbol string.
     */
    public String getCurrencySymbol() { return currencySymbol; }

    /**
     * Retrieves a ShopCategory by its display name (as shown in GUI titles).
     * @param title The display name of the category.
     * @return The ShopCategory if found, otherwise null.
     */
    public ShopCategory getCategoryByTitle(String title) {
        for (ShopCategory category : categories) {
            // Ensure titles are compared after color translation, though category.getDisplayName() should already be translated.
            if (category.getDisplayName().equals(ChatUtils.translateAlternateColorCodes(title))) {
                return category;
            }
        }
        return null;
    }

    public ShopItem getShopItemByInternalName(String internalName) {
        for (ShopCategory category : this.categories) {
            for (ShopItem item : category.getItems()) {
                if (item.getInternalName().equals(internalName)) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a ShopCategory based on the ItemStack icon clicked in the main shop GUI.
     * Matches based on material and display name of the category's stored icon.
     * @param icon The ItemStack that was clicked.
     * @return The ShopCategory if a match is found, otherwise null.
     */
    public ShopCategory getCategoryByIcon(ItemStack icon) {
        if (icon == null || icon.getType() == Material.AIR) return null;
        for (ShopCategory category : categories) {
            ItemStack catIcon = category.getCategoryIcon(); // Assumes this was set during openMainShopGUI
            if (catIcon != null && catIcon.getType() == icon.getType()){
                 ItemMeta catIconMeta = catIcon.getItemMeta();
                 ItemMeta clickedIconMeta = icon.getItemMeta();
                 if(catIconMeta != null && clickedIconMeta != null &&
                    catIconMeta.hasDisplayName() && clickedIconMeta.hasDisplayName() &&
                    catIconMeta.getDisplayName().equals(clickedIconMeta.getDisplayName())) { // Both names should be color-translated consistently
                     return category;
                 }
            }
        }
        return null;
    }

    /**
     * Calculates the total number of pages for a given category based on item count and items per page.
     * @param category The ShopCategory.
     * @return The total number of pages (at least 1).
     */
    public int getTotalPages(ShopCategory category) {
        if (category == null) return 0;
        int itemsPerPage = Math.max(1, itemRows * 9 - 9);
         if (itemRows == 1) { itemsPerPage = itemRows * 9; }

        int totalItems = category.getItems().size();
        return Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
    }

    // --- Other Helper Getters (Primarily for internal use or potential API extensions) ---

    /**
     * Retrieves a ShopCategory by its internal configuration name.
     * @param internalName The internal name of the category.
     * @return The ShopCategory if found, otherwise null.
     */
    public ShopCategory getCategory(String internalName) {
        return categories.stream()
                         .filter(cat -> cat.getInternalName().equalsIgnoreCase(internalName))
                         .findFirst()
                         .orElse(null);
    }

    public ShopItem getShopItemFromCategoryBySlot(ShopCategory category, int slot) {
        // This needs to be more robust if items are not strictly mapped 0 to (itemsPerPage-1)
        // For now, it assumes itemsBySlot is populated for specifically assigned slots,
        // or listener needs to iterate category.getItems() for sequentially placed items.
        // Let's refine: the listener will get items based on visual slot, which might not map directly.
        // The listener should probably iterate through the items displayed on the current page.
        // However, if itemsBySlot is populated by AdminShopGUIManager correctly for the *displayed* slots, then it's fine.
        // The current openCategoryGUI logic places items sequentially or by gui_slot, but itemsBySlot uses configured gui_slot.
        // This needs careful alignment.
        // A safer way for the listener:
        // 1. Get items for page.
        // 2. If item has specific slot in config AND matches event.getSlot(), use it.
        // 3. Otherwise, map event.getSlot() to index in pageItems list.
        // For now, let's assume itemsBySlot can be used if the slot matches a configured one.
        // This is tricky because itemsBySlot uses *configured* gui_slot, not *runtime* slot.

        // Let's change this: iterate through items on the current page based on slot.
        // This requires knowing the page items.
        // This method might be better if it takes (category, slot, page)
        // Or, the listener gets the list of items for the page and finds the item by its display position.
        // For now, the listener's current logic of iterating through items on page is better.
        // So, this specific getShopItemFromCategoryBySlot might be less useful if slots are auto-assigned.
        // Let's keep it as is, but acknowledge its limitations for auto-placed items.
        return category.getItemsBySlot().get(slot);
    }

    public ShopItem getShopItemByInternalName(String categoryInternalName, String itemInternalName) {
        ShopCategory category = getCategory(categoryInternalName);
        if (category != null) {
            return category.getItems().stream()
                           .filter(item -> item.getInternalName().equalsIgnoreCase(itemInternalName))
                           .findFirst()
                           .orElse(null);
        }
        return null;
    }

    public List<ShopCategory> getCategories() {
        return new ArrayList<>(categories);
    }

    public void reloadConfig() {
        plugin.getLogger().info("Reloading AdminShop configuration...");
        categories.clear();
        messages.clear();
        loadConfiguration(); // This will repopulate categories and messages
        plugin.getLogger().info("AdminShop configuration reloaded.");
    }
}
