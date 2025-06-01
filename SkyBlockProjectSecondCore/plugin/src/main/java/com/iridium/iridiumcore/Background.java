package com.iridium.iridiumcore;

import com.cryptomorin.xseries.XMaterial;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public class Background {

    public com.iridium.iridiumcore.Item filler = new com.iridium.iridiumcore.Item(XMaterial.BLACK_STAINED_GLASS_PANE, 1, " ", Collections.emptyList());
    public Map<Integer, com.iridium.iridiumcore.Item> items;

    public Background(Map<Integer, com.iridium.iridiumcore.Item> items) {
        this.items = items;
    }

    public Background(Map<Integer, com.iridium.iridiumcore.Item> items, com.iridium.iridiumcore.Item filler) {
        this.items = items;
        this.filler = filler;
    }

}
