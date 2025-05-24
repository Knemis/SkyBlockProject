package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IslandBiomeManager {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandLifecycleManager islandLifecycleManager;

    public IslandBiomeManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler, IslandLifecycleManager islandLifecycleManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandLifecycleManager = islandLifecycleManager;
    }

    public boolean setIslandBiome(Player player, Island island, String biomeName) {
        if (island == null || island.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Biyomunu değiştirebileceğin bir adan veya ada dünyan bulunmuyor.");
            return false;
        }

        Biome targetBiome;
        try {
            targetBiome = Biome.valueOf(biomeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Geçersiz biyom adı: " + biomeName);
            player.sendMessage(ChatColor.YELLOW + "Kullanılabilir biyomları görmek için: /island biome list");
            return false;
        }

        World world = island.getWorld();
        CuboidRegion islandTerritory;
        try {
            islandTerritory = islandLifecycleManager.getIslandTerritoryRegion(island.getBaseLocation());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Ada sınırları hesaplanırken bir hata oluştu: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "Ada sınırları hesaplanırken IO Hatası (setIslandBiome): " + e.getMessage(), e);
            return false;
        }

        if (islandTerritory == null) {
            player.sendMessage(ChatColor.RED + "Ada bölgesi bulunamadı.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Ada biyomu '" + targetBiome.name() + "' olarak ayarlanıyor... Bu işlem biraz zaman alabilir.");

        try {
            BlockVector3 min = islandTerritory.getMinimumPoint();
            BlockVector3 max = islandTerritory.getMaximumPoint();

            for (int chunkX = min.getBlockX() >> 4; chunkX <= max.getBlockX() >> 4; chunkX++) {
                for (int chunkZ = min.getBlockZ() >> 4; chunkZ <= max.getBlockZ() >> 4; chunkZ++) {
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    if (!chunk.isLoaded()) {
                        // UYARI DÜZELTMESİ: chunk.load() metodunun dönüş değeri kullanıldı (veya en azından bir değişkene atandı).
                        @SuppressWarnings("unused") // Eğer "loaded" değişkeni hiç kullanılmayacaksa bu annotation ile uyarı baskılanabilir.
                        boolean loaded = chunk.load(false);
                    }
                    for (int x = chunk.getX() * 16; x < chunk.getX() * 16 + 16; x++) {
                        for (int z = chunk.getZ() * 16; z < chunk.getZ() * 16 + 16; z++) {
                            // HATA DÜZELTMESİ: contains metodu BlockVector3.at() ile çağrıldı.
                            if (islandTerritory.contains(BlockVector3.at(x, min.getBlockY(), z))) {
                                for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                                    if (y >= world.getMinHeight() && y < world.getMaxHeight()) {
                                        world.setBiome(x, y, z, targetBiome);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            world.getPlayers().stream()
                    .filter(p -> islandTerritory.contains(BlockVector3.at(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())))
                    .forEach(pOnline -> pOnline.sendMessage(ChatColor.AQUA + "Ada biyomu değişti! Değişikliklerin tam olarak görünmesi için bölgeden çıkıp tekrar girebilir veya yeniden giriş yapabilirsiniz."));


            island.setCurrentBiome(targetBiome.name());
            islandDataHandler.addOrUpdateIslandData(island);
            islandDataHandler.saveChangesToDisk();

            plugin.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") adasının (" + island.getOwnerUUID() + ") biyomunu " + targetBiome.name() + " olarak ayarladı.");
            player.sendMessage(ChatColor.GREEN + "Adanın biyomu başarıyla " + ChatColor.AQUA + targetBiome.name() + ChatColor.GREEN + " olarak ayarlandı!");
            return true;

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Biyom ayarlanırken beklenmedik bir hata oluştu. Lütfen konsolu kontrol edin.");
            plugin.getLogger().log(Level.SEVERE, "setIslandBiome sırasında hata: " + e.getMessage(), e);
            return false;
        }
    }

    public String getIslandBiome(Island island) {
        if (island != null && island.getCurrentBiome() != null) {
            return island.getCurrentBiome();
        }
        if (island != null && island.getBaseLocation() != null && island.getBaseLocation().getWorld() != null) {
            return island.getBaseLocation().getBlock().getBiome().name();
        }
        return "Bilinmiyor";
    }

    public void sendAvailableBiomes(Player player) {
        List<String> availableBiomes = Arrays.stream(Biome.values())
                .filter(b -> b != Biome.CUSTOM && !b.name().startsWith("THE_VOID"))
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
        player.sendMessage(ChatColor.GOLD + "--- Kullanılabilir Biyomlar ---");
        player.sendMessage(ChatColor.YELLOW + String.join(ChatColor.GRAY + ", " + ChatColor.YELLOW, availableBiomes));
        player.sendMessage(ChatColor.GRAY + "Not: Bazı biyomlar sunucu yapılandırmasına veya oyun versiyonuna göre farklılık gösterebilir.");
    }
}