package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

import com.keviin.keviincore.Background;
import com.keviin.keviincore.Item;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BlockValuesTypeSelectorInventoryConfig extends NoItemGUI {
    public BlockTypeItem blocks;
    public BlockTypeItem spawners;

    public BlockValuesTypeSelectorInventoryConfig(int size, String title, Background background, BlockTypeItem blocks, BlockTypeItem spawners) {
        this.size = size;
        this.title = title;
        this.background = background;
        this.blocks = blocks;
        this.spawners = spawners;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class BlockTypeItem {
        public Item item;
        public boolean enabled;
    }
}
