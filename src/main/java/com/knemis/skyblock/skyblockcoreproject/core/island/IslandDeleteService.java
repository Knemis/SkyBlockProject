package com.knemis.skyblock.skyblockcoreproject.core.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IslandDeleteService {
    private final SkyBlockProject plugin;

    public IslandDeleteService(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    public boolean deleteIsland(Player player) {
        if (!plugin.getIslandManager().playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
            return false;
        }

        Location islandBaseLocation = plugin.getIslandManager().getIslandLocation(player.getUniqueId());
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız ve tüm bölgesi siliniyor...");
        try {
            CuboidRegion islandTerritory = plugin.getIslandManager().getIslandTerritoryRegion(islandBaseLocation);
            
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                editSession.setBlocks(islandTerritory, BlockTypes.AIR.getDefaultState());
            }

            // Remove island data from storage
            plugin.getIslandManager().removeIslandDataFromStorage(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla silindi.");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Ada silme sırasında hata: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Ada silinirken bir hata oluştu.");
            return false;
        }
    }
}