package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.Shop;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShopCategoryGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> /* extends com.keviin.keviincore.gui.BackGUI */ { // TODO: Update Team and IridiumUser to actual classes, resolve BackGUI
    private final IridiumTeams<T, U> iridiumTeams;
    @Getter
    private final String categoryName;
    private final Shop.ShopCategory shopCategory;
    @Getter
    private int page;
    private Player player; // Added player field

    public ShopCategoryGUI(String categoryName, Player player, int page, IridiumTeams<T, U> iridiumTeams) {
        // super(iridiumTeams.getInventories().shopCategoryGUI.background, player, iridiumTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.iridiumTeams = iridiumTeams;
        this.categoryName = categoryName;
        this.shopCategory = iridiumTeams.getShop().categories.get(categoryName); // TODO: May need a null check or default if getShop() isn't ready
        this.page = page;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = iridiumTeams.getInventories().shopOverviewGUI;
        // Inventory inventory = Bukkit.createInventory(this, shopCategory.inventorySize, StringUtils.color(noItemGUI.title.replace("%category_name%", categoryName))); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, shopCategory.inventorySize, "ShopCategory GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // if (!iridiumTeams.getShop().items.containsKey(categoryName)) { // TODO: Uncomment when getShop is available
            // iridiumTeams.getLogger().warning("Shop Category " + categoryName + " Is not configured with any items!");
            // return;
        // }

        // for (Shop.ShopItem shopItem : iridiumTeams.getShop().items.get(categoryName)) { // TODO: Uncomment when getShop is available
            // if (shopItem.page != this.page) continue;
            // ItemStack itemStack = shopItem.type.parseItem();
            // ItemMeta itemMeta = itemStack.getItemMeta();

            // itemStack.setAmount(shopItem.defaultAmount);
            // itemMeta.setDisplayName(StringUtils.color(shopItem.name)); // TODO: Replace StringUtils.color
            // itemMeta.setLore(getShopLore(shopItem));

            // itemStack.setItemMeta(itemMeta);
            // inventory.setItem(shopItem.slot, itemStack);
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(this.iridiumTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(this.iridiumTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    private List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getShopLorePlaceholders(Shop.ShopItem item) { // TODO: Replace Placeholder
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = new ArrayList<>(Arrays.asList( // TODO: Replace Placeholder
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("amount", iridiumTeams.getShopManager().formatPrice(item.defaultAmount)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_cost", iridiumTeams.getShopManager().formatPrice(item.buyCost.money)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_reward", iridiumTeams.getShopManager().formatPrice(item.sellCost.money)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("minLevel", String.valueOf(item.minLevel)) // TODO: Replace Placeholder
        // ));
        // for (Map.Entry<String, Double> bankItem : item.buyCost.bankItems.entrySet()) {
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_cost", iridiumTeams.getShopManager().formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder, uncomment when getShopManager is available
        // }
        // for (Map.Entry<String, Double> bankItem : item.sellCost.bankItems.entrySet()) {
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_reward", iridiumTeams.getShopManager().formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder, uncomment when getShopManager is available
        // }
        // return placeholders;
        return Collections.emptyList(); // Placeholder
    }

    private List<String> getShopLore(Shop.ShopItem item) {
        // List<String> lore = item.lore == null ? new ArrayList<>() : new ArrayList<>(StringUtils.color(item.lore)); // TODO: Replace StringUtils.color
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = getShopLorePlaceholders(item); // TODO: Replace Placeholder

        // if (item.buyCost.canPurchase()) {
            // lore.add(iridiumTeams.getShop().buyPriceLore); // TODO: Uncomment when getShop is available
        // } else {
            // lore.add(iridiumTeams.getShop().notPurchasableLore); // TODO: Uncomment when getShop is available
        // }

        // if(item.minLevel > 1) {
            // lore.add(iridiumTeams.getShop().levelRequirementLore); // TODO: Uncomment when getShop is available
        // }

        // if (item.sellCost.canPurchase()) {
            // lore.add(iridiumTeams.getShop().sellRewardLore); // TODO: Uncomment when getShop is available
        // } else {
            // lore.add(iridiumTeams.getShop().notSellableLore); // TODO: Uncomment when getShop is available
        // }

        // lore.addAll(iridiumTeams.getShop().shopItemLore); // TODO: Uncomment when getShop is available

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

        // Optional<Shop.ShopItem> shopItem = iridiumTeams.getShop().items.get(categoryName).stream() // TODO: Uncomment when getShop is available
                // .filter(item -> item.slot == event.getSlot())
                // .filter(item -> item.page == this.page)
                // .findAny();

        // if (!shopItem.isPresent()) {
            // return;
        // }

        // Player player = (Player) event.getWhoClicked();
        // int amount = event.isShiftClick() ? shopItem.get().type.parseItem().getMaxStackSize() : shopItem.get().defaultAmount;
        // if (event.isLeftClick() && shopItem.get().buyCost.canPurchase()) {
            // iridiumTeams.getShopManager().buy(player, shopItem.get(), amount); // TODO: Uncomment when getShopManager is available
        // } else if (event.isRightClick() && shopItem.get().sellCost.canPurchase()) {
            // iridiumTeams.getShopManager().sell(player, shopItem.get(), amount); // TODO: Uncomment when getShopManager is available
        // } else {
            // iridiumTeams.getShop().failSound.play(player); // TODO: Uncomment when getShop is available
        // }
    }

    private boolean doesNextPageExist() {
        // return iridiumTeams.getShop().items.get(categoryName).stream().anyMatch(item -> item.page == this.page + 1); // TODO: Uncomment when getShop is available
        return false; // Placeholder
    }

    private boolean doesPreviousPageExist() {
        // return iridiumTeams.getShop().items.get(categoryName).stream().anyMatch(item -> item.page == this.page - 1); // TODO: Uncomment when getShop is available
        return false; // Placeholder
    }
}
