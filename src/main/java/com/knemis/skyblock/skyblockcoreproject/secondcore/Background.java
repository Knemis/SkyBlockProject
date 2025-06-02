package com.knemis.skyblock.skyblockcoreproject.secondcore;

import com.cryptomorin.xseries.XMaterial;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public class Background {

    public com.knemis.skyblock.skyblockcoreproject.secondcore.Item filler = new com.knemis.skyblock.skyblockcoreproject.secondcore.Item(XMaterial.BLACK_STAINED_GLASS_PANE, 1, " ", Collections.emptyList());
    public Map<Integer, com.knemis.skyblock.skyblockcoreproject.secondcore.Item> items;

    public Background(Map<Integer, com.knemis.skyblock.skyblockcoreproject.secondcore.Item> items) {
        this.items = items;
    }

    public Background(Map<Integer, com.knemis.skyblock.skyblockcoreproject.secondcore.Item> items, com.knemis.skyblock.skyblockcoreproject.secondcore.Item filler) {
        this.items = items;
        this.filler = filler;
    }

}
