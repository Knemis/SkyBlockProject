package com.knemis.skyblock.skyblockcoreproject.island.features; // Veya .island;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager; // IslandManager'a erişim gerekebilir (save için)
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import com.sk89q.worldedit.math.BlockVector3; // getIslandTerritoryRegion için
import com.sk89q.worldedit.regions.CuboidRegion; // getIslandTerritoryRegion için

import java.io.IOException; // getIslandTerritoryRegion için
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IslandBiomeManager {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager; // Island nesnesini kaydetmek için

    public IslandBiomeManager(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    /**
     * Oyuncunun adasının biyomunu değiştirir.
     * @param player Komutu kullanan oyuncu
     * @param island Oyuncunun Island nesnesi
     * @param biomeName Ayarlanacak biyomun adı (String)
     * @return İşlem başarılıysa true, değilse false
     */
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

        // İzin kontrolü (opsiyonel, örn: eklenti parası veya item karşılığı)
        // if (!player.hasPermission("skyblock.biome.change." + targetBiome.name().toLowerCase())) {
        //    player.sendMessage(ChatColor.RED + "Bu biyomu ayarlama iznin yok.");
        //    return false;
        // }

        World world = island.getWorld();
        CuboidRegion islandTerritory;
        try {
            // IslandManager'dan adanın tam bölgesini almamız gerekiyor.
            // Bu metodun public olması veya Island nesnesinin kendi bölgesini tutması gerekebilir.
            // Şimdilik IslandManager'daki getIslandTerritoryRegion metodunu kullanacağız.
            // Bu metodun public olması ve Island nesnesini parametre olarak alması daha iyi olabilir.
            // VEYA Island nesnesi kendi CuboidRegion'ını tutabilir.
            // Basitlik adına şimdilik IslandManager üzerinden gidiyoruz.
            islandTerritory = islandManager.getIslandTerritoryRegion(island.getBaseLocation());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Ada sınırları hesaplanırken bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("Ada sınırları hesaplanırken IO Hatası (setIslandBiome): " + e.getMessage());
            return false;
        }

        if (islandTerritory == null) {
            player.sendMessage(ChatColor.RED + "Ada bölgesi bulunamadı.");
            return false;
        }

        player.sendMessage(ChatColor.YELLOW + "Ada biyomu '" + targetBiome.name() + "' olarak ayarlanıyor... Bu işlem biraz zaman alabilir.");

        // Biyom değiştirme işlemini asenkron yapmak sunucuyu kilitlememek için iyi olabilir,
        // ancak chunk işlemleri genellikle senkron yapılmalıdır. Dikkatli olunmalı.
        // Şimdilik senkron yapalım.
        try {
            BlockVector3 min = islandTerritory.getMinimumPoint();
            BlockVector3 max = islandTerritory.getMaximumPoint();

            // Tüm chunk'ları dolaşarak biyomu ayarla
            for (int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; x++) {
                for (int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; z++) {
                    Chunk chunk = world.getChunkAt(x, z);
                    if (!chunk.isLoaded()) {
                        chunk.load(); // Chunk'ı yükle (gerekirse)
                    }
                    // Chunk içindeki her bloğun biyomunu ayarla (1.16+)
                    // Daha eski sürümler için farklı bir yaklaşım gerekebilir (her bloğa setBiome)
                    for (int cx = 0; cx < 16; cx++) {
                        for (int cz = 0; cz < 16; cz++) {
                            // Y eksenini de dolaşmak gerekiyor mu? Genellikle biyomlar Y'den bağımsızdır
                            // ama bazı özel durumlar olabilir. Minecraft Wiki'ye göre biyomlar sütun bazlı.
                            // Dolayısıyla tüm Y seviyeleri için setBiome çağrısı yapmak yerine
                            // world.setBiome(blockX, blockZ, biome) yeterliydi eski API'lerde.
                            // 1.16.5'te chunk.setBiome(x,y,z,biome) veya world.setBiome(x,y,z,biome) kullanılabilir.
                            // En güvenlisi tüm Y seviyelerini dolaşmak gibi görünüyor ama performansı etkiler.
                            // Sadece bir Y seviyesi için ayarlamak genellikle yeterlidir. (örn: 64)
                            // Ancak en doğru yöntem, WorldGuard bölgesi içindeki her bir X,Z bloğu için ayarlamaktır.
                            int blockX = (x << 4) + cx;
                            int blockZ = (z << 4) + cz;
                            if (islandTerritory.contains(BlockVector3.at(blockX, min.getBlockY(), blockZ))) { // Sadece bölge içindeyse
                                for (int cy = min.getBlockY(); cy <= max.getBlockY(); cy++){ // Bölgenin Y sınırları içinde
                                    world.setBiome(blockX, cy, blockZ, targetBiome);
                                }
                            }
                        }
                    }
                }
            }
            // Oyuncular için chunk'ları yeniden göndermek gerekebilir.
            // world.refreshChunk(chunkX, chunkZ) tüm oyuncular için.
            // Veya sadece adadaki oyuncular için Player#updateInventory() gibi bir şey tetiklenebilir.
            // En basit yöntem, oyuncunun relog atması veya bölgeden çıkıp girmesi.
            // Daha iyisi: Adadaki oyunculara chunk'ları yeniden gönder.
            BlockVector3 center = islandTerritory.getCenter().toBlockPoint();
            int radius = (int) Math.ceil(islandTerritory.getWidth() / 2.0 / 16.0) + 2; // Chunk radius
            for(Player pOnline : world.getPlayers()){
                if(islandTerritory.contains(BlockVector3.at(pOnline.getLocation().getBlockX(), pOnline.getLocation().getBlockY(), pOnline.getLocation().getBlockZ()))){
                    // Oyuncuya chunk'ları yeniden gönderme (Bu kısım karmaşık olabilir ve versiyona göre değişir)
                    // Basit bir çözüm olarak:
                    pOnline.sendMessage(ChatColor.AQUA + "Ada biyomu değişti! Değişikliklerin tam olarak görünmesi için bölgeden çıkıp tekrar girebilirsiniz.");
                }
            }


            island.setCurrentBiome(targetBiome.name()); // Island nesnesine kaydet
            islandManager.saveIslandData(island); // Değişikliği .yml'ye yansıt (veya toplu save)
            try {
                // IslandManager içindeki config nesnesi üzerinden kaydetmek daha iyi
                islandManager.getIslandsConfig().save(islandManager.getIslandsFile());
                plugin.getLogger().info(player.getName() + " adasının biyomunu " + targetBiome.name() + " olarak ayarladı.");
            } catch (IOException e) {
                plugin.getLogger().severe("Ada biyomu kaydedilirken hata: " + e.getMessage());
            }

            player.sendMessage(ChatColor.GREEN + "Adanın biyomu başarıyla " + ChatColor.AQUA + targetBiome.name() + ChatColor.GREEN + " olarak ayarlandı!");
            return true;

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Biyom ayarlanırken beklenmedik bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("setIslandBiome sırasında hata: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Oyuncunun adasının mevcut biyomunu döndürür.
     * @param island Island nesnesi
     * @return Mevcut biyomun adı veya "Bilinmiyor"
     */
    public String getIslandBiome(Island island) {
        if (island != null && island.getCurrentBiome() != null) {
            return island.getCurrentBiome();
        }
        // Eğer adada kayıtlı biyom yoksa, baseLocation'dan bir biyom okumayı deneyebiliriz.
        if (island != null && island.getBaseLocation() != null && island.getBaseLocation().getWorld() != null) {
            return island.getBaseLocation().getBlock().getBiome().name();
        }
        return "Bilinmiyor";
    }

    /**
     * Kullanılabilir biyomların bir listesini oyuncuya gönderir.
     * @param player Komutu kullanan oyuncu
     */
    public void sendAvailableBiomes(Player player) {
        List<String> availableBiomes = Arrays.stream(Biome.values())
                .filter(b -> b != Biome.CUSTOM) // CUSTOM biyomları genellikle ayarlanamaz
                .map(Enum::name)
                .collect(Collectors.toList());

        player.sendMessage(ChatColor.GOLD + "--- Kullanılabilir Biyomlar ---");
        player.sendMessage(ChatColor.YELLOW + String.join(", ", availableBiomes));
        player.sendMessage(ChatColor.GRAY + "Not: Bazı biyomlar sunucu yapılandırmasına göre çalışmayabilir.");
    }
}