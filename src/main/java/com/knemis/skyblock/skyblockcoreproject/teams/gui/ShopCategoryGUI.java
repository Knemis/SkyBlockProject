package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.Shop;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.List;

public class ShopCategoryGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> /* extends com.knemis.skyblock.skyblockcoreproject.core.keviincore.gui.BackGUI */ { // TODO: Update Team and SkyBlockProjectUser to actual classes, resolve BackGUI
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;
    @Getter
    private final String categoryName;
    private final Shop.ShopCategory shopCategory;
    @Getter
    private int page;
    private Player player; // Added player field

    public ShopCategoryGUI(String categoryName, Player player, int page, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // super(SkyBlockProjectTeams.getInventories().shopCategoryGUI.background, player, SkyBlockProjectTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.categoryName = categoryName;
        this.shopCategory = SkyBlockProjectTeams.getShop().categories.get(categoryName); // TODO: May need a null check or default if getShop() isn't ready
        this.page = page;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = SkyBlockProjectTeams.getInventories().shopOverviewGUI;
        // Inventory inventory = Bukkit.createInventory(this, shopCategory.inventorySize, StringUtils.color(noItemGUI.title.replace("%category_name%", categoryName))); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, shopCategory.inventorySize, "ShopCategory GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // if (!SkyBlockProjectTeams.getShop().items.containsKey(categoryName)) { // TODO: Uncomment when getShop is available
            // SkyBlockProjectTeams.getLogger().warning("Shop Category " + categoryName + " Is not configured with any items!");
            // return;
        // }

        // for (Shop.ShopItem shopItem : SkyBlockProjectTeams.getShop().items.get(categoryName)) { // TODO: Uncomment when getShop is available
            // if (shopItem.page != this.page) continue;
            // ItemStack itemStack = shopItem.type.parseItem();
            // ItemMeta itemMeta = itemStack.getItemMeta();

            // itemStack.setAmount(shopItem.defaultAmount);
            // itemMeta.setDisplayName(StringUtils.color(shopItem.name)); // TODO: Replace StringUtils.color
            // itemMeta.setLore(getShopLore(shopItem));

            // itemStack.setItemMeta(itemMeta);
            // inventory.setItem(shopItem.slot, itemStack);
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(this.SkyBlockProjectTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(this.SkyBlockProjectTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    private List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getShopLorePlaceholders(Shop.ShopItem item) { // TODO: Replace Placeholder
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = new ArrayList<>(Arrays.asList( // TODO: Replace Placeholder
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("amount", SkyBlockProjectTeams.getShopManager().formatPrice(item.defaultAmount)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_cost", SkyBlockProjectTeams.getShopManager().formatPrice(item.buyCost.money)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_reward", SkyBlockProjectTeams.getShopManager().formatPrice(item.sellCost.money)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("minLevel", String.valueOf(item.minLevel)) // TODO: Replace Placeholder
        // ));
        // for (Map.Entry<String, Double> bankItem : item.buyCost.bankItems.entrySet()) {
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_cost", SkyBlockProjectTeams.getShopManager().formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder, uncomment when getShopManager is available
        // }
        // for (Map.Entry<String, Double> bankItem : item.sellCost.bankItems.entrySet()) {
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_reward", SkyBlockProjectTeams.getShopManager().formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder, uncomment when getShopManager is available
        // }
        // return placeholders;
        return Collections.emptyList(); // Placeholder
    }

    private List<String> getShopLore(Shop.ShopItem item) {
        // List<String> lore = item.lore == null ? new ArrayList<>() : new ArrayList<>(StringUtils.color(item.lore)); // TODO: Replace StringUtils.color
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = getShopLorePlaceholders(item); // TODO: Replace Placeholder

        // if (item.buyCost.canPurchase()) {
            // lore.add(SkyBlockProjectTeams.getShop().buyPriceLore); // TODO: Uncomment when getShop is available
        // } else {
            // lore.add(SkyBlockProjectTeams.getShop().notPurchasableLore); // TODO: Uncomment when getShop is available
        // }

        // if(item.minLevel > 1) {
            // lore.add(SkyBlockProjectTeams.getShop().levelRequirementLore); // TODO: Uncomment when getShop is available
        // }

        // if (item.sellCost.canPurchase()) {
            // lore.add(SkyBlockProjectTeams.getShop().sellRewardLore); // TODO: Uncomment when getShop is available
        // } else {
            // lore.add(SkyBlockProjectTeams.getShop().notSellableLore); // TODO: Uncomment when getShop is available
        // }

        // lore.addAll(SkyBlockProjectTeams.getShop().shopItemLore); // TODO: Uncomment when getShop is available

        // return StringUtils.color(StringUtils.processMultiplePlaceholders(lore, placeholders)); // TODO: Replace StringUtils methods
        return Collections.emptyList(); // Placeholder
    }

    // @Override //TODO: Uncomment if super class method has it
    public void onInventoryClick(InventoryClickEvent event) {
        // super.onInventoryClick(event); // TODO: Uncomment if BackGUI is extended and has this method

        // if (event.getSlot() == event.getInventory().getSize() - 3 && doesNextPageExist()) {
            // this.page++;
            // addContent(event.getInventory());
            // return;
        // }

        // if (event.getSlot() == event.getInventory().getSize() - 7 && doesPreviousPageExist()) {
            // this.page--;
            // addContent(event.getInventory());
            // return;
        // }

        // Optional<Shop.ShopItem> shopItem = SkyBlockProjectTeams.getShop().items.get(categoryName).stream() // TODO: Uncomment when getShop is available
                // .filter(item -> item.slot == event.getSlot())
                // .filter(item -> item.page == this.page)
                // .findAny();

        // if (!shopItem.isPresent()) {
            // return;
        // }

        // Player player = (Player) event.getWhoClicked();
        // int amount = event.isShiftClick() ? shopItem.get().type.parseItem().getMaxStackSize() : shopItem.get().defaultAmount;
        // if (event.isLeftClick() && shopItem.get().buyCost.canPurchase()) {
            // SkyBlockProjectTeams.getShopManager().buy(player, shopItem.get(), amount); // TODO: Uncomment when getShopManager is available
        // } else if (event.isRightClick() && shopItem.get().sellCost.canPurchase()) {
            // SkyBlockProjectTeams.getShopManager().sell(player, shopItem.get(), amount); // TODO: Uncomment when getShopManager is available
        // } else {
            // SkyBlockProjectTeams.getShop().failSound.play(player); // TODO: Uncomment when getShop is available
        // }
    }

    private boolean doesNextPageExist() {
        // return SkyBlockProjectTeams.getShop().items.get(categoryName).stream().anyMatch(item -> item.page == this.page + 1); // TODO: Uncomment when getShop is available
        return false; // Placeholder
    }

    private boolean doesPreviousPageExist() {
        // return SkyBlockProjectTeams.getShop().items.get(categoryName).stream().anyMatch(item -> item.page == this.page - 1); // TODO: Uncomment when getShop is available
        return false; // Placeholder
    }
}
