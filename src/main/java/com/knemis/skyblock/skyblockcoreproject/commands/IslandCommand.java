package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location; // BU IMPORT EKLECEK VEYA KONTROL EDİLECEK
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;


public class IslandCommand implements CommandExecutor, TabCompleter {

    private final SkyBlockProject plugin;
    private final IslandManager islandManager;
    private final Map<UUID, Long> createCooldowns;
    private final long CREATE_COOLDOWN_SECONDS;

    private final Map<UUID, Long> deleteConfirmations;
    private final long DELETE_CONFIRM_TIMEOUT_SECONDS = 30;

    private final Map<UUID, Long> resetConfirmations;
    private final long RESET_CONFIRM_TIMEOUT_SECONDS = 30;

    public IslandCommand(SkyBlockProject plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.createCooldowns = new HashMap<>();
        this.CREATE_COOLDOWN_SECONDS = plugin.getConfig().getLong("island.creation-cooldown-seconds", 300);
        this.deleteConfirmations = new HashMap<>();
        this.resetConfirmations = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreateCommand(player);
                break;
            case "go":
            case "home":
                handleHomeCommand(player, args);
                break;
            case "sethome":
                handleSetHomeCommand(player, args);
                break;
            case "delhome":
                handleDelHomeCommand(player, args);
                break;
            case "delete":
            case "sil":
                handleDeleteCommand(player, args);
                break;
            case "reset":
            case "sıfırla":
                handleResetCommand(player, args);
                break;
            case "flags":
            case "bayraklar":
                handleFlagsCommand(player);
                break;
            case "info":
            case "bilgi":
                handleInfoCommand(player, args);
                break;
            case "settings":
            case "ayarlar":
                handleSettingsCommand(player, args);
                break;
            case "team":
            case "takım":
            case "uye":
                handleTeamCommand(player, args);
                break;
            case "visit":
            case "ziyaret":
                handleVisitCommand(player, args);
                break;
            case "help":
            case "yardim":
                sendHelpMessage(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen alt komut: " + subCommand);
                sendHelpMessage(player);
                break;
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Skyblock Komutları ---");
        player.sendMessage(ChatColor.YELLOW + "/island create" + ChatColor.GRAY + " - Yeni ada oluştur.");
        player.sendMessage(ChatColor.YELLOW + "/island go" + ChatColor.GRAY + " - Adanın ana spawn noktasına git.");
        player.sendMessage(ChatColor.YELLOW + "/island sethome <isim>" + ChatColor.GRAY + " - Ada içinde ev belirle.");
        player.sendMessage(ChatColor.YELLOW + "/island home <isim>" + ChatColor.GRAY + " - Belirtilen evine git.");
        player.sendMessage(ChatColor.YELLOW + "/island home list" + ChatColor.GRAY + " - Evlerini listele.");
        player.sendMessage(ChatColor.YELLOW + "/island delhome <isim>" + ChatColor.GRAY + " - Belirtilen evini sil.");
        player.sendMessage(ChatColor.YELLOW + "/island flags" + ChatColor.GRAY + " - Ada bayraklarını yönet (ziyaretçiler için).");
        player.sendMessage(ChatColor.YELLOW + "/island info [oyuncu]" + ChatColor.GRAY + " - Kendi adanın veya başkasının adasının bilgilerini gör.");
        player.sendMessage(ChatColor.YELLOW + "/island settings name <yeni_isim>" + ChatColor.GRAY + " - Adanın ismini değiştir.");
        player.sendMessage(ChatColor.YELLOW + "/island settings visibility <public|private>" + ChatColor.GRAY + " - Adanın ziyaretçi durumunu ayarla.");
        player.sendMessage(ChatColor.YELLOW + "/island settings boundary <on|off>" + ChatColor.GRAY + " - Ada sınırlarını aç/kapa.");
        player.sendMessage(ChatColor.YELLOW + "/island team add <oyuncu>" + ChatColor.GRAY + " - Adana üye ekle.");
        player.sendMessage(ChatColor.YELLOW + "/island team remove <oyuncu>" + ChatColor.GRAY + " - Adadan üye çıkar.");
        player.sendMessage(ChatColor.YELLOW + "/island team list" + ChatColor.GRAY + " - Ada üyelerini listele.");
        player.sendMessage(ChatColor.YELLOW + "/island visit <oyuncu_adı>" + ChatColor.GRAY + " - Başka bir oyuncunun adasını ziyaret et.");
        player.sendMessage(ChatColor.YELLOW + "/island reset" + ChatColor.GRAY + " - Adanı sıfırla (onay gerekir).");
        player.sendMessage(ChatColor.YELLOW + "/island delete" + ChatColor.GRAY + " - Adanı sil (onay gerekir).");
        player.sendMessage(ChatColor.YELLOW + "/island help" + ChatColor.GRAY + " - Bu yardım mesajını göster.");
    }

    private void handleCreateCommand(Player player) {
        if (createCooldowns.containsKey(player.getUniqueId())) {
            long timeElapsedMillis = System.currentTimeMillis() - createCooldowns.get(player.getUniqueId());
            long cooldownMillis = CREATE_COOLDOWN_SECONDS * 1000;
            if (timeElapsedMillis < cooldownMillis) {
                long secondsLeft = (cooldownMillis - timeElapsedMillis) / 1000;
                player.sendMessage(ChatColor.RED + "Bu komutu tekrar kullanmak için " + secondsLeft + " saniye beklemelisiniz.");
                return;
            } else {
                createCooldowns.remove(player.getUniqueId());
            }
        }

        if (islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Zaten bir adanız var! Sıfırlamak için " + ChatColor.GOLD + "/island reset" + ChatColor.RED + " kullanabilirsiniz.");
        } else {
            islandManager.createIsland(player);
            if (CREATE_COOLDOWN_SECONDS > 0) {
                createCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    private void handleHomeCommand(Player player, String[] args) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Önce bir ada oluşturmalısın: " + ChatColor.GOLD + "/island create");
            return;
        }
        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("spawn"))) {
            islandManager.teleportToIsland(player);
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                List<String> homes = islandManager.getNamedHomesList(player);
                int maxHomes = plugin.getConfig().getInt("island.max-named-homes", 5);
                if (homes.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Ayarlanmış hiç ev noktan yok. " + ChatColor.GRAY + "(Maks: " + maxHomes + ")");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Ev Noktaların (" + homes.size() + "/" + maxHomes + "): " + ChatColor.GOLD + String.join(ChatColor.GRAY + ", " + ChatColor.GOLD, homes));
                }
            } else {
                islandManager.teleportToNamedHome(player, args[1]);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island home [isim|list|spawn]");
        }
    }

    private void handleSetHomeCommand(Player player, String[] args) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Ev noktanı ayarlayabileceğin bir adan yok!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island sethome <ev_ismi>");
            return;
        }
        String homeNameToSet = args[1];
        islandManager.setNamedHome(player, homeNameToSet, player.getLocation());
    }

    private void handleDelHomeCommand(Player player, String[] args) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Ev silebilmek için önce bir adanız olmalı!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island delhome <ev_ismi>");
            return;
        }
        String homeNameToDelete = args[1];
        islandManager.deleteNamedHome(player, homeNameToDelete);
    }


    private void handleDeleteCommand(Player player, String[] args) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Silebileceğin bir adan yok!");
            return;
        }
        if (args.length == 1) {
            deleteConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatColor.YELLOW + "Adanı silmek istediğinden emin misin? Onaylamak için " +
                    ChatColor.GOLD + "/island delete confirm" + ChatColor.YELLOW + " yaz. Bu işlem geri alınamaz!");
            player.sendMessage(ChatColor.GRAY + "(Onaylama isteğin " + DELETE_CONFIRM_TIMEOUT_SECONDS + " saniye sonra zaman aşımına uğrayacak.)");

            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (deleteConfirmations.remove(player.getUniqueId()) != null) {
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.RED + "Ada silme onaylama isteğin zaman aşımına uğradı.");
                        }
                    }
                }
            }.runTaskLater(plugin, DELETE_CONFIRM_TIMEOUT_SECONDS * 20L);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            Long requestTime = deleteConfirmations.remove(player.getUniqueId());
            if (requestTime == null || (System.currentTimeMillis() - requestTime) > (DELETE_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                player.sendMessage(ChatColor.RED + "Onaylanacak aktif bir ada silme isteğin bulunmuyor veya isteğin zaman aşımına uğramış.");
                return;
            }
            if (islandManager.deleteIsland(player)) {
                createCooldowns.remove(player.getUniqueId());
            }
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island delete veya /island delete confirm");
        }
    }

    private void handleResetCommand(Player player, String[] args) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Sıfırlayabileceğin bir adan yok!");
            return;
        }
        if (args.length == 1) {
            resetConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatColor.YELLOW + "Adanı sıfırlamak istediğinden emin misin? Bu işlem adandaki her şeyi silip adayı başlangıç haline döndürecek ve TÜM EV NOKTALARINI SİLECEKTİR. Onaylamak için " +
                    ChatColor.GOLD + "/island reset confirm" + ChatColor.YELLOW + " yaz. Bu işlem geri alınamaz!");
            player.sendMessage(ChatColor.GRAY + "(Onaylama isteğin " + RESET_CONFIRM_TIMEOUT_SECONDS + " saniye sonra zaman aşımına uğrayacak.)");
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (resetConfirmations.remove(player.getUniqueId()) != null) {
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.RED + "Ada sıfırlama onaylama isteğin zaman aşımına uğradı.");
                        }
                    }
                }
            }.runTaskLater(plugin, RESET_CONFIRM_TIMEOUT_SECONDS * 20L);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            Long requestTime = resetConfirmations.remove(player.getUniqueId());
            if (requestTime == null || (System.currentTimeMillis() - requestTime) > (RESET_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                player.sendMessage(ChatColor.RED + "Onaylanacak aktif bir ada sıfırlama isteğin bulunmuyor veya isteğin zaman aşımına uğramış.");
                return;
            }
            islandManager.resetIsland(player);
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island reset veya /island reset confirm");
        }
    }

    private void handleFlagsCommand(Player player) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Bayraklarını düzenleyebileceğin bir adan yok!");
            return;
        }
        plugin.getFlagGUIManager().openFlagsGUI(player);
    }

    private void handleInfoCommand(Player player, String[] args) {
        Island islandToInfo;
        String islandOwnerName;

        if (args.length == 1) {
            islandToInfo = islandManager.getIsland(player);
            if (islandToInfo == null) {
                player.sendMessage(ChatColor.RED + "Görüntülenecek bir adan yok. Oluşturmak için: " + ChatColor.GOLD + "/island create");
                return;
            }
            islandOwnerName = player.getName();
        } else if (args.length == 2) {
            OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(args[1]);
            if ((!targetOwner.hasPlayedBefore() && !targetOwner.isOnline()) && targetOwner.getUniqueId() == null) {
                player.sendMessage(ChatColor.RED + "'" + args[1] + "' adında bir oyuncu bulunamadı veya bu oyuncunun hiç ada verisi yok.");
                return;
            }
            islandToInfo = islandManager.getIslandByOwner(targetOwner.getUniqueId());
            if (islandToInfo == null) {
                player.sendMessage(ChatColor.RED + "'" + args[1] + "' adlı oyuncunun bir adası bulunmuyor.");
                return;
            }
            islandOwnerName = targetOwner.getName();
            if (islandOwnerName == null) islandOwnerName = args[1];
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island info [oyuncu_adı]");
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(new Locale("tr", "TR"))
                .withZone(ZoneId.systemDefault());

        player.sendMessage(ChatColor.GOLD + "--- Ada Bilgileri: " + ChatColor.AQUA + islandToInfo.getIslandName() + ChatColor.GOLD + " ---");
        player.sendMessage(ChatColor.YELLOW + "Sahibi: " + ChatColor.WHITE + islandOwnerName);
        player.sendMessage(ChatColor.YELLOW + "Oluşturulma Tarihi: " + ChatColor.WHITE + formatter.format(islandToInfo.getCreationDate()));
        player.sendMessage(ChatColor.YELLOW + "Ziyaret Durumu: " + (islandToInfo.isPublic() ? ChatColor.GREEN + "Herkese Açık" : ChatColor.RED + "Özel"));
        player.sendMessage(ChatColor.YELLOW + "Sınırlar: " + (islandToInfo.areBoundariesEnforced() ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Pasif"));

        Location baseLoc = islandToInfo.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) { // getWorld() null kontrolü
            player.sendMessage(ChatColor.YELLOW + "Konum (Merkez): " + ChatColor.WHITE + "X: " + baseLoc.getBlockX() + ", Y: " + baseLoc.getBlockY() + ", Z: " + baseLoc.getBlockZ() + ChatColor.GRAY + " (" + baseLoc.getWorld().getName() + ")");
        }


        List<OfflinePlayer> members = islandManager.getIslandMembers(islandToInfo.getOwnerUUID());
        if (members.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Üyeler: " + ChatColor.GRAY + "Yok");
        } else {
            String memberNames = members.stream().map(OfflinePlayer::getName).collect(Collectors.joining(", "));
            player.sendMessage(ChatColor.YELLOW + "Üyeler (" + members.size() + "): " + ChatColor.WHITE + memberNames);
        }
        player.sendMessage(ChatColor.YELLOW + "Ev Sayısı: " + ChatColor.WHITE + islandToInfo.getNamedHomes().size() + "/" + plugin.getConfig().getInt("island.max-named-homes", 5));
    }

    private void handleSettingsCommand(Player player, String[] args) {
        if (!islandManager.playerHasIsland(player)) {
            player.sendMessage(ChatColor.RED + "Ayarlarını düzenleyebileceğin bir adan yok!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island settings <name|visibility|boundary> [değer]");
            return;
        }
        String settingType = args[1].toLowerCase();
        switch (settingType) {
            case "name":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island settings name <yeni_isim>");
                    return;
                }
                String newName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                islandManager.setIslandName(player, newName);
                break;
            case "visibility":
            case "gizlilik":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island settings visibility <public|private>");
                    return;
                }
                String visibility = args[2].toLowerCase();
                if (visibility.equals("public") || visibility.equals("herkeseacik")) {
                    islandManager.setIslandPublic(player, true);
                } else if (visibility.equals("private") || visibility.equals("ozel")) {
                    islandManager.setIslandPublic(player, false);
                } else {
                    player.sendMessage(ChatColor.RED + "Geçersiz görünürlük türü. Kullanılabilir: public, private");
                }
                break;
            case "boundary":
            case "sınır":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island settings boundary <on|off>");
                    return;
                }
                String boundaryState = args[2].toLowerCase();
                Island currentIsland = islandManager.getIsland(player);
                if (currentIsland == null) return;

                if(boundaryState.equals("on") || boundaryState.equals("aktif")) {
                    if(!currentIsland.areBoundariesEnforced()){
                        islandManager.toggleIslandBoundaries(player);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Ada sınırları zaten aktif.");
                    }
                } else if (boundaryState.equals("off") || boundaryState.equals("pasif")) {
                    if(currentIsland.areBoundariesEnforced()){
                        islandManager.toggleIslandBoundaries(player);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Ada sınırları zaten pasif.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Geçersiz sınır durumu. Kullanılabilir: on, off");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen ayar türü. Kullanılabilir: name, visibility, boundary");
                break;
        }
    }

    private void handleTeamCommand(Player player, String[] args) {
        Island island = islandManager.getIsland(player);
        if (island == null && !(args.length > 1 && (args[1].equalsIgnoreCase("accept") || args[1].equalsIgnoreCase("deny")) ) ) {
            player.sendMessage(ChatColor.RED + "Takım komutlarını kullanabilmek için bir adanız olmalı.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island team <add|remove|list>");
            return;
        }
        String teamAction = args[1].toLowerCase(); // args[0] "team", args[1] action
        switch (teamAction) {
            case "add":
            case "ekle":
                if (args.length < 3) { // args[2] is player name
                    player.sendMessage(ChatColor.RED + "Kullanım: /island team add <oyuncu_adı>");
                    return;
                }
                OfflinePlayer targetToAdd = Bukkit.getOfflinePlayer(args[2]);
                if ((!targetToAdd.hasPlayedBefore() && !targetToAdd.isOnline()) && targetToAdd.getUniqueId() == null) {
                    player.sendMessage(ChatColor.RED + "'" + args[2] + "' adında bir oyuncu bulunamadı.");
                    return;
                }
                islandManager.addIslandMember(player, targetToAdd);
                break;
            case "remove":
            case "kick":
            case "at":
            case "çıkar":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island team remove <oyuncu_adı>");
                    return;
                }
                OfflinePlayer targetToRemove = Bukkit.getOfflinePlayer(args[2]);
                // OfflinePlayer.hasPlayedBefore() her zaman bir UUID döndürmeyebilir veya null olabilir.
                // Bu yüzden targetToRemove.getUniqueId() null kontrolü yapılmalı.
                boolean canFindPlayer = targetToRemove.getUniqueId() != null && (targetToRemove.isOnline() || targetToRemove.hasPlayedBefore());
                boolean isMember = islandManager.getIsland(player) != null && islandManager.getIsland(player).isMember(targetToRemove.getUniqueId());

                if (!canFindPlayer && !isMember) {
                    player.sendMessage(ChatColor.RED + "'" + args[2] + "' adında bir üye bulunamadı veya oyuncu hiç oynamamış.");
                    return;
                }
                if (!isMember) { // Oyuncu var ama üye değilse
                    player.sendMessage(ChatColor.RED + "'" + args[2] + "' adında bir üyeniz bulunmuyor.");
                    return;
                }
                islandManager.removeIslandMember(player, targetToRemove);
                break;
            case "list":
            case "liste":
                if (island == null) {
                    player.sendMessage(ChatColor.RED + "Listelenecek bir adan yok."); // Bu durum zaten yukarıda yakalanıyor
                    return;
                }
                List<OfflinePlayer> members = islandManager.getIslandMembers(player.getUniqueId());
                if (members.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Adanızda hiç üye yok.");
                } else {
                    String memberNames = members.stream()
                            .map(op -> op.getName() != null ? op.getName() : op.getUniqueId().toString().substring(0,8)) // Eğer ismi yoksa UUID'nin bir kısmını göster
                            .collect(Collectors.joining(ChatColor.GRAY + ", " + ChatColor.WHITE));
                    player.sendMessage(ChatColor.GREEN + "Ada Üyeleri ("+ members.size() + "/" + plugin.getConfig().getInt("island.max-members",3) +"): " + ChatColor.WHITE + memberNames);
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen takım komutu. Kullanılabilir: add, remove, list");
                break;
        }
    }

    private void handleVisitCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island visit <oyuncu_adı>");
            return;
        }
        String ownerName = args[1];
        OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(ownerName);

        if ((!targetOwner.hasPlayedBefore() && !targetOwner.isOnline()) && targetOwner.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "'" + ownerName + "' adında bir oyuncu bulunamadı veya hiç oynamamış.");
            return;
        }

        Island targetIsland = islandManager.getIslandByOwner(targetOwner.getUniqueId());
        if (targetIsland == null) {
            player.sendMessage(ChatColor.RED + "'" + ownerName + "' adlı oyuncunun bir adası bulunmuyor.");
            return;
        }

        if (targetIsland.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Zaten kendi adandasın. Gitmek için " + ChatColor.GOLD + "/island go" + ChatColor.YELLOW + " kullan.");
            return;
        }

        if (!targetIsland.isPublic() && !targetIsland.isMember(player.getUniqueId()) ) {
            player.sendMessage(ChatColor.RED + "'" + ownerName + "' adlı oyuncunun adası özel ve ziyaret edilemez.");
            return;
        }
        if(targetIsland.isBanned(player.getUniqueId()) ) {
            player.sendMessage(ChatColor.RED + "Bu adadan yasaklanmışsın!");
            return;
        }

        Location teleportLocation = targetIsland.getBaseLocation();
        if (teleportLocation == null || teleportLocation.getWorld() == null) { // getWorld() null kontrolü
            player.sendMessage(ChatColor.RED + "Bu adanın konumu bulunamadı veya dünya yüklenemedi.");
            return;
        }
        double offsetX = plugin.getConfig().getDouble("island-spawn-offset.x", 0.5);
        double offsetY = plugin.getConfig().getDouble("island-spawn-offset.y", 1.5);
        double offsetZ = plugin.getConfig().getDouble("island-spawn-offset.z", 0.5);
        Location actualTeleportLoc = teleportLocation.clone().add(offsetX, offsetY, offsetZ);
        actualTeleportLoc.setYaw(0f);
        actualTeleportLoc.setPitch(0f);

        if (actualTeleportLoc.getChunk() != null && !actualTeleportLoc.getChunk().isLoaded()) { // Chunk null kontrolü
            actualTeleportLoc.getChunk().load();
        }
        player.teleport(actualTeleportLoc); // player.teleport(Location) doğru kullanım
        player.sendMessage(ChatColor.GREEN + ownerName + " adlı oyuncunun adasına ışınlandın!");
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        List<String> subCommands = Arrays.asList("create", "go", "sethome", "home", "delhome", "delete", "reset", "flags", "info", "settings", "team", "visit", "help");

        if (args.length == 1) {
            for (String sc : subCommands) {
                if (sc.startsWith(args[0].toLowerCase())) {
                    completions.add(sc);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("home") || subCmd.equals("delhome") || subCmd.equals("sethome")) {
                if (islandManager.playerHasIsland(player)) {
                    List<String> homes = new ArrayList<>(islandManager.getNamedHomesList(player)); // Değiştirilebilir liste için
                    if (subCmd.equals("home")) homes.add("list");
                    for (String home : homes) {
                        if (home.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(home);
                        }
                    }
                }
            } else if (subCmd.equals("settings")) {
                List<String> settingsOptions = Arrays.asList("name", "visibility", "boundary");
                for (String opt : settingsOptions) {
                    if (opt.startsWith(args[1].toLowerCase())) {
                        completions.add(opt);
                    }
                }
            } else if (subCmd.equals("team")) {
                List<String> teamOptions = Arrays.asList("add", "remove", "list");
                for (String opt : teamOptions) {
                    if (opt.startsWith(args[1].toLowerCase())) {
                        completions.add(opt);
                    }
                }
            } else if (subCmd.equals("info") || subCmd.equals("visit")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                });
                // Offline oyuncuları da önermek için daha karmaşık bir yapı gerekebilir (config'den son girenler vb.)
            } else if (subCmd.equals("delete") || subCmd.equals("reset")) {
                if ("confirm".startsWith(args[1].toLowerCase())) {
                    completions.add("confirm");
                }
            }
        }

        if (args.length == 3) {
            String subCmd = args[0].toLowerCase();
            String settingType = args[1].toLowerCase();
            if(subCmd.equals("settings")) {
                if(settingType.equals("visibility")) {
                    if("public".startsWith(args[2].toLowerCase())) completions.add("public");
                    if("private".startsWith(args[2].toLowerCase())) completions.add("private");
                } else if (settingType.equals("boundary")) {
                    if("on".startsWith(args[2].toLowerCase())) completions.add("on");
                    if("off".startsWith(args[2].toLowerCase())) completions.add("off");
                }
            } else if (subCmd.equals("team") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!p.equals(player) && p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(p.getName());
                    }
                });
            }
        }
        return completions;
    }
}