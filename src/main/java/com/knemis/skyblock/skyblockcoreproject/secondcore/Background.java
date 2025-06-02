package com.knemis.skyblock.skyblockcoreproject.secondcore;

import com.cryptomorin.xseries.XMaterial;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public class Background {

    public com.keviin.keviincore.Item filler = new com.keviin.keviincore.Item(XMaterial.BLACK_STAINED_GLASS_PANE, 1, " ", Collections.emptyList());
    public Map<Integer, com.keviin.keviincore.Item> items;

    public Background(Map<Integer, com.keviin.keviincore.Item> items) {
        this.items = items;
    }

    public Background(Map<Integer, com.keviin.keviincore.Item> items, com.keviin.keviincore.Item filler) {
        this.items = items;
        this.filler = filler;
    }

}
