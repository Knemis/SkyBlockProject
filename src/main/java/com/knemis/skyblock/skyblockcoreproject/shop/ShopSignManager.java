package com.knemis.skyblock.skyblockcoreproject.shop;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer; // Added
import org.bukkit.Bukkit;
// import org.bukkit.ChatColor; // To be removed
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
        if (maxLength < 3) maxLength = 3;

        // Deserialize to component to handle colors, then serialize to plain for length check
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(text); // Assuming '&' codes
        String plainText = PlainComponentSerializer.plain().serialize(component);

        if (plainText.length() <= maxLength) {
            return text; // Return original if it contains formatting and is short enough based on plain length
        }

        // If we need to shorten, we shorten the plain text and append "..."
        // This loses color information in the shortened part.
        // A more sophisticated version would try to preserve color on the "..." or the part before it.
        // For now, plain shortening is implemented.
        return plainText.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    public String shortenItemName(String name) {
        return shortenFormattedString(name, 15);
    }

    public String getItemNameForMessages(ItemStack itemStack, int maxLength) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return "Bilinmeyen";
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            Component displayNameComponent = meta.displayName(); // Paper API
            if (displayNameComponent != null) {
                // Serialize to legacy string to pass to shortenFormattedString, which expects legacy codes
                return shortenFormattedString(LegacyComponentSerializer.legacySection().serialize(displayNameComponent), maxLength);
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
            // This string is plain, no legacy codes, fine for shortenFormattedString's plain path
            return shortenFormattedString(capitalizedName.toString().trim(), maxLength);
        }
        return "Eşya";
    }
    
    public Sign findOrCreateAttachedSign(Block chestBlock) {
        BlockFace[] facesToTry = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        Material signMaterial = Material.OAK_WALL_SIGN;

        for (BlockFace face : facesToTry) {
            Block relative = chestBlock.getRelative(face);
            if (Tag.WALL_SIGNS.isTagged(relative.getType()) && relative.getState() instanceof Sign) {
                if (relative.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) relative.getBlockData();
                    if (wallSignData.getFacing().getOppositeFace() == face) {
                        Sign sign = (Sign) relative.getState();
                        // Modern Paper sign lines are Components
                        if (Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD).equals(sign.line(0))) {
                            plugin.getLogger().info("Found existing shop sign for chest at " + Shop.locationToString(chestBlock.getLocation()) + " on face " + face);
                            return sign;
                        }
                    }
                }
            }
        }

        for (BlockFace face : facesToTry) {
            Block potentialSignBlock = chestBlock.getRelative(face);
            if (potentialSignBlock.getType() == Material.AIR) {
                potentialSignBlock.setType(signMaterial, false);
                if (potentialSignBlock.getBlockData() instanceof WallSign) {
                    WallSign wallSignData = (WallSign) potentialSignBlock.getBlockData();
                    wallSignData.setFacing(face.getOppositeFace());
                    potentialSignBlock.setBlockData(wallSignData, true);
                    plugin.getLogger().info(String.format("[SIGN_MGR_TRACE] Successfully created new shop sign for chest at %s. Sign placed on face %s of chest, actual sign location: %s, facing %s.",
                        Shop.locationToString(chestBlock.getLocation()),
                        face.toString(),
                        Shop.locationToString(potentialSignBlock.getLocation()),
                        wallSignData.getFacing().toString()
                    ));
                    return (Sign) potentialSignBlock.getState();
                } else {
                    plugin.getLogger().warning("Failed to set WallSign data for new sign at " + Shop.locationToString(potentialSignBlock.getLocation()));
                    potentialSignBlock.setType(Material.AIR);
                }
            }
        }
        plugin.getLogger().warning("No suitable face found to create a new shop sign for chest at " + Shop.locationToString(chestBlock.getLocation()));
        return null;
    }

    public void updateAttachedSign(Shop shop, String currencySymbol) { // currencySymbol is legacy string
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
        String ownerNamePlain = owner.getName() != null ? owner.getName() : "Bilinmeyen";
        String ownerNameShort = shortenFormattedString(ownerNamePlain, 14); // shortenFormattedString handles plain text

        String itemNameLegacy = getItemNameForMessages(shop.getTemplateItemStack(), 15); // Returns legacy string
        
        String priceStringLegacy;
        boolean canPlayerBuy = shop.getBuyPrice() >= 0;
        boolean canPlayerSell = shop.getSellPrice() >= 0;

        if (canPlayerBuy && canPlayerSell) {
            priceStringLegacy = String.format("Al:%.0f Sat:%.0f", shop.getBuyPrice(), shop.getSellPrice());
        } else if (canPlayerBuy) {
            priceStringLegacy = String.format("Fiyat: %.0f", shop.getBuyPrice());
        } else if (canPlayerSell) {
            priceStringLegacy = String.format("Ödeme: %.0f", shop.getSellPrice());
        } else {
            priceStringLegacy = "Fiyat Yok";
        }

        String bundleInfo = shop.getBundleAmount() + " adet";
        // currencySymbol is already a legacy string
        String fullPriceLineLegacy = bundleInfo + " " + currencySymbol + " " + priceStringLegacy;

        String plainFullPriceLine = PlainComponentSerializer.plain().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(fullPriceLineLegacy));

        if (plainFullPriceLine.length() > 15) {
            fullPriceLineLegacy = shop.getBundleAmount() + currencySymbol + "/" + priceStringLegacy;
            plainFullPriceLine = PlainComponentSerializer.plain().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(fullPriceLineLegacy));
             if (plainFullPriceLine.length() > 15) {
                if (canPlayerBuy && canPlayerSell) priceStringLegacy = String.format("A:%.0f S:%.0f", shop.getBuyPrice(), shop.getSellPrice());
                else if (canPlayerBuy) priceStringLegacy = String.format("F:%.0f", shop.getBuyPrice());
                else if (canPlayerSell) priceStringLegacy = String.format("Ö:%.0f", shop.getSellPrice());
                else priceStringLegacy = "N/A";
                fullPriceLineLegacy = shop.getBundleAmount() + "/" + priceStringLegacy;
            }
        }
        
        String line0Str = "[Dükkan]";
        String line1Str = itemNameLegacy;
        String line2Str = fullPriceLineLegacy;
        String line3Str = ownerNameShort;

        plugin.getLogger().info(String.format("[SIGN_MGR_TRACE] Attempting to write to sign at %s for shop %s: L0='%s', L1='%s', L2='%s', L3='%s'",
            Shop.locationToString(signState.getLocation()),
            shop.getShopId(),
            line0Str,
            line1Str,
            line2Str,
            line3Str
        ));

        // plugin.getLogger().info("[ShopSignManager] Writing to sign for shop " + shop.getShopId() + ": L0=[Dükkan], L1=" + itemNameLegacy + ", L2=" + fullPriceLineLegacy + ", L3=" + ownerNameShort); // Old log, replaced by detailed trace

        signState.line(0, Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD));
        signState.line(1, LegacyComponentSerializer.legacyAmpersand().deserialize(itemNameLegacy).colorIfAbsent(NamedTextColor.BLACK)); // Deserialize and color
        signState.line(2, LegacyComponentSerializer.legacyAmpersand().deserialize(fullPriceLineLegacy).colorIfAbsent(NamedTextColor.DARK_GREEN));
        signState.line(3, LegacyComponentSerializer.legacyAmpersand().deserialize(ownerNameShort).colorIfAbsent(NamedTextColor.DARK_PURPLE));

        plugin.getLogger().info(String.format("[SIGN_MGR_TRACE] Calling signState.update(true) for sign at %s.", Shop.locationToString(signState.getLocation())));
        try {
            signState.update(true);
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
                if (wallSignData.getFacing().getOppositeFace() == face) {
                    Sign signState = (Sign) signBlock.getState();
                    if (Component.text("[Dükkan]", NamedTextColor.DARK_BLUE, TextDecoration.BOLD).equals(signState.line(0))) {
                        signBlock.setType(Material.AIR);
                        plugin.getLogger().info("[ShopSignManager] Shop sign cleared for chest at " + Shop.locationToString(chestLocation) + " on face " + face);
                        return;
                    }
                }
            }
        }
        plugin.getLogger().fine("[ShopSignManager] No attached shop sign found to clear for chest at " + Shop.locationToString(chestLocation));
    }
}
