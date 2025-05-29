package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Level;

public class ShopSignManager {

    private final SkyBlockProject plugin;

    public ShopSignManager(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    public String shortenFormattedString(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength < 3) maxLength = 3; // Ensure space for "..."
        String cleanText = ChatColor.stripColor(text); // Use Bukkit's ChatColor for stripping
        if (cleanText.length() <= maxLength) {
            return text; // Return original if it contains formatting and is short enough
        }
        // If the original text (with color codes) is longer than cleanText,
        // we might need a more sophisticated way to shorten while preserving colors.
        // For now, this simple approach shortens the clean text.
        return cleanText.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    public String shortenItemName(String name) {
        return shortenFormattedString(name, 15);
    }

    public String getItemNameForMessages(ItemStack itemStack, int maxLength) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen";
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            try {
                Component displayNameComponent = meta.displayName();
                if (displayNameComponent != null) {
                    return shortenFormattedString(LegacyComponentSerializer.legacySection().serialize(displayNameComponent), maxLength);
                }
            } catch (NoSuchMethodError e) { // API < 1.16.5 (or no Adventure support)
                 if (meta.hasDisplayName()) return shortenFormattedString(meta.getDisplayName(), maxLength);
            }
        }
        String name = itemStack.getType().toString().toLowerCase().replace("_", " ");
        if (!name.isEmpty()) {
            String[] parts = name.split(" ");
            StringBuilder capitalizedName = new StringBuilder();
            for (String part : parts) {
                if (part.length() > 0) {
                    capitalizedName.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1).toLowerCase()).append(" ");
                }
            }
            return shortenFormattedString(capitalizedName.toString().trim(), maxLength);
        }
        return "Eşya";
    }
    
    public Sign findOrCreateAttachedSign(Block chestBlock) {
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        Material signMaterial = Material.OAK_WALL_SIGN; // Default sign type

        for (BlockFace face : facesToTry) {
            Block relative = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(relative.getType()) && relative.getState() instanceof Sign) {
                if (relative.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) relative.getBlockData();
                    if (wallSignData.getFacing().getOppositeFace() == face) { // Sign is attached to the chest
                        Sign sign = (Sign) relative.getState();
                        // Check if it's a shop sign (e.g., by a specific line or persistent data)
                        if (sign.line(0).equals(Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                            plugin.getLogger().info("Found existing shop sign for chest at " + Shop.locationToString(chestBlock.getLocation()) + " on face " + face);
                            return sign;
                        }
                    }
                }
            }
        }

        // No existing shop sign found, try to create a new one
        for (BlockFace face : facesToTry) {
            Block potentialSignBlock = chestBlock.getRelative(face);
            if (potentialSignBlock.getType() == Material.AIR) { // Found a place for a new sign
                potentialSignBlock.setType(signMaterial, false); // Place the sign block
                if (potentialSignBlock.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) potentialSignBlock.getBlockData();
                    wallSignData.setFacing(face.getOppositeFace()); // Make it face away from the chest
                    potentialSignBlock.setBlockData(wallSignData, true); // Apply facing direction
                    plugin.getLogger().info("Created new shop sign for chest at " + Shop.locationToString(chestBlock.getLocation()) + " on face " + face);
                    return (Sign) potentialSignBlock.getState();
                } else {
                    // Should not happen if signMaterial is a valid WallSign
                    plugin.getLogger().warning("Failed to set WallSign data for new sign at " + Shop.locationToString(potentialSignBlock.getLocation()));
                    potentialSignBlock.setType(Material.AIR); // Clean up if not a valid sign state
                }
            }
        }
        plugin.getLogger().warning("No suitable face found to create a new shop sign for chest at " + Shop.locationToString(chestBlock.getLocation()));
        return null;
    }

    public void updateAttachedSign(Shop shop, String currencySymbol) {
        plugin.getLogger().fine("[ShopSignManager] Attempting to update sign for shop at " + (shop != null ? Shop.locationToString(shop.getLocation()) : "null"));
        if (shop == null || !shop.isSetupComplete() || shop.getLocation() == null || shop.getTemplateItemStack() == null) {
            plugin.getLogger().fine("[ShopSignManager] Update sign aborted: Shop or essential shop data is null/incomplete. Shop: " + shop);
            return;
        }
        Block chestBlock = shop.getLocation().getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST) {
            plugin.getLogger().warning("[ShopSignManager] Update sign aborted: Block at shop location " + Shop.locationToString(shop.getLocation()) + " is not a chest. Type: " + chestBlock.getType());
            return;
        }

        Sign signState = findOrCreateAttachedSign(chestBlock);
        if (signState == null) {
            plugin.getLogger().warning("[ShopSignManager] Could not find or create an attached sign for shop at " + Shop.locationToString(shop.getLocation()));
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        String ownerName = owner.getName() != null ? shortenFormattedString(owner.getName(), 14) : "Bilinmeyen";
        String itemName = getItemNameForMessages(shop.getTemplateItemStack(), 15);
        
        String priceString;
        boolean canPlayerBuy = shop.getBuyPrice() >= 0;
        boolean canPlayerSell = shop.getSellPrice() >= 0;

        if (canPlayerBuy && canPlayerSell) {
            priceString = String.format("Al:%.0f Sat:%.0f", shop.getBuyPrice(), shop.getSellPrice());
        } else if (canPlayerBuy) {
            priceString = String.format("Fiyat: %.0f", shop.getBuyPrice());
        } else if (canPlayerSell) {
            priceString = String.format("Ödeme: %.0f", shop.getSellPrice());
        } else {
            priceString = "Fiyat Yok";
        }

        String bundleInfo = shop.getBundleAmount() + " adet";
        String fullPriceLine = bundleInfo + " " + currencySymbol + " " + priceString;

        // Attempt to shorten if too long
        if (ChatColor.stripColor(fullPriceLine).length() > 15) {
            fullPriceLine = shop.getBundleAmount() + "" + currencySymbol + "/" + priceString; // No spaces, short currency
             if (ChatColor.stripColor(fullPriceLine).length() > 15) { // Try even shorter
                if (canPlayerBuy && canPlayerSell) priceString = String.format("A:%.0f S:%.0f", shop.getBuyPrice(), shop.getSellPrice());
                else if (canPlayerBuy) priceString = String.format("F:%.0f", shop.getBuyPrice());
                else if (canPlayerSell) priceString = String.format("Ö:%.0f", shop.getSellPrice());
                else priceString = "N/A";
                fullPriceLine = shop.getBundleAmount() + "/" + priceString; // Minimal
            }
        }
        
        plugin.getLogger().info("[ShopSignManager] Writing to sign for shop " + shop.getShopId() + ": L0=[Dükkan], L1=" + itemName + ", L2=" + fullPriceLine + ", L3=" + ownerName);

        signState.line(0, Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD));
        signState.line(1, Component.text(itemName, NamedTextColor.BLACK));
        signState.line(2, Component.text(fullPriceLine, NamedTextColor.DARK_GREEN));
        signState.line(3, Component.text(ownerName, NamedTextColor.DARK_PURPLE));
        try {
            signState.update(true); // Force update
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[ShopSignManager] Exception while updating sign state for shop at " + Shop.locationToString(shop.getLocation()), e);
        }
    }

    public void clearAttachedSign(Location chestLocation) {
        if (chestLocation == null) return;
        Block chestBlock = chestLocation.getBlock();
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : facesToTry) {
            Block signBlock = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(signBlock.getType()) && signBlock.getBlockData() instanceof WallSign) {
                WallSign wallSignData = (WallSign) signBlock.getBlockData();
                if (wallSignData.getFacing().getOppositeFace() == face) { // Sign is attached to this chest
                    Sign signState = (Sign) signBlock.getState();
                    // Check if it's a shop sign by looking for the specific first line
                    if (signState.line(0).equals(Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD))) {
                        signBlock.setType(Material.AIR); // Remove the sign
                        plugin.getLogger().info("[ShopSignManager] Shop sign cleared for chest at " + Shop.locationToString(chestLocation) + " on face " + face);
                        return; // Assume only one shop sign per chest
                    }
                }
            }
        }
        plugin.getLogger().fine("[ShopSignManager] No attached shop sign found to clear for chest at " + Shop.locationToString(chestLocation));
    }
}
