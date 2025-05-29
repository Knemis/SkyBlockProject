package com.knemis.skyblock.skyblockcoreproject.shop.setup;

import com.knemis.skyblock.skyblockcoreproject.gui.ShopSetupGUIManager;
import com.knemis.skyblock.skyblockcoreproject.shop.Shop;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopSetupSession {

    private final UUID playerId;
    private final Location chestLocation;
    private Shop pendingShop;
    private ShopSetupGUIManager.InputType expectedInputType;
    private ItemStack initialStockItem;
    private String currentGuiTitle; // Optional: to track current GUI step more explicitly
    private boolean intentToAllowPlayerBuy = false;
    private boolean intentToAllowPlayerSell = false;

    public ShopSetupSession(UUID playerId, Location chestLocation, Shop pendingShop, ItemStack initialStockItem) {
        this.playerId = playerId;
        this.chestLocation = chestLocation;
        this.pendingShop = pendingShop;
        this.initialStockItem = initialStockItem;
        // expectedInputType can be null initially or set to a default starting type if applicable
        // currentGuiTitle can also be null or set to the first GUI's title
    }

    // Getters (final fields)
    public UUID getPlayerId() {
        return playerId;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    // Getters and Setters for mutable fields
    public Shop getPendingShop() {
        return pendingShop;
    }

    public void setPendingShop(Shop pendingShop) {
        this.pendingShop = pendingShop;
    }

    public ShopSetupGUIManager.InputType getExpectedInputType() {
        return expectedInputType;
    }

    public void setExpectedInputType(ShopSetupGUIManager.InputType expectedInputType) {
        this.expectedInputType = expectedInputType;
    }

    public ItemStack getInitialStockItem() {
        return initialStockItem;
    }

    public void setInitialStockItem(ItemStack initialStockItem) {
        this.initialStockItem = initialStockItem;
    }

    public String getCurrentGuiTitle() {
        return currentGuiTitle;
    }

    public void setCurrentGuiTitle(String currentGuiTitle) {
        this.currentGuiTitle = currentGuiTitle;
    }

    // Getters and Setters for intent flags
    public boolean isIntentToAllowPlayerBuy() {
        return intentToAllowPlayerBuy;
    }

    public void setIntentToAllowPlayerBuy(boolean intentToAllowPlayerBuy) {
        this.intentToAllowPlayerBuy = intentToAllowPlayerBuy;
    }

    public boolean isIntentToAllowPlayerSell() {
        return intentToAllowPlayerSell;
    }

    public void setIntentToAllowPlayerSell(boolean intentToAllowPlayerSell) {
        this.intentToAllowPlayerSell = intentToAllowPlayerSell;
    }

    @Override
    public String toString() {
        return "ShopSetupSession{" +
                "playerId=" + playerId +
                ", chestLocation=" + (chestLocation != null ? chestLocation.toString() : "null") +
                ", pendingShopExists=" + (pendingShop != null) +
                ", expectedInputType=" + expectedInputType +
                ", initialStockItem=" + (initialStockItem != null ? initialStockItem.getType().name() : "null") +
                ", currentGuiTitle='" + currentGuiTitle + '\'' +
                ", intentToAllowPlayerBuy=" + intentToAllowPlayerBuy +
                ", intentToAllowPlayerSell=" + intentToAllowPlayerSell +
                '}';
    }
}
