// com/knemis/skyblock/skyblockcoreproject/shop/ShopType.java
package com.knemis.skyblock.skyblockcoreproject.shop;

public enum ShopType {
    PLAYER_SELL_SHOP,  // Players buy from this shop (owner sells items)
    PLAYER_BUY_SHOP,   // Players sell to this shop (owner buys items)
    PLAYER_BUY_SELL_SHOP; // Shop does both
    // Consider if ADMIN_SHOP should be a type or an orthogonal flag. For now, focus on player shops.
    // BANK_CHEST was removed as it's not actively used in the current shop logic based on previous files.
}