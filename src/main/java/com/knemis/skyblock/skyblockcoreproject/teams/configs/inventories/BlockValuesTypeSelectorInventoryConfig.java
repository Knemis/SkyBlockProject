package com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories;

// import com.keviin.keviincore.Background; // TODO: Replace with actual Background class
// import com.keviin.keviincore.Item; // TODO: Replace with actual Item class
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BlockValuesTypeSelectorInventoryConfig extends com.knemis.skyblock.skyblockcoreproject.teams.configs.inventories.NoItemGUI { // TODO: Ensure NoItemGUI is correctly referenced or imported
    public BlockTypeItem blocks;
    public BlockTypeItem spawners;

    public BlockValuesTypeSelectorInventoryConfig(int size, String title, com.knemis.skyblock.skyblockcoreproject.teams.Background background, BlockTypeItem blocks, BlockTypeItem spawners) { // TODO: Replace with actual Background class
        this.size = size;
        this.title = title;
        this.background = background;
        this.blocks = blocks;
        this.spawners = spawners;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class BlockTypeItem {
        public com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
        public boolean enabled;
    }
}
