package com.knemis.skyblock.skyblockcoreproject.island.features;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;

public class IslandWelcomeManager {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;

    public IslandWelcomeManager(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    /**
     * Bir adanın karşılama mesajını ayarlar.
     * @param island Mesajı ayarlanacak ada.
     * @param message Ayarlanacak mesaj. Boş veya null ise mesaj silinir.
     * @param player Komutu kullanan oyuncu (mesaj göndermek için).
     */
    public void setWelcomeMessage(Player player, Island island, String message) {
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Mesaj ayarlamak için bir adanız olmalı.");
            return;
        }

        // Mesaj uzunluğu kontrolü (config'den alınabilir)
        int maxLength = plugin.getConfig().getInt("island.welcome-message.max-length", 100);
        if (message != null && message.length() > maxLength) {
            player.sendMessage(ChatColor.RED + "Karşılama mesajı maksimum " + maxLength + " karakter olabilir.");
            return;
        }

        String oldMessage = island.getWelcomeMessage();
        island.setWelcomeMessage(message); // Null ise mesajı temizler (Island sınıfında bu şekilde ayarlanmalı)
        islandManager.saveIslandData(island); // IslandManager değişikliği kaydeder
        try {
            islandManager.getIslandsConfig().save(islandManager.getIslandsFile());
            if (message == null || message.isEmpty()) {
                if (oldMessage != null && !oldMessage.isEmpty()) { // Sadece önceden mesaj varsa silindi mesajı gönder
                    player.sendMessage(ChatColor.GREEN + "Ada karşılama mesajınız başarıyla silindi.");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Ayarlanacak bir karşılama mesajı girmediniz veya zaten mesajınız yoktu.");
                }
            } else {
                player.sendMessage(ChatColor.GREEN + "Ada karşılama mesajınız ayarlandı: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
            }
            plugin.getLogger().info(player.getName() + " adasının (" + island.getOwnerUUID() + ") karşılama mesajını güncelledi.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Karşılama mesajı kaydedilirken bir hata oluştu.");
            plugin.getLogger().severe("Ada karşılama mesajı kaydedilemedi (Sahip: " + island.getOwnerUUID() + "): " + e.getMessage());
        }
    }

    /**
     * Bir adanın karşılama mesajını temizler.
     * @param island Mesajı temizlenecek ada.
     * @param player Komutu kullanan oyuncu.
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