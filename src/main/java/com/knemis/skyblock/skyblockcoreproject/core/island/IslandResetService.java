package com.knemis.skyblock.skyblockcoreproject.core.island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;

public class IslandResetService {
    private final SkyBlockProject plugin;
    private final File schematicFile;

    public IslandResetService(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem");
    }

    public boolean resetIsland(Player player) {
        if (!plugin.getIslandManager().playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Sıfırlayabileceğin bir adan yok!");
            return false;
        }

        Location islandBaseLocation = plugin.getIslandManager().getIslandLocation(player.getUniqueId());
        if (islandBaseLocation == null || islandBaseLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Adanın konumu veya dünyası bulunamadı.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız sıfırlanıyor... Lütfen bekleyin.");
        try {
            CuboidRegion islandTerritory = plugin.getIslandManager().getIslandTerritoryRegion(islandBaseLocation);
            try (EditSession clearSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                clearSession.setBlocks(islandTerritory, BlockTypes.AIR.getDefaultState());
            }

            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }

            try (EditSession pasteSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            plugin.getIslandManager().clearNamedHomesForPlayer(player.getUniqueId());
            plugin.getIslandManager().resetIslandFlags(player.getUniqueId());
            plugin.getIslandManager().teleportToIsland(player);
            
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla sıfırlandı!");
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Ada sıfırlama sırasında hata: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Ada sıfırlanırken bir hata oluştu.");
            return false;
        }
    }
}