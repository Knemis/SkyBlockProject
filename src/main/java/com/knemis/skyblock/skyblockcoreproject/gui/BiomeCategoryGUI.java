package com.knemis.skyblock.skyblockcoreproject.gui;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.base.BackGUI;
import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils;
import com.knemis.skyblock.skyblockcoreproject.utils.InventoryUtils;
import com.knemis.skyblock.skyblockcoreproject.utils.Placeholder;

import com.cryptomorin.xseries.XBiome;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BiomeCategoryGUI extends BackGUI {

    // --- Start Inner Config Classes ---
    public static class CostConfig {
        public final double money;
        public final Map<String, Double> bankItems;

        public CostConfig(@NotNull ConfigurationSection section) {
            this.money = section.getDouble("money", 0);
            Map<String, Double> items = new HashMap<>();
            ConfigurationSection bankItemsSection = section.getConfigurationSection("bank-items");
            if (bankItemsSection != null) {
                for (String key : bankItemsSection.getKeys(false)) {
                    items.put(key, bankItemsSection.getDouble(key));
                }
            }
            this.bankItems = Collections.unmodifiableMap(items);
        }
        public boolean canPurchase() {
            // A biome might be free (no money, no items) but still "purchasable" to select.
            // Or it might have a cost. If money > 0 or bankItems not empty, it has a cost.
            // For simplicity, let's say it's "purchasable" if it's defined, actual check is done by BiomeManager.
            return true;
        }
    }

    public static class BiomeItemConfig {
        public final String id;
        public final String displayName;
        public final XMaterial material;
        public final XBiome biome;
        public final List<String> loreFormat;
        public final int defaultAmount;
        public final int slot;
        public final CostConfig buyCost;
        public final int minLevel;

        public BiomeItemConfig(String id, @NotNull ConfigurationSection section) {
            this.id = id;
            this.displayName = section.getString("display-name", "&cUnnamed Biome");

            String materialName = section.getString("material", "GRASS_BLOCK");
            Optional<XMaterial> xMat = XMaterial.matchXMaterial(materialName);
            if (xMat.isPresent()) {
                this.material = xMat.get();
            } else {
                Bukkit.getLogger().warning("[BiomeCategoryGUI] Invalid material '" + materialName + "' for biome item " + id + ". Defaulting to GRASS_BLOCK.");
                this.material = XMaterial.GRASS_BLOCK;
            }

            String biomeName = section.getString("biome", "PLAINS");
            Optional<XBiome> xBiome = XBiome.matchXBiome(biomeName);
            if(xBiome.isPresent()){
                this.biome = xBiome.get();
            } else {
                 Bukkit.getLogger().warning("[BiomeCategoryGUI] Invalid biome '" + biomeName + "' for biome item " + id + ". Defaulting to PLAINS.");
                this.biome = XBiome.PLAINS;
            }

            this.loreFormat = section.getStringList("lore");
            this.defaultAmount = section.getInt("amount", 1);
            this.slot = section.getInt("slot", -1); // Default to -1 if not set, indicating auto-placement or error

            ConfigurationSection costSection = section.getConfigurationSection("cost");
            this.buyCost = (costSection != null) ? new CostConfig(costSection) : new CostConfig(Bukkit.getPluginManager().getPlugins()[0].getConfig().createSection("dummy")); // Empty cost

            this.minLevel = section.getInt("min-level", 0); // Default to 0, meaning no level requirement
        }
    }

    public static class BiomeCategoryConfig {
        public final String categoryId;
        public final String titleFormat;
        public final int inventorySize;
        public final List<BiomeItemConfig> items;

        public BiomeCategoryConfig(String categoryId, @NotNull ConfigurationSection section, @NotNull SkyBlockProject plugin) {
            this.categoryId = categoryId;
            this.titleFormat = section.getString("title", "&4Biome Category: %category_name%");
            this.inventorySize = section.getInt("inventory-size", 54);

            List<BiomeItemConfig> biomeItems = new ArrayList<>();
            // Items are configured under "biomes.<categoryName>.items" not "gui.biome-gui.categories.<cat>.items"
            ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("biomes." + categoryId + ".items");
            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    ConfigurationSection itemConfigSection = itemsSection.getConfigurationSection(itemKey);
                    if (itemConfigSection != null) {
                        biomeItems.add(new BiomeItemConfig(itemKey, itemConfigSection));
                    }
                }
            } else {
                 plugin.getLogger().warning("[BiomeCategoryGUI] No items found for biome category '" + categoryId + "' under 'biomes." + categoryId + ".items' in config.");
            }
            this.items = Collections.unmodifiableList(biomeItems);
        }
    }
    // --- End Inner Config Classes ---

    private final SkyBlockProject plugin;
    private final String categoryName;
    private final BiomeCategoryConfig categoryConfig;
    private final List<String> buyPriceLoreFormat;
    private final String notPurchasableLore; // This is for items that are not for sale at all.
    private final String cannotAffordLore; // For items that are for sale but player can't afford.
    private final List<String> levelRequirementLoreFormat;
    private final List<String> biomeItemDefaultLoreFormat; // General lore for biome items
    private final Sound failSound;
    private final Sound successSound;

    public BiomeCategoryGUI(String categoryName, Player player, SkyBlockProject plugin) {
        super(
            InventoryUtils.createSolidBackgroundItem(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()),
            player,
            InventoryUtils.createGuiItem(plugin, "gui.biome-gui.back-button", Material.BARRIER, "&cBack")
        );
        this.plugin = plugin;
        this.categoryName = categoryName;

        ConfigurationSection guiCategorySection = plugin.getConfig().getConfigurationSection("gui.biome-gui.categories." + categoryName);
        if (guiCategorySection == null) {
            plugin.getLogger().severe("Biome GUI category '" + categoryName + "' not found in gui.biome-gui.categories config!");
            this.categoryConfig = null;
        } else {
            this.categoryConfig = new BiomeCategoryConfig(categoryName, guiCategorySection, plugin);
        }

        this.buyPriceLoreFormat = plugin.getConfig().getStringList("gui.biome-gui.lore-formats.buy-price");
        this.notPurchasableLore = ChatUtils.colorize(plugin.getConfig().getString("gui.biome-gui.lore-formats.not-purchasable", "&cNot for sale."));
        this.cannotAffordLore = ChatUtils.colorize(plugin.getConfig().getString("gui.biome-gui.lore-formats.cannot-afford", "&cYou cannot afford this."));
        this.levelRequirementLoreFormat = plugin.getConfig().getStringList("gui.biome-gui.lore-formats.level-requirement");
        this.biomeItemDefaultLoreFormat = plugin.getConfig().getStringList("gui.biome-gui.lore-formats.default-item-lore");

        String failSoundName = plugin.getConfig().getString("gui.biome-gui.sounds.fail", "BLOCK_ANVIL_LAND");
        this.failSound = XSound.matchXSound(failSoundName).map(XSound::parseSound).orElseGet(() -> {
            plugin.getLogger().warning("Failed to parse fail sound: " + failSoundName);
            return null;
        });
        String successSoundName = plugin.getConfig().getString("gui.biome-gui.sounds.success", "ENTITY_PLAYER_LEVELUP");
        this.successSound = XSound.matchXSound(successSoundName).map(XSound::parseSound).orElseGet(() -> {
            plugin.getLogger().warning("Failed to parse success sound: " + successSoundName);
            return null;
        });
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        if (categoryConfig == null) {
            Inventory errorInv = Bukkit.createInventory(this, 27, ChatUtils.colorize("&cError: Biome Category Not Found"));
            super.addContent(errorInv); // Add background and back button to error inventory too
            return errorInv;
        }
        String title = Placeholder.process(categoryConfig.titleFormat, List.of(new Placeholder("category_name", categoryName)));
        Inventory inventory = Bukkit.createInventory(this, categoryConfig.inventorySize, ChatUtils.colorize(title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public void addContent(Inventory inventory) {
        super.addContent(inventory);

        if (categoryConfig == null || categoryConfig.items.isEmpty()) {
            return;
        }

        for (BiomeItemConfig biomeItemConf : categoryConfig.items) {
            if (biomeItemConf.slot < 0 || biomeItemConf.slot >= inventory.getSize()) {
                 plugin.getLogger().warning("Invalid slot " + biomeItemConf.slot + " for biome item " + biomeItemConf.id + " in category " + categoryName + ". Skipping.");
                continue;
            }
            ItemStack itemStack = biomeItemConf.material.parseItem();
            if (itemStack == null) itemStack = new ItemStack(Material.STONE); // Fallback
            itemStack.setAmount(Math.max(1, biomeItemConf.defaultAmount)); // Ensure amount is at least 1

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(ChatUtils.colorize(biomeItemConf.displayName));
                // TODO: Pass player/island context to generateBiomeItemLore if placeholders need current player/island data
                itemMeta.setLore(generateBiomeItemLore(biomeItemConf, player)); // Pass player for context
                itemStack.setItemMeta(itemMeta);
            }
            inventory.setItem(biomeItemConf.slot, itemStack);
        }
    }

    private List<String> generateBiomeItemLore(BiomeItemConfig itemConf, Player forPlayer) {
        List<String> finalLore = new ArrayList<>();

        // TODO: Get player's current island and its level for minLevel check
        // Island playerIsland = plugin.getIslandDataHandler().getIslandByOwner(forPlayer.getUniqueId());
        // int playerIslandLevel = (playerIsland != null) ? playerIsland.getIslandLevel() : 0;
        // boolean meetsLevelRequirement = playerIslandLevel >= itemConf.minLevel;
        boolean meetsLevelRequirement = true; // Placeholder for actual check

        // TODO: Check if player can afford (money and bank items)
        boolean canAfford = true; // Placeholder for actual check

        List<Placeholder> placeholders = new ArrayList<>(List.of(
            new Placeholder("min_level", String.valueOf(itemConf.minLevel)), // Changed placeholder key
            new Placeholder("biome_cost_money", String.format("%,.2f", itemConf.buyCost.money)) // Changed placeholder key
        ));
        itemConf.buyCost.bankItems.forEach((key, value) ->
            placeholders.add(new Placeholder("biome_cost_" + key.toLowerCase(), String.format("%,.0f", value))) // Changed placeholder key
        );

        if (itemConf.loreFormat != null && !itemConf.loreFormat.isEmpty()) {
            finalLore.addAll(Placeholder.process(itemConf.loreFormat, placeholders));
        }

        finalLore.add(" "); // Spacer

        if (itemConf.buyCost.money > 0 || !itemConf.buyCost.bankItems.isEmpty()) {
            finalLore.addAll(Placeholder.process(this.buyPriceLoreFormat, placeholders));
             if (!canAfford) { // If it has a price but cannot afford
                finalLore.add(this.cannotAffordLore); // Already colorized
            }
        } else { // No cost defined, implies it's free or not for direct purchase this way
            // finalLore.add(this.notPurchasableLore); // Or a "Free" message if applicable
        }

        if (itemConf.minLevel > 0) {
             finalLore.addAll(Placeholder.process(this.levelRequirementLoreFormat, placeholders));
             if(!meetsLevelRequirement){
                 finalLore.add(ChatUtils.colorize("&cYour island level is too low.")); // Example
             }
        }
        if (this.biomeItemDefaultLoreFormat != null && !this.biomeItemDefaultLoreFormat.isEmpty()) {
            finalLore.addAll(Placeholder.process(this.biomeItemDefaultLoreFormat, placeholders));
        }

        // Remove empty lines that might result from empty placeholder formats
        finalLore.removeIf(String::isEmpty);

        return finalLore; // Colorization handled by Placeholder.process if formats include colors
    }


    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);
        if (event.isCancelled() && getBackButtonSlot() == event.getSlot()) { // Back button already handled by super
            return;
        }
        event.setCancelled(true); // Cancel all other interactions within this GUI

        if (categoryConfig == null) return;

        Optional<BiomeItemConfig> clickedBiomeItemOpt = categoryConfig.items.stream()
                .filter(item -> item.slot == event.getSlot())
                .findFirst();

        if (!clickedBiomeItemOpt.isPresent()) {
            return;
        }

        BiomeItemConfig clickedBiomeItem = clickedBiomeItemOpt.get();
        Player player = (Player) event.getWhoClicked();

        // TODO: Implement actual Biome Purchase Logic (check level, cost, deduct, apply biome)
        // For now, this is a stub.

        // Example checks (placeholders for real logic)
        // Island playerIsland = plugin.getIslandDataHandler().getIslandByOwner(player.getUniqueId());
        // if (playerIsland == null) {
        //     player.sendMessage(ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.no-island")));
        //     if (failSound != null) player.playSound(player.getLocation(), failSound, 1f, 1f);
        //     return;
        // }
        // if (playerIsland.getIslandLevel() < clickedBiomeItem.minLevel) {
        //     player.sendMessage(ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.biome.level-too-low")));
        //     if (failSound != null) player.playSound(player.getLocation(), failSound, 1f, 1f);
        //     return;
        // }
        // Check cost (Vault and bank items) - this is complex and would involve EconomyManager/BankManager
        // boolean canAfford = checkAffordability(player, playerIsland, clickedBiomeItem.buyCost);
        // if (!canAfford) {
        //    player.sendMessage(ChatUtils.colorize(prefix + plugin.getConfig().getString("messages.biome.cannot-afford")));
        //    if (failSound != null) player.playSound(player.getLocation(), failSound, 1f, 1f);
        //    return;
        // }

        // If all checks pass:
        // Deduct costs
        // plugin.getBiomeManager().setIslandBiome(playerIsland, clickedBiomeItem.biome, player); // Hypothetical

        player.sendMessage(ChatUtils.colorize(prefix + "&a[Stub] Attempting to buy biome: " + clickedBiomeItem.displayName));
        player.sendMessage(ChatUtils.colorize(prefix + "&eBiome ID: " + clickedBiomeItem.biome.name()));
        player.sendMessage(ChatUtils.colorize(prefix + "&eCost: Money: " + clickedBiomeItem.buyCost.money + ", Items: " + clickedBiomeItem.buyCost.bankItems.toString()));
        player.sendMessage(ChatUtils.colorize(prefix + "&eMin Level: " + clickedBiomeItem.minLevel));

        if (successSound != null) player.playSound(player.getLocation(), successSound, 1f, 1f);
        player.closeInventory();
    }
}
