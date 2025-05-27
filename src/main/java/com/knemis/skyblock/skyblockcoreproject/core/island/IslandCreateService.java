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
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;

public class IslandCreateService {
    private final SkyBlockProject plugin;
    private final File schematicFile;

    public IslandCreateService(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.schematicFile = new File(plugin.getDataFolder(), "island.schem");
    }

    public void createIsland(Player player) {
        if (!schematicFile.exists()) {
            player.sendMessage(ChatColor.RED + "Ada şematiği bulunamadı. Lütfen bir yetkiliye bildirin.");
            plugin.getLogger().severe("createIsland çağrıldığında schematicFile bulunamadı: " + schematicFile.getPath());
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Adanız oluşturuluyor... Bu işlem birkaç saniye sürebilir, lütfen bekleyin.");

        final int actualIslandX = plugin.getNextIslandXAndIncrement();
        final Location islandBaseLocation = new Location(plugin.getIslandManager().getSkyblockWorld(), actualIslandX, 100, 0);

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                player.sendMessage(ChatColor.RED + "Ada oluşturulurken bir hata oluştu. (Şematik Formatı)");
                return;
            }

            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(islandBaseLocation.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(islandBaseLocation.getX(), islandBaseLocation.getY(), islandBaseLocation.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            plugin.getIslandManager().registerNewIsland(player, islandBaseLocation);

            double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
            double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
            double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);
            Location teleportLocation = islandBaseLocation.clone().add(offsetX, offsetY, offsetZ);
            player.teleport(teleportLocation);
            player.sendMessage(ChatColor.GREEN + "Adanız başarıyla oluşturuldu!");

        } catch (Exception e) {
            plugin.getLogger().severe("Ada oluşturma sırasında hata: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Ada oluşturulurken bir hata oluştu.");
        }
    }
}