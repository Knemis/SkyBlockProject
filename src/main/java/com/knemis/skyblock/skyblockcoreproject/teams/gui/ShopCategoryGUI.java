package com.knemis.skyblock.skyblockcoreproject.teams.gui;

// import com.keviin.keviincore.gui.BackGUI; // TODO: Replace with actual BackGUI class or remove extension
// import com.keviin.keviincore.utils.ItemStackUtils; // TODO: Replace ItemStackUtils
// import com.keviin.keviincore.utils.Placeholder; // TODO: Replace Placeholder
// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils
// TODO: Address keviincore imports later, possibly replace with com.knemis.skyblock.skyblockcoreproject.utils.*
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.Shop;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.User; // TODO: Update to actual User class
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

// TODO: Update Team and User to actual classes, resolve BackGUI
public class ShopCategoryGUI<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.User<T>> /* extends com.keviin.keviincore.gui.BackGUI */ {
    private final SkyBlockTeams<T, U> skyblockTeams;
    @Getter
    private final String categoryName;
    private final Shop.ShopCategory shopCategory;
    @Getter
    private int page;
    private Player player; // Added player field

    public ShopCategoryGUI(String categoryName, Player player, int page, SkyBlockTeams<T, U> skyblockTeams) {
        // super(skyblockTeams.getInventories().shopCategoryGUI.background, player, skyblockTeams.getInventories().backButton); // TODO: Uncomment when BackGUI and inventories are refactored
        this.player = player; // Added
        this.skyblockTeams = skyblockTeams;
        this.categoryName = categoryName;
        this.shopCategory = skyblockTeams.getShop().categories.get(categoryName); // TODO: May need a null check or default if getShop() isn't ready
        this.page = page;
    }

    // @NotNull //TODO: Uncomment if super class method has it
    // @Override //TODO: Uncomment if super class method has it
    public Inventory getInventory() { // TODO: This method likely needs to be @Override if BackGUI is a proper GUI base class
        NoItemGUI noItemGUI = skyblockTeams.getInventories().shopOverviewGUI;
        // Inventory inventory = Bukkit.createInventory(this, shopCategory.inventorySize, StringUtils.color(noItemGUI.title.replace("%category_name%", categoryName))); // TODO: Replace StringUtils.color. 'this' might not be an InventoryHolder if BackGUI is not extended.
        Inventory inventory = Bukkit.createInventory(null, shopCategory.inventorySize, "ShopCategory GUI Title Placeholder"); // Placeholder
        addContent(inventory);
        return inventory;
    }

    // @Override //TODO: Uncomment if super class method has it
    public void addContent(Inventory inventory) {
        // super.addContent(inventory); // TODO: Uncomment if BackGUI is extended and has this method

        // if (!skyblockTeams.getShop().items.containsKey(categoryName)) { // TODO: Uncomment when getShop is available
            // skyblockTeams.getLogger().warning("Shop Category " + categoryName + " Is not configured with any items!");
            // return;
        // }

        // for (Shop.ShopItem shopItem : skyblockTeams.getShop().items.get(categoryName)) { // TODO: Uncomment when getShop is available
            // if (shopItem.page != this.page) continue;
            // ItemStack itemStack = shopItem.type.parseItem();
            // ItemMeta itemMeta = itemStack.getItemMeta();

            // itemStack.setAmount(shopItem.defaultAmount);
            // itemMeta.setDisplayName(StringUtils.color(shopItem.name)); // TODO: Replace StringUtils.color
            // itemMeta.setLore(getShopLore(shopItem));

            // itemStack.setItemMeta(itemMeta);
            // inventory.setItem(shopItem.slot, itemStack);
        // }

        // inventory.setItem(inventory.getSize() - 3, ItemStackUtils.makeItem(this.skyblockTeams.getInventories().nextPage)); // TODO: Replace ItemStackUtils.makeItem
        // inventory.setItem(inventory.getSize() - 7, ItemStackUtils.makeItem(this.skyblockTeams.getInventories().previousPage)); // TODO: Replace ItemStackUtils.makeItem
    }

    private List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> getShopLorePlaceholders(Shop.ShopItem item) { // TODO: Replace Placeholder
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = new ArrayList<>(Arrays.asList( // TODO: Replace Placeholder
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("amount", skyblockTeams.getShopManager().formatPrice(item.defaultAmount)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_cost", skyblockTeams.getShopManager().formatPrice(item.buyCost.money)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("vault_reward", skyblockTeams.getShopManager().formatPrice(item.sellCost.money)), // TODO: Replace Placeholder, uncomment when getShopManager is available
                // new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder("minLevel", String.valueOf(item.minLevel)) // TODO: Replace Placeholder
        // ));
        // for (Map.Entry<String, Double> bankItem : item.buyCost.bankItems.entrySet()) {
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_cost", skyblockTeams.getShopManager().formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder, uncomment when getShopManager is available
        // }
        // for (Map.Entry<String, Double> bankItem : item.sellCost.bankItems.entrySet()) {
            // placeholders.add(new com.knemis.skyblock.skyblockcoreproject.teams.Placeholder(bankItem.getKey() + "_reward", skyblockTeams.getShopManager().formatPrice(bankItem.getValue()))); // TODO: Replace Placeholder, uncomment when getShopManager is available
        // }
        // return placeholders;
        return Collections.emptyList(); // Placeholder
    }

    private List<String> getShopLore(Shop.ShopItem item) {
        // List<String> lore = item.lore == null ? new ArrayList<>() : new ArrayList<>(StringUtils.color(item.lore)); // TODO: Replace StringUtils.color
        // List<com.knemis.skyblock.skyblockcoreproject.teams.Placeholder> placeholders = getShopLorePlaceholders(item); // TODO: Replace Placeholder

        // if (item.buyCost.canPurchase()) {
            // lore.add(skyblockTeams.getShop().buyPriceLore); // TODO: Uncomment when getShop is available
        // } else {
            // lore.add(skyblockTeams.getShop().notPurchasableLore); // TODO: Uncomment when getShop is available
        // }

        // if(item.minLevel > 1) {
            // lore.add(skyblockTeams.getShop().levelRequirementLore); // TODO: Uncomment when getShop is available
        // }

        // if (item.sellCost.canPurchase()) {
            // lore.add(skyblockTeams.getShop().sellRewardLore); // TODO: Uncomment when getShop is available
        // } else {
            // lore.add(skyblockTeams.getShop().notSellableLore); // TODO: Uncomment when getShop is available
        // }

        // lore.addAll(skyblockTeams.getShop().shopItemLore); // TODO: Uncomment when getShop is available

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

        // Optional<Shop.ShopItem> shopItem = skyblockTeams.getShop().items.get(categoryName).stream() // TODO: Uncomment when getShop is available
                // .filter(item -> item.slot == event.getSlot())
                // .filter(item -> item.page == this.page)
                // .findAny();

        // if (!shopItem.isPresent()) {
            // return;
        // }

        // Player player = (Player) event.getWhoClicked();
        // int amount = event.isShiftClick() ? shopItem.get().type.parseItem().getMaxStackSize() : shopItem.get().defaultAmount;
        // if (event.isLeftClick() && shopItem.get().buyCost.canPurchase()) {
            // skyblockTeams.getShopManager().buy(player, shopItem.get(), amount); // TODO: Uncomment when getShopManager is available
        // } else if (event.isRightClick() && shopItem.get().sellCost.canPurchase()) {
            // skyblockTeams.getShopManager().sell(player, shopItem.get(), amount); // TODO: Uncomment when getShopManager is available
        // } else {
            // skyblockTeams.getShop().failSound.play(player); // TODO: Uncomment when getShop is available
        // }
    }

    private boolean doesNextPageExist() {
        // return skyblockTeams.getShop().items.get(categoryName).stream().anyMatch(item -> item.page == this.page + 1); // TODO: Uncomment when getShop is available
        return false; // Placeholder
    }

    private boolean doesPreviousPageExist() {
        // return skyblockTeams.getShop().items.get(categoryName).stream().anyMatch(item -> item.page == this.page - 1); // TODO: Uncomment when getShop is available
        return false; // Placeholder
    }
}
