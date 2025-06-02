package com.knemis.skyblock.skyblockcoreproject.secondcore;

import com.cryptomorin.xseries.XMaterial;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public class Background {

    public com.SkyBlockProject.SkyBlockProjectSecondCore.Item filler = new com.SkyBlockProject.SkyBlockProjectSecondCore.Item(XMaterial.BLACK_STAINED_GLASS_PANE, 1, " ", Collections.emptyList());
    public Map<Integer, com.SkyBlockProject.SkyBlockProjectSecondCore.Item> items;

    public Background(Map<Integer, com.SkyBlockProject.SkyBlockProjectSecondCore.Item> items) {
        this.items = items;
    }

    public Background(Map<Integer, com.SkyBlockProject.SkyBlockProjectSecondCore.Item> items, com.SkyBlockProject.SkyBlockProjectSecondCore.Item filler) {
        this.items = items;
        this.filler = filler;
    }

}
