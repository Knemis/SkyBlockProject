package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List; // List için import eklendi
import java.util.Map;
import java.util.UUID;

public class IslandCommand implements CommandExecutor {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;
    private final Map<UUID, Long> createCooldowns;
    private final long CREATE_COOLDOWN_SECONDS;

    // Ada silme onayı için değişkenler (bunlar kalıyor)
    private final Map<UUID, Long> deleteConfirmations;
    private final long DELETE_CONFIRM_TIMEOUT_SECONDS = 30;

    private final Map<UUID, Long> resetConfirmations; // Oyuncu UUID'si -> Onaylama isteği zaman damgası
    private final long RESET_CONFIRM_TIMEOUT_SECONDS = 30;

    public IslandCommand(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.createCooldowns = new HashMap<>();
        this.CREATE_COOLDOWN_SECONDS = plugin.getConfig().getLong("island-creation-cooldown-seconds", 300);
        this.deleteConfirmations = new HashMap<>();
        this.resetConfirmations = new HashMap<>(); // Yeni map'i başlat
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Yardım mesajını güncelle
            player.sendMessage(ChatColor.GOLD + "--- Skyblock Komutları ---");
            player.sendMessage(ChatColor.YELLOW + "/island create" + ChatColor.GRAY + " - Yeni ada oluştur.");
            player.sendMessage(ChatColor.YELLOW + "/island go" + ChatColor.GRAY + " - Adanın ana spawn noktasına git.");
            player.sendMessage(ChatColor.YELLOW + "/island sethome <isim>" + ChatColor.GRAY + " - Ev belirle.");
            player.sendMessage(ChatColor.YELLOW + "/island home <isim|list|delete <isim>>" + ChatColor.GRAY + " - Ev işlemleri.");
            player.sendMessage(ChatColor.YELLOW + "/island reset" + ChatColor.GRAY + " - Adanı sıfırla (onay gerekir).");
            player.sendMessage(ChatColor.YELLOW + "/island delete" + ChatColor.GRAY + " - Adanı sil (onay gerekir).");
            player.sendMessage(ChatColor.YELLOW + "/island flags" + ChatColor.GRAY + " - Ada bayraklarını yönetmek için menü açar."); // YENİ
            return true;
        }


        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (createCooldowns.containsKey(player.getUniqueId())) {
                    long timeElapsedMillis = System.currentTimeMillis() - createCooldowns.get(player.getUniqueId());
                    long cooldownMillis = CREATE_COOLDOWN_SECONDS * 1000;
                    if (timeElapsedMillis < cooldownMillis) {
                        long secondsLeft = (cooldownMillis - timeElapsedMillis) / 1000;
                        player.sendMessage(ChatColor.RED + "Bu komutu tekrar kullanmak için " + secondsLeft + " saniye beklemelisiniz.");
                        return true;
                    } else {
                        createCooldowns.remove(player.getUniqueId());
                    }
                }

                if (islandManager.playerHasIsland(player)) {
                    islandManager.createIsland(player); // Bu sadece "Zaten bir adanız var!" mesajını gösterecek.
                } else {
                    createCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    islandManager.createIsland(player); // Asıl ada oluşturma işlemi
                }
                break;

            case "go":
            case "spawn": // Bu komutlar ana ada spawn noktasına ışınlar
                islandManager.teleportToIsland(player);
                break;

            case "delete":
            case "sil":
                if (args.length == 1) {
                    if (!islandManager.playerHasIsland(player)) {
                        player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
                        return true;
                    }
                    deleteConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
                    player.sendMessage(ChatColor.YELLOW + "Adanı silmek istediğinden emin misin? Onaylamak için " +
                            ChatColor.GOLD + "/island delete confirm" + ChatColor.YELLOW + " yaz. Bu işlem geri alınamaz!");
                    player.sendMessage(ChatColor.GRAY + "(Onaylama isteğin " + DELETE_CONFIRM_TIMEOUT_SECONDS + " saniye sonra zaman aşımına uğrayacak.)");

                    new org.bukkit.scheduler.BukkitRunnable() { // BukkitRunnable tam yoluyla belirtildi
                        @Override
                        public void run() {
                            if (deleteConfirmations.remove(player.getUniqueId()) != null) {
                                player.sendMessage(ChatColor.RED + "Ada silme onaylama isteğin zaman aşımına uğradı.");
                            }
                        }
                    }.runTaskLater(plugin, DELETE_CONFIRM_TIMEOUT_SECONDS * 20L);

                } else if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    Long requestTime = deleteConfirmations.remove(player.getUniqueId()); // İstekten sonra kaldır
                    if (requestTime == null) {
                        player.sendMessage(ChatColor.RED + "Onaylanacak aktif bir ada silme isteğin bulunmuyor veya isteğin zaman aşımına uğramış.");
                        return true;
                    }
                    if ((System.currentTimeMillis() - requestTime) > (DELETE_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                        player.sendMessage(ChatColor.RED + "Ada silme onaylama isteğin zaman aşımına uğramış.");
                        return true;
                    }

                    boolean deleted = islandManager.deleteIsland(player);
                    if (deleted) {
                        player.sendMessage(ChatColor.GREEN + "Adan başarıyla silindi.");
                        createCooldowns.remove(player.getUniqueId());
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island delete veya /island delete confirm");
                }
                break;

            case "sethome":
                if (!islandManager.playerHasIsland(player)) {
                    player.sendMessage(ChatColor.RED + "Ev noktanı ayarlayabileceğin bir adan yok!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island sethome <ev_ismi>");
                    return true;
                }
                String homeNameToSet = args[1];
                islandManager.setNamedHome(player, homeNameToSet, player.getLocation());
                // IslandManager.setNamedHome zaten başarı/hata mesajını oyuncuya gönderiyor.
                break;

            case "home": // Bu komut artık list, delete <isim> ve <isim> (ışınlanma) işlemlerini yönetecek
                if (!islandManager.playerHasIsland(player)) {
                    player.sendMessage(ChatColor.RED + "Bu komutu kullanabilmek için bir adanız olmalı!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island home <ev_ismi>, /island home list, veya /island home delete <ev_ismi>");
                    return true;
                }

                String homeActionOrName = args[1].toLowerCase();

                if (homeActionOrName.equals("list")) {
                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "Kullanım: /island home list");
                        return true;
                    }
                    List<String> homes = islandManager.getNamedHomesList(player);
                    int maxHomes = plugin.getConfig().getInt("island.max-named-homes", 5);
                    if (homes.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + "Ayarlanmış hiç ev noktan yok. " + ChatColor.GRAY + "(Maks: " + maxHomes + ")");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Ev Noktaların (" + homes.size() + "/" + maxHomes + "):");
                        StringBuilder homesList = new StringBuilder();
                        for (int i = 0; i < homes.size(); i++) {
                            homesList.append(ChatColor.GOLD).append(homes.get(i));
                            if (i < homes.size() - 1) {
                                homesList.append(ChatColor.GRAY).append(", ");
                            }
                        }
                        player.sendMessage(homesList.toString());
                    }
                } else if (homeActionOrName.equals("delete") || homeActionOrName.equals("sil")) { // YENİ KISIM
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Kullanım: /island home delete <ev_ismi>");
                        return true;
                    }
                    String homeNameToDelete = args[2];
                    islandManager.deleteNamedHome(player, homeNameToDelete);
                    // islandManager.deleteNamedHome zaten başarı/hata mesajını oyuncuya gönderiyor.
                } else { // Eğer "list" veya "delete" değilse, bunun bir ev ismi olduğunu varsay (ışınlanma için)
                    if (args.length != 2) { // Sadece /island home <isim> olmalı
                        player.sendMessage(ChatColor.RED + "Kullanım: /island home <ev_ismi>");
                        return true;
                    }
                    String homeNameToTeleport = homeActionOrName; // args[1] ev ismidir
                    islandManager.teleportToNamedHome(player, homeNameToTeleport);
                    // islandManager.teleportToNamedHome zaten başarı/hata mesajını oyuncuya gönderiyor.
                }
                break;
            case "reset": // YENİ ALT KOMUT
            case "sıfırla": // Alias
                if (args.length == 1) { // Sadece /island reset yazıldıysa
                    if (!islandManager.playerHasIsland(player)) {
                        player.sendMessage(ChatColor.RED + "Sıfırlayabileceğin bir adan yok!");
                        return true;
                    }
                    resetConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
                    player.sendMessage(ChatColor.YELLOW + "Adanı sıfırlamak istediğinden emin misin? Bu işlem adandaki her şeyi silip adayı başlangıç haline döndürecek ve TÜM EV NOKTALARINI SİLECEKTİR. Onaylamak için " +
                            ChatColor.GOLD + "/island reset confirm" + ChatColor.YELLOW + " yaz. Bu işlem geri alınamaz!");
                    player.sendMessage(ChatColor.GRAY + "(Onaylama isteğin " + RESET_CONFIRM_TIMEOUT_SECONDS + " saniye sonra zaman aşımına uğrayacak.)");

                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            if (resetConfirmations.remove(player.getUniqueId()) != null) {
                                player.sendMessage(ChatColor.RED + "Ada sıfırlama onaylama isteğin zaman aşımına uğradı.");
                            }
                        }
                    }.runTaskLater(plugin, RESET_CONFIRM_TIMEOUT_SECONDS * 20L);

                } else if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) { // /island reset confirm
                    Long requestTime = resetConfirmations.remove(player.getUniqueId());
                    if (requestTime == null) {
                        player.sendMessage(ChatColor.RED + "Onaylanacak aktif bir ada sıfırlama isteğin bulunmuyor veya isteğin zaman aşımına uğramış.");
                        return true;
                    }
                    if ((System.currentTimeMillis() - requestTime) > (RESET_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                        player.sendMessage(ChatColor.RED + "Ada sıfırlama onaylama isteğin zaman aşımına uğramış.");
                        return true;
                    }

                    boolean resetSuccess = islandManager.resetIsland(player);
                    if (resetSuccess) {
                        // islandManager.resetIsland() zaten mesaj gönderiyor ve ışınlıyor.
                        // İsteğe bağlı: createCooldowns.remove(player.getUniqueId()); // Yeni ada için cooldown'ı sıfırla
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island reset veya /island reset confirm");
                }
                break;
            case "flags": // YENİ ALT KOMUT
            case "bayraklar": // Alias
                if (!islandManager.playerHasIsland(player)) {
                    player.sendMessage(ChatColor.RED + "Bayraklarını düzenleyebileceğin bir adan yok!");
                    return true;
                }
                // Burada IslandManager veya yeni bir GUI yönetim sınıfı üzerinden GUI açılacak.
                // Şimdilik placeholder:
                plugin.getFlagGUIManager().openFlagsGUI(player); // FlagGUIManager adında yeni bir sınıfımız olacak varsayalım
                break;

            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen alt komut: " + subCommand);
                player.sendMessage(ChatColor.YELLOW + "Kullanım için /island yazınız."); // Ana /island komutuna yönlendir
                break;
        }
        return true;
    }
}