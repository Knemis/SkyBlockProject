package com.knemis.skyblock.skyblockcoreproject.shop.admin;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdminShopGUIManager {

    private final SkyBlockProject plugin;
    private AdminShopListener listener;
    private FileConfiguration adminShopConfig;
    private File adminShopFile;
    private NamespacedKey itemKeyPDC;

    private Component mainShopTitle = Component.text("Admin Shop", NamedTextColor.DARK_GREEN, TextDecoration.BOLD); // Default
    private int categoryRows = 3;
    private int itemRows = 3;
    private boolean fillEmptySlots = true;
    private Material fillerPaneMaterial = Material.BLACK_STAINED_GLASS_PANE;

    private final List<ShopCategory> categories = new ArrayList<>();
    private final Map<String, Component> messages = new HashMap<>(); // Changed to Component

    private Component currencySymbol = Component.text("$"); // Default currency symbol, now Component

    public AdminShopGUIManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.itemKeyPDC = new NamespacedKey(plugin, "admin_shop_item_internal_name");
        loadConfiguration();
    }

    public void setListener(AdminShopListener listener) {
        this.listener = listener;
    }

    public static class ShopItem {
        String internalName;
        Material material;
        Component displayName; // Changed to Component
        String nbtData;
        double buyPrice;
        double sellPrice;
        int guiSlot;
        String permissionBuy;
        String permissionSell;
        ShopCategory parentCategory;
        ItemStack sourceItem;

        public ShopItem(String internalName, Material material, Component displayName, String nbtData,
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

        public String getInternalName() { return internalName; }
        public Material getMaterial() { return material; }
        public Component getDisplayName() { return displayName; } // Returns Component
        public String getNbtData() { return nbtData; }
        public double getBuyPrice() { return buyPrice; }
        public double getSellPrice() { return sellPrice; }
        public int getGuiSlot() { return guiSlot; }
        public String getPermissionBuy() { return permissionBuy; }
        public String getPermissionSell() { return permissionSell; }
        public ShopCategory getParentCategory() { return parentCategory; }
        public ItemStack getSourceItem() { return sourceItem; }
    }

    public static class ShopCategory {
        String internalName;
        Component displayName; // Changed to Component
        Material guiMaterial;
        List<ShopItem> items = new ArrayList<>();
        Map<Integer, ShopItem> itemsBySlot = new HashMap<>();
        ItemStack categoryIcon;

        public ShopCategory(String internalName, Component displayName, Material guiMaterial) {
            this.internalName = internalName;
            this.displayName = displayName;
            this.guiMaterial = guiMaterial;
        }

        public void addItem(ShopItem item) {
            this.items.add(item);
            if (item.getGuiSlot() != -1 && item.getGuiSlot() < 54) {
                this.itemsBySlot.put(item.getGuiSlot(), item);
            }
        }
        public void setCategoryIcon(ItemStack icon) { this.categoryIcon = icon; }
        public ItemStack getCategoryIcon() { return this.categoryIcon; }
        public String getInternalName() { return internalName; }
        public Component getDisplayName() { return displayName; } // Returns Component
        public Material getGuiMaterial() { return guiMaterial; }
        public List<ShopItem> getItems() { return items; }
        public Map<Integer, ShopItem> getItemsBySlot() { return itemsBySlot; }
    }

    public void loadConfiguration() {
        adminShopFile = new File(plugin.getDataFolder(), "adminshop.yml");
        if (!adminShopFile.exists()) {
            plugin.saveResource("adminshop.yml", false);
        }
        adminShopConfig = YamlConfiguration.loadConfiguration(adminShopFile);
        InputStream defaultConfigStream = plugin.getResource("adminshop.yml");
        if (defaultConfigStream != null) {
            adminShopConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream)));
        }

        ConfigurationSection settingsSection = adminShopConfig.getConfigurationSection("admin_shop_settings");
        if (settingsSection == null) {
            plugin.getLogger().severe("AdminShop Error: 'admin_shop_settings' section is missing! Using defaults.");
            settingsSection = adminShopConfig.createSection("admin_shop_settings");
        }

        mainShopTitle = ChatUtils.deserializeLegacyColorCodes(settingsSection.getString("title", "&2Admin Shop"));
        categoryRows = Math.max(1, Math.min(6, settingsSection.getInt("category_rows", 3)));
        itemRows = Math.max(1, Math.min(6, settingsSection.getInt("item_rows", 3)));
        fillEmptySlots = settingsSection.getBoolean("fill_empty_slots_with_pane", true);
        try {
            fillerPaneMaterial = Material.valueOf(settingsSection.getString("pane_material", "BLACK_STAINED_GLASS_PANE").toUpperCase());
        } catch (IllegalArgumentException e) {
            fillerPaneMaterial = Material.BLACK_STAINED_GLASS_PANE;
        }
        currencySymbol = ChatUtils.deserializeLegacyColorCodes(settingsSection.getString("currency_symbol", "$"));

        messages.clear();
        ConfigurationSection messagesSection = settingsSection.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, ChatUtils.deserializeLegacyColorCodes(messagesSection.getString(key)));
            }
        }
        messages.putIfAbsent("default_error", Component.text("Message key not found.", NamedTextColor.RED));
        // Add other default messages as Component

        categories.clear();
        ConfigurationSection categoriesSection = adminShopConfig.getConfigurationSection("shop_categories");
        if (categoriesSection != null) {
            for (String categoryKey : categoriesSection.getKeys(false)) {
                ConfigurationSection catConfig = categoriesSection.getConfigurationSection(categoryKey);
                if (catConfig == null) continue;

                Component catDisplayName = ChatUtils.deserializeLegacyColorCodes(catConfig.getString("display_name", categoryKey));
                Material catMaterial;
                try {
                    catMaterial = Material.valueOf(catConfig.getString("gui_material", "GRASS_BLOCK").toUpperCase());
                } catch (IllegalArgumentException e) {
                    catMaterial = Material.GRASS_BLOCK;
                }
                ShopCategory category = new ShopCategory(categoryKey, catDisplayName, catMaterial);

                ConfigurationSection itemsSection = catConfig.getConfigurationSection("items");
                if (itemsSection != null) {
                    for (String itemKey : itemsSection.getKeys(false)) {
                        ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                        if (itemConfig == null) continue;
                        Material itemMaterial;
                        try {
                            itemMaterial = Material.valueOf(itemConfig.getString("material", "").toUpperCase());
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("AdminShop Error: Invalid material name '" + itemConfig.getString("material") + "' for item '" + itemKey + "' in category '" + categoryKey + "'. Skipping item.");
                            continue;
                        }

                        String displayNameStr = itemConfig.getString("display_name");
                        Component itemDisplayName;
                        if (displayNameStr != null) {
                            itemDisplayName = ChatUtils.deserializeLegacyColorCodes(displayNameStr);
                        } else {
                            itemDisplayName = Component.text(capitalizeFully(itemMaterial.name().replace("_", " ")), NamedTextColor.WHITE); // Changed RESET to WHITE
                        }

                        String nbt = itemConfig.getString("nbt");
                        double buyPrice = itemConfig.getDouble("buy", -1.0);
                        double sellPrice = itemConfig.getDouble("sell", -1.0);
                        int guiSlot = itemConfig.getInt("gui_slot", -1);
                        String permBuy = itemConfig.getString("permission_buy");
                        String permSell = itemConfig.getString("permission_sell");

                        ItemStack sourceStack = new ItemStack(itemMaterial);
                        ItemMeta sourceMeta = sourceStack.getItemMeta();
                        if (sourceMeta != null) {
                            if (displayNameStr != null) { // Only set if custom name was in config
                                sourceMeta.displayName(itemDisplayName);
                            }
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
                                            if (ench == null) ench = Enchantment.getByKey(NamespacedKey.minecraft(enchantId.toLowerCase()));
                                            if (ench != null) esm.addStoredEnchant(ench, level, true);
                                        } catch (Exception ignored) {}
                                    }
                                }
                            }
                            sourceStack.setItemMeta(sourceMeta);
                        }
                        category.addItem(new ShopItem(itemKey, itemMaterial, itemDisplayName, nbt, buyPrice, sellPrice, guiSlot, permBuy, permSell, category, sourceStack));
                    }
                }
                categories.add(category);
            }
        }
        plugin.getLogger().info("AdminShop: Loaded " + categories.size() + " categories and " + categories.stream().mapToInt(c -> c.items.size()).sum() + " items.");
    }

    private String capitalizeFully(String str) {
        if (str == null || str.isEmpty()) return str;
        return Pattern.compile("\\b(.)(.*?)\\b").matcher(str.toLowerCase()).replaceAll(m -> m.group(1).toUpperCase() + m.group(2));
    }

    public Component getMessage(String key, @Nullable Map<String, String> placeholders) {
        Component messageComponent = messages.getOrDefault(key, messages.getOrDefault("default_error", Component.text("Message key '" + key + "' not found.", NamedTextColor.RED)));
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholderKey = "{" + entry.getKey() + "}";
                String placeholderValue = entry.getValue() == null ? "null" : entry.getValue();
                 messageComponent = messageComponent.replaceText(TextReplacementConfig.builder().matchLiteral(placeholderKey).replacement(placeholderValue).build());
            }
        }
        return messageComponent;
    }

    public void openMainShopGUI(Player player) {
        int numCategories = categories.size();
        int guiSize = Math.max(9, (int) Math.ceil(numCategories / 9.0) * 9);
        guiSize = Math.min(guiSize, categoryRows * 9);
        guiSize = Math.min(guiSize, 54);

        Inventory gui = Bukkit.createInventory(player, guiSize, mainShopTitle);

        for (int i = 0; i < numCategories; i++) {
            if (i >= guiSize) break;
            ShopCategory category = categories.get(i);
            ItemStack categoryIcon = new ItemStack(category.getGuiMaterial());
            ItemMeta meta = categoryIcon.getItemMeta();
            if (meta != null) {
                meta.displayName(category.getDisplayName());
                List<Component> lore = new ArrayList<>();
                lore.add(ChatUtils.deserializeLegacyColorCodes("&7Click to view items in this category."));
                meta.lore(lore);
                categoryIcon.setItemMeta(meta);
            }
            category.setCategoryIcon(categoryIcon);
            gui.setItem(i, categoryIcon);
        }
        if (fillEmptySlots) {
            ItemStack filler = new ItemStack(fillerPaneMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
            for (int i = 0; i < guiSize; i++) if (gui.getItem(i) == null) gui.setItem(i, filler);
        }
        player.openInventory(gui);
    }

    public void openCategoryGUI(Player player, ShopCategory category, int page) {
        int itemsPerPage = Math.max(1, itemRows * 9 - (itemRows > 1 ? 9 : 0) );
        int totalItems = category.getItems().size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        page = Math.max(1, Math.min(page, totalPages));
        int guiSize = itemRows * 9;

        Inventory gui = Bukkit.createInventory(player, guiSize, category.getDisplayName());

        List<ShopItem> pageItems = category.getItems().stream().skip((long)(page - 1) * itemsPerPage).limit(itemsPerPage).collect(Collectors.toList());

        if (totalItems == 0) {
            ItemStack emptyShopItem = new ItemStack(Material.BARRIER);
            ItemMeta emptyMeta = emptyShopItem.getItemMeta();
            emptyMeta.displayName(getMessage("shop_is_empty", null));
            emptyShopItem.setItemMeta(emptyMeta);
            gui.setItem(guiSize / 2, emptyShopItem);
        } else {
            boolean[] slotFilled = new boolean[itemsPerPage];
            for(ShopItem item : pageItems) {
                if(item.getGuiSlot() != -1 && item.getGuiSlot() < itemsPerPage) {
                    gui.setItem(item.getGuiSlot(), createDisplayItem(item, player));
                    slotFilled[item.getGuiSlot()] = true;
                }
            }
            int currentAutoSlot = 0;
            for(ShopItem item : pageItems) {
                if(item.getGuiSlot() == -1 || item.getGuiSlot() >= itemsPerPage || !slotFilled[item.getGuiSlot()] && gui.getItem(item.getGuiSlot()) != null ) {
                    while(currentAutoSlot < itemsPerPage && slotFilled[currentAutoSlot]) currentAutoSlot++;
                    if(currentAutoSlot < itemsPerPage) {
                        gui.setItem(currentAutoSlot, createDisplayItem(item, player));
                        slotFilled[currentAutoSlot] = true;
                    }
                }
            }
        }

        if (itemRows > 1 || (itemRows == 1 && totalPages > 1) ) {
            int navRowStartSlot = guiSize - 9;
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            backMeta.displayName(ChatUtils.deserializeLegacyColorCodes("&e&lBack to Categories"));
            backButton.setItemMeta(backMeta);
            gui.setItem(navRowStartSlot, backButton);

            if (totalPages > 1) {
                ItemStack prevButton = new ItemStack(Material.PAPER);
                ItemMeta prevMeta = prevButton.getItemMeta();
                prevMeta.displayName(page > 1 ? ChatUtils.deserializeLegacyColorCodes("&a&lPrevious Page") : ChatUtils.deserializeLegacyColorCodes("&7Previous Page"));
                prevButton.setItemMeta(prevMeta);
                gui.setItem(navRowStartSlot + 3, prevButton);

                ItemStack pageInfo = new ItemStack(Material.BOOK);
                ItemMeta pageMeta = pageInfo.getItemMeta();
                pageMeta.displayName(ChatUtils.deserializeLegacyColorCodes("&fPage " + page + "/" + totalPages));
                pageInfo.setItemMeta(pageMeta);
                gui.setItem(navRowStartSlot + 4, pageInfo);

                ItemStack nextButton = new ItemStack(Material.PAPER);
                ItemMeta nextMeta = nextButton.getItemMeta();
                nextMeta.displayName(page < totalPages ? ChatUtils.deserializeLegacyColorCodes("&a&lNext Page") : ChatUtils.deserializeLegacyColorCodes("&7Next Page"));
                nextButton.setItemMeta(nextMeta);
                gui.setItem(navRowStartSlot + 5, nextButton);
            }
        }
        if (fillEmptySlots) {
            ItemStack filler = new ItemStack(fillerPaneMaterial);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.displayName(Component.empty());
            filler.setItemMeta(fillerMeta);
            for (int i = 0; i < guiSize; i++) if (gui.getItem(i) == null) gui.setItem(i, filler.clone());
        }
        player.openInventory(gui);
        if (listener != null) listener.playerOpenedCategoryView(player, category, page);
    }

    private ItemStack createDisplayItem(ShopItem shopItem, Player playerContext) {
        ItemStack item = shopItem.getSourceItem().clone();
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("AdminShop: Could not get ItemMeta for material " + shopItem.getMaterial());
            return new ItemStack(shopItem.getMaterial()); // Fallback
        }
        meta.displayName(shopItem.getDisplayName()); // Already a Component

        List<Component> lore = new ArrayList<>();
        boolean canBuy = shopItem.getBuyPrice() >= 0;
        boolean canSell = shopItem.getSellPrice() >= 0;
        boolean hasBuyPerm = shopItem.getPermissionBuy() == null || playerContext.hasPermission(shopItem.getPermissionBuy());
        boolean hasSellPerm = shopItem.getPermissionSell() == null || playerContext.hasPermission(shopItem.getPermissionSell());

        if (canBuy) {
            Map<String,String> buyPlaceholders = new HashMap<>();
            buyPlaceholders.put("price", String.format("%.2f", shopItem.getBuyPrice()));
            buyPlaceholders.put("currency", PlainComponentSerializer.plain().serialize(currencySymbol)); // Assuming currencySymbol is simple
            lore.add(getMessage(hasBuyPerm ? "buy_price_format" : "buy_no_permission_format", buyPlaceholders));
            if(!hasBuyPerm) lore.add(getMessage("no_permission_buy",null));
        } else {
            lore.add(getMessage("item_not_buyable", null));
        }
        if (canSell) {
            Map<String,String> sellPlaceholders = new HashMap<>();
            sellPlaceholders.put("price", String.format("%.2f", shopItem.getSellPrice()));
            sellPlaceholders.put("currency", PlainComponentSerializer.plain().serialize(currencySymbol));
            lore.add(getMessage(hasSellPerm ? "sell_price_format" : "sell_no_permission_format", sellPlaceholders));
            if(!hasSellPerm) lore.add(getMessage("no_permission_sell",null));
        } else {
            lore.add(getMessage("item_not_sellable", null));
        }
        meta.lore(lore);
        if (meta != null) { // Re-check meta as it could be a new one from fallback
            meta.getPersistentDataContainer().set(this.itemKeyPDC, org.bukkit.persistence.PersistentDataType.STRING, shopItem.getInternalName());
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createItemStackForShopItem(ShopItem shopItem, int amount) {
        ItemStack item = shopItem.getSourceItem().clone();
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("AdminShop: Could not get ItemMeta for creating shop transaction item: " + shopItem.getMaterial());
            return item;
        }
        if (shopItem.getDisplayName() != null) { // displayName is Component
            meta.displayName(shopItem.getDisplayName());
        }
        item.setItemMeta(meta);
        return item;
    }

    public Component getMainShopTitle() { return mainShopTitle; }
    public Component getCurrencySymbol() { return currencySymbol; }

    public ShopCategory getCategoryByTitle(Component titleComponent) { // Changed to accept Component
        String plainTitle = PlainComponentSerializer.plain().serialize(titleComponent);
        for (ShopCategory category : categories) {
            if (PlainComponentSerializer.plain().serialize(category.getDisplayName()).equals(plainTitle)) {
                return category;
            }
        }
        return null;
    }

    public ShopItem getShopItemByInternalName(String internalName) {
        for (ShopCategory category : this.categories) {
            for (ShopItem item : category.getItems()) {
                if (item.getInternalName().equals(internalName)) return item;
            }
        }
        return null;
    }

    public ShopCategory getCategoryByIcon(ItemStack icon) {
        if (icon == null || icon.getType() == Material.AIR) return null;
        for (ShopCategory category : categories) {
            ItemStack catIcon = category.getCategoryIcon();
            if (catIcon != null && catIcon.getType() == icon.getType()){
                 ItemMeta catIconMeta = catIcon.getItemMeta();
                 ItemMeta clickedIconMeta = icon.getItemMeta();
                 if(catIconMeta != null && clickedIconMeta != null &&
                    catIconMeta.displayName() != null && clickedIconMeta.displayName() != null &&
                    catIconMeta.displayName().equals(clickedIconMeta.displayName())) { // Comparing Components
                     return category;
                 }
            }
        }
        return null;
    }

    public int getTotalPages(ShopCategory category) {
        if (category == null) return 0;
        int itemsPerPage = Math.max(1, itemRows * 9 - (itemRows > 1 ? 9 : 0));
        int totalItems = category.getItems().size();
        return Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
    }

    public ShopCategory getCategory(String internalName) {
        return categories.stream().filter(cat -> cat.getInternalName().equalsIgnoreCase(internalName)).findFirst().orElse(null);
    }

    public ShopItem getShopItemFromCategoryBySlot(ShopCategory category, int slot) {
        return category.getItemsBySlot().get(slot);
    }

    public List<ShopCategory> getCategories() { return new ArrayList<>(categories); }
    public int getItemRows() { return this.itemRows; }
    public void reloadConfig() {
        plugin.getLogger().info("Reloading AdminShop configuration...");
        categories.clear();
        messages.clear();
        loadConfiguration();
        plugin.getLogger().info("AdminShop configuration reloaded.");
    }
}
