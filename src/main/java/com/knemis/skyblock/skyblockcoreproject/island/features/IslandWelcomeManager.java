package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler; // IslandManager yerine
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.logging.Level;

public class IslandWelcomeManager {

    private final SkyBlockProject plugin;
    // Eski IslandManager alanı IslandDataHandler ile değiştirildi
    private final IslandDataHandler islandDataHandler;

    // Constructor güncellendi
    public IslandWelcomeManager(SkyBlockProject plugin, IslandDataHandler islandDataHandler) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
    }

    /**
     * Bir adanın karşılama mesajını ayarlar.
     * @param player Komutu kullanan oyuncu (mesaj göndermek için).
     * @param island Mesajı ayarlanacak ada.
     * @param message Ayarlanacak mesaj. Boş veya null ise mesaj silinir.
     */
    public void setWelcomeMessage(Player player, Island island, String message) {
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Mesaj ayarlamak için bir adanız olmalı.");
            return;
        }

        // Mesaj uzunluğu kontrolü (config'den alınabilir)
        int maxLength = plugin.getConfig().getInt("island.welcome-message.max-length", 100); // [cite: 10]
        if (message != null && message.length() > maxLength) {
            player.sendMessage(ChatColor.RED + "Karşılama mesajı maksimum " + maxLength + " karakter olabilir.");
            return;
        }

        String oldMessage = island.getWelcomeMessage();
        island.setWelcomeMessage(message); // Island nesnesindeki mesajı güncelle

        // Ada verisini IslandDataHandler üzerinden kaydet
        islandDataHandler.addOrUpdateIslandData(island);
        try {
            islandDataHandler.saveChangesToDisk(); // Değişiklikleri diske yaz

            if (message == null || message.isEmpty()) {
                if (oldMessage != null && !oldMessage.isEmpty()) { // Sadece önceden mesaj varsa silindi mesajı gönder
                    player.sendMessage(ChatColor.GREEN + "Ada karşılama mesajınız başarıyla silindi.");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Ayarlanacak bir karşılama mesajı girmediniz veya zaten mesajınız yoktu.");
                }
            } else {
                player.sendMessage(ChatColor.GREEN + "Ada karşılama mesajınız ayarlandı: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
            }
            plugin.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") adasının (" + island.getOwnerUUID() + ") karşılama mesajını güncelledi.");
        } catch (Exception e) { // Geniş tuttuk, saveChangesToDisk IOException atabilir veya başka bir runtime.
            player.sendMessage(ChatColor.RED + "Karşılama mesajı kaydedilirken bir hata oluştu.");
            plugin.getLogger().log(Level.SEVERE, "Ada karşılama mesajı kaydedilemedi (Sahip: " + island.getOwnerUUID() + "): " + e.getMessage(), e);
        }
    }

    /**
     * Bir adanın karşılama mesajını temizler.
     * @param player Komutu kullanan oyuncu.
     * @param island Mesajı temizlenecek ada.
     */
    public void clearWelcomeMessage(Player player, Island island) {
        setWelcomeMessage(player, island, null); // Null mesaj göndermek silme işlemi yapar
    }

    /**
     * Bir adanın karşılama mesajını alır.
     * @param island Mesajı alınacak ada.
     * @return Karşılama mesajı veya mesaj yoksa null.
     */
    public String getWelcomeMessage(Island island) {
        if (island != null) {
            return island.getWelcomeMessage();
        }
        return null;
    }

    /**
     * Oyuncuya adasının mevcut karşılama mesajını gösterir.
     * @param player Oyuncu
     * @param island Adanın sahibi olduğu ada
     */
    public void viewWelcomeMessage(Player player, Island island) {
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Görüntülenecek bir adanız yok.");
            return;
        }
        String message = island.getWelcomeMessage();
        if (message == null || message.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Şu anda ayarlanmış bir ada karşılama mesajınız yok.");
            player.sendMessage(ChatColor.GRAY + "Ayarlamak için: /island welcome set <mesaj>");
        } else {
            player.sendMessage(ChatColor.GOLD + "--- Ada Karşılama Mesajınız ---");
            player.sendMessage(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}