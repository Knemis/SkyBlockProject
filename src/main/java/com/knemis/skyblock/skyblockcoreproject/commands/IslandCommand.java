package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.gui.FlagGUIManager;
import com.knemis.skyblock.skyblockcoreproject.island.Island;
import com.knemis.skyblock.skyblockcoreproject.island.IslandDataHandler;
import com.knemis.skyblock.skyblockcoreproject.island.IslandLifecycleManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandMemberManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandSettingsManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandTeleportManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandBiomeManager;
import com.knemis.skyblock.skyblockcoreproject.island.features.IslandWelcomeManager;


import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import com.sk89q.worldedit.math.BlockVector3;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IslandCommand implements CommandExecutor, TabCompleter {

    private final SkyBlockProject plugin;
    private final IslandDataHandler islandDataHandler;
    private final IslandLifecycleManager islandLifecycleManager;
    private final IslandSettingsManager islandSettingsManager;
    private final IslandMemberManager islandMemberManager;
    private final IslandTeleportManager islandTeleportManager;
    private final IslandBiomeManager islandBiomeManager;
    private final IslandWelcomeManager islandWelcomeManager;
    private final FlagGUIManager flagGUIManager;
    private final Economy economy;


    // Bu alanlar komutlar arası durumu tuttuğu için sınıf üyesi olmalıdır.
    // IDE uyarısı (Field can be converted to a local variable) bu bağlamda göz ardı edilebilir.
    private final Map<UUID, Long> createCooldowns;
    private final long CREATE_COOLDOWN_SECONDS;
    private final Map<UUID, Long> deleteConfirmations;
    private final long DELETE_CONFIRM_TIMEOUT_SECONDS = 30;
    private final Map<UUID, Long> resetConfirmations;
    private final long RESET_CONFIRM_TIMEOUT_SECONDS = 30;

    public IslandCommand(SkyBlockProject plugin,
                         IslandDataHandler islandDataHandler,
                         IslandLifecycleManager islandLifecycleManager,
                         IslandSettingsManager islandSettingsManager,
                         IslandMemberManager islandMemberManager,
                         IslandTeleportManager islandTeleportManager,
                         IslandBiomeManager islandBiomeManager,
                         IslandWelcomeManager islandWelcomeManager,
                         FlagGUIManager flagGUIManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandLifecycleManager = islandLifecycleManager;
        this.islandSettingsManager = islandSettingsManager;
        this.islandMemberManager = islandMemberManager;
        this.islandTeleportManager = islandTeleportManager;
        this.islandBiomeManager = islandBiomeManager;
        this.islandWelcomeManager = islandWelcomeManager;
        this.flagGUIManager = flagGUIManager;
        this.economy = plugin.getEconomy(); // Ekonomi nesnesini al

        this.createCooldowns = new HashMap<>();
        this.CREATE_COOLDOWN_SECONDS = plugin.getConfig().getLong("island.creation-cooldown-seconds", 300);
        this.deleteConfirmations = new HashMap<>();
        this.resetConfirmations = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // ... (metodun başı aynı) ...
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
            case "biome":
                handleBiomeCommand(player, args);
                break;
            case "welcome":
                handleWelcomeCommand(player, args);
                break;
            case "upgrade": // YENİ
                handleUpgradeCommand(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen alt komut: " + subCommand);
                sendHelpMessage(player);
                break;
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        // ... (yardım mesajları aynı) ...
        player.sendMessage(ChatColor.GOLD + "--- Skyblock Komutları ---");
        player.sendMessage(ChatColor.YELLOW + "/island create" + ChatColor.GRAY + " - Yeni ada oluştur.");
        player.sendMessage(ChatColor.YELLOW + "/island go" + ChatColor.GRAY + " - Adanın ana spawn noktasına git.");
        player.sendMessage(ChatColor.YELLOW + "/island sethome <isim>" + ChatColor.GRAY + " - Ada içinde ev belirle.");
        player.sendMessage(ChatColor.YELLOW + "/island home <isim>" + ChatColor.GRAY + " - Belirtilen evine git.");
        player.sendMessage(ChatColor.YELLOW + "/island home list" + ChatColor.GRAY + " - Evlerini listele.");
        player.sendMessage(ChatColor.YELLOW + "/island delhome <isim>" + ChatColor.GRAY + " - Belirtilen evini sil.");
        player.sendMessage(ChatColor.YELLOW + "/island biome <set|get|list> [biyom]" + ChatColor.GRAY + " - Ada biyomunu yönet.");
        player.sendMessage(ChatColor.YELLOW + "/island welcome <set|clear|view> [mesaj]" + ChatColor.GRAY + " - Ada karşılama mesajını yönet.");
        player.sendMessage(ChatColor.YELLOW + "/island flags" + ChatColor.GRAY + " - Ada bayraklarını yönet (Genel).");
        player.sendMessage(ChatColor.YELLOW + "/island info [oyuncu]" + ChatColor.GRAY + " - Kendi adanın veya başkasının adasının bilgilerini gör.");
        player.sendMessage(ChatColor.YELLOW + "/island settings name <yeni_isim>" + ChatColor.GRAY + " - Adanın ismini değiştir.");
        player.sendMessage(ChatColor.YELLOW + "/island settings visibility <public|private>" + ChatColor.GRAY + " - Adanın ziyaretçi durumunu ayarla.");
        player.sendMessage(ChatColor.YELLOW + "/island settings boundary" + ChatColor.GRAY + " - Ada sınırlarını aç/kapa (toggle).");
        player.sendMessage(ChatColor.YELLOW + "/island team add <oyuncu>" + ChatColor.GRAY + " - Adana üye ekle.");
        player.sendMessage(ChatColor.YELLOW + "/island team remove <oyuncu>" + ChatColor.GRAY + " - Adadan üye çıkar.");
        player.sendMessage(ChatColor.YELLOW + "/island team list" + ChatColor.GRAY + " - Ada üyelerini listele.");
        player.sendMessage(ChatColor.YELLOW + "/island upgrade homes" + ChatColor.GRAY + " - Maksimum ev sayını artır.");
        player.sendMessage(ChatColor.YELLOW + "/island visit <oyuncu_adı>" + ChatColor.GRAY + " - Başka bir oyuncunun adasını ziyaret et.");
        player.sendMessage(ChatColor.YELLOW + "/island reset" + ChatColor.GRAY + " - Adanı sıfırla (onay gerekir).");
        player.sendMessage(ChatColor.YELLOW + "/island delete" + ChatColor.GRAY + " - Adanı sil (onay gerekir).");
        player.sendMessage(ChatColor.YELLOW + "/island help" + ChatColor.GRAY + " - Bu yardım mesajını göster.");
    }

    private void handleCreateCommand(Player player) {
        // ... (metod aynı) ...
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

        if (this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Zaten bir adanız var! Sıfırlamak için " + ChatColor.GOLD + "/island reset" + ChatColor.RED + " kullanabilirsiniz.");
        } else {
            this.islandLifecycleManager.createIsland(player);
            if (CREATE_COOLDOWN_SECONDS > 0) {
                createCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    private void handleHomeCommand(Player player, String[] args) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId()); //
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Önce bir ada oluşturmalısın: " + ChatColor.GOLD + "/island create"); //
            return;
        }
        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("spawn"))) { //
            this.islandTeleportManager.teleportPlayerToIslandSpawn(player); //
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) { //
                List<String> homes = island.getHomeNames(); //
                int currentIslandMaxHomes = island.getMaxHomesLimit(); // GÜNCELLENDİ: Adaya özel limitten al
                if (homes.isEmpty()) { //
                    player.sendMessage(ChatColor.YELLOW + "Ayarlanmış hiç ev noktan yok. " + ChatColor.GRAY + "(Maks: " + currentIslandMaxHomes + ")"); //
                } else {
                    player.sendMessage(ChatColor.GREEN + "Ev Noktaların (" + homes.size() + "/" + currentIslandMaxHomes + "): " + ChatColor.GOLD + String.join(ChatColor.GRAY + ", " + ChatColor.GOLD, homes)); //
                }
            } else {
                this.islandTeleportManager.teleportPlayerToNamedHome(player, args[1]); //
            }
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island home [isim|list|spawn]"); //
        }
    }

    private void handleSetHomeCommand(Player player, String[] args) {
        // ... (metod aynı, önceki düzeltmelerle) ...
        double setHomeCost = plugin.getConfig().getDouble("commands.sethome.cost", 50.0);
        if (this.economy != null && setHomeCost > 0) {
            if (economy.getBalance(player) < setHomeCost) {
                player.sendMessage(ChatColor.RED + "/island sethome kullanmak için yeterli paran yok! Gereken: " + economy.format(setHomeCost));
                return; // Para yoksa işlemi burada sonlandır
            }
            EconomyResponse r = economy.withdrawPlayer(player, setHomeCost);
            if (r.transactionSuccess()) {
                player.sendMessage(ChatColor.AQUA + economy.format(setHomeCost) + " sethome kullanım ücreti olarak hesabından çekildi.");
            } else {
                player.sendMessage(ChatColor.RED + "SetHome ücreti çekilirken bir hata oluştu: " + r.errorMessage);
                return; // Para çekilemezse işlemi burada sonlandır
            }
        }
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Ev noktanı ayarlayabileceğin bir adan yok!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island sethome <ev_ismi>");
            return;
        }
        String homeNameToSet = args[1];
        String homeNamePattern = plugin.getConfig().getString("island.home-name-pattern", "^[a-zA-Z0-9_]{2,16}$");
        if (!homeNameToSet.toLowerCase().matches(homeNamePattern)) {
            player.sendMessage(ChatColor.RED + "Ev adı 2-16 karakter uzunluğunda olmalı ve sadece harf, rakam veya alt çizgi içerebilir.");
            return;
        }

        Map<String, Location> homes = island.getNamedHomes();
        int currentIslandMaxHomes = island.getMaxHomesLimit();
        if (!homes.containsKey(homeNameToSet.toLowerCase()) && homes.size() >= currentIslandMaxHomes) {
            // DÜZELTME: Mesajda da 'currentIslandMaxHomes' kullanılıyor.
            player.sendMessage(ChatColor.RED + "Maksimum ev sayısına (" + currentIslandMaxHomes + ") ulaştın. Yeni bir ev ayarlamak için önce birini silmelisin veya /island upgrade homes ile limiti artırmalısın.");
            return;
        }

        try {
            if (!islandLifecycleManager.getIslandTerritoryRegion(island.getBaseLocation()).contains(BlockVector3.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
                player.sendMessage(ChatColor.RED + "Ev noktanı sadece adanın (genişletilmiş) sınırları içinde ayarlayabilirsin!");
                return;
            }
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Ada sınırları kontrol edilirken bir hata oluştu.");
            plugin.getLogger().log(Level.SEVERE, "Error checking island boundaries for sethome command for player " + player.getName(), e);
            return;
        }

        island.setNamedHome(homeNameToSet, player.getLocation());
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();
        player.sendMessage(ChatColor.GREEN + "'" + homeNameToSet + "' adlı ev noktan ayarlandı!");
    }

    // YENİ METOD: handleUpgradeCommand
    private void handleUpgradeCommand(Player player, String[] args) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Yükseltme yapabileceğin bir adan yok!");
            return;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("homes")) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island upgrade homes");
            player.sendMessage(ChatColor.YELLOW + "Bu komut, adanızdaki maksimum ev sayısını artırmanızı sağlar.");
            // Mevcut durumu ve sonraki yükseltme maliyetini göstermek iyi olabilir
            int currentLimit = island.getMaxHomesLimit();
            int maxPossible = plugin.getConfig().getInt("island.upgrades.homes.max_possible_total_homes", 10);
            double costPerUpgrade = plugin.getConfig().getDouble("island.upgrades.homes.cost_per_upgrade", 2000.0);
            int incrementAmount = plugin.getConfig().getInt("island.upgrades.homes.increment_amount", 1);

            player.sendMessage(ChatColor.AQUA + "Mevcut maksimum ev limitin: " + ChatColor.GOLD + currentLimit);
            if (currentLimit < maxPossible) {
                player.sendMessage(ChatColor.AQUA + "Sonraki yükseltme ile limitin " + ChatColor.GOLD + (currentLimit + incrementAmount) + ChatColor.AQUA + " olacak.");
                player.sendMessage(ChatColor.AQUA + "Maliyet: " + ChatColor.GOLD + economy.format(costPerUpgrade));
            } else {
                player.sendMessage(ChatColor.GREEN + "Maksimum ev limitine zaten ulaşmışsın!");
            }
            return;
        }

        // Yükseltme işlemi
        if (args[1].equalsIgnoreCase("homes")) {
            int currentLimit = island.getMaxHomesLimit();
            int incrementAmount = plugin.getConfig().getInt("island.upgrades.homes.increment_amount", 1);
            double costPerUpgrade = plugin.getConfig().getDouble("island.upgrades.homes.cost_per_upgrade", 2000.0);
            int maxPossibleTotalHomes = plugin.getConfig().getInt("island.upgrades.homes.max_possible_total_homes", 10);

            if (currentLimit >= maxPossibleTotalHomes) {
                player.sendMessage(ChatColor.RED + "Zaten ulaşabileceğin maksimum ev limitine (" + maxPossibleTotalHomes + ") sahipsin!");
                return;
            }

            if (this.economy == null) {
                player.sendMessage(ChatColor.RED + "Ekonomi sistemi aktif değil. Yükseltme yapılamıyor.");
                return;
            }

            if (economy.getBalance(player) < costPerUpgrade) {
                player.sendMessage(ChatColor.RED + "Ev limitini yükseltmek için yeterli paran yok! Gereken: " + economy.format(costPerUpgrade));
                return;
            }

            EconomyResponse r = economy.withdrawPlayer(player, costPerUpgrade);
            if (r.transactionSuccess()) {
                int newLimit = Math.min(currentLimit + incrementAmount, maxPossibleTotalHomes); // Limitin maxPossible'ı geçmediğinden emin ol
                island.setMaxHomesLimit(newLimit);
                islandDataHandler.addOrUpdateIslandData(island);
                islandDataHandler.saveChangesToDisk();
                player.sendMessage(ChatColor.GREEN + "Tebrikler! Maksimum ev limitin " + ChatColor.GOLD + newLimit + ChatColor.GREEN + " olarak yükseltildi.");
                player.sendMessage(ChatColor.GRAY + economy.format(costPerUpgrade) + " hesabından çekildi.");
            } else {
                player.sendMessage(ChatColor.RED + "Yükseltme ücreti çekilirken bir hata oluştu: " + r.errorMessage);
            }
        }
    }
    private void handleDelHomeCommand(Player player, String[] args) {
        // ... (metod aynı) ...
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Ev silebilmek için önce bir adanız olmalı!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island delhome <ev_ismi>");
            return;
        }
        String homeNameToDelete = args[1];
        if (island.deleteNamedHome(homeNameToDelete)) {
            islandDataHandler.addOrUpdateIslandData(island);
            islandDataHandler.saveChangesToDisk();
            player.sendMessage(ChatColor.GREEN + "'" + homeNameToDelete + "' adlı ev noktanız başarıyla silindi.");
        } else {
            player.sendMessage(ChatColor.RED + "'" + homeNameToDelete + "' adında bir ev noktanız bulunmuyor.");
        }
    }

    private void handleDeleteCommand(Player player, String[] args) {
        // ... (metod aynı) ...
        if (!this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
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
            if (this.islandLifecycleManager.deleteIsland(player)) {
                createCooldowns.remove(player.getUniqueId());
            }
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island delete veya /island delete confirm");
        }
    }

    private void handleResetCommand(Player player, String[] args) {
        // ... (metod aynı) ...
        if (!this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
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
            this.islandLifecycleManager.resetIsland(player);
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island reset veya /island reset confirm");
        }
    }

    private void handleFlagsCommand(Player player) {
        if (!this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bayraklarını düzenleyebileceğin bir adan yok!");
            return;
        }
        this.flagGUIManager.openFlagsGUI(player);
    }

    @SuppressWarnings("deprecation") // Bukkit.getOfflinePlayer(String) kullanımı için
    private void handleInfoCommand(Player player, String[] args) {
        Island islandToInfo;
        String islandOwnerName;
        OfflinePlayer ownerToDisplay; // Ada sahibini göstermek için


        if (args.length == 1) {
            islandToInfo = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
            if (islandToInfo == null) {
                player.sendMessage(ChatColor.RED + "Görüntülenecek bir adan yok. Oluşturmak için: " + ChatColor.GOLD + "/island create");
                return;
            }
            islandOwnerName = player.getName();
        } else if (args.length == 2) {
            OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(args[1]); // Deprecated
            // DÜZELTME: OfflinePlayer null ve UUID null kontrolü basitleştirildi/iyileştirildi
            if (targetOwner == null || targetOwner.getUniqueId() == null) { // Eğer UUID yoksa, oyuncu geçerli sayılmaz
                player.sendMessage(ChatColor.RED + "'" + args[1] + "' adında geçerli bir oyuncu profili bulunamadı.");
                return;
            }
            islandToInfo = this.islandDataHandler.getIslandByOwner(targetOwner.getUniqueId());
            if (islandToInfo == null) {
                player.sendMessage(ChatColor.RED + "'" + (targetOwner.getName() != null ? targetOwner.getName() : args[1]) + "' adlı oyuncunun bir adası bulunmuyor.");
                return;
            }
            islandOwnerName = targetOwner.getName() != null ? targetOwner.getName() : args[1];
        } else {
            player.sendMessage(ChatColor.RED + "Kullanım: /island info [oyuncu_adı]");
            return;
        }
        // ... (metodun geri kalanı aynı) ...
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(new Locale("tr", "TR"))
                .withZone(ZoneId.systemDefault());
        player.sendMessage(ChatColor.GOLD + "--- Ada Bilgileri: " + ChatColor.AQUA + islandToInfo.getIslandName() + ChatColor.GOLD + " ---");
        player.sendMessage(ChatColor.YELLOW + "Sahibi: " + ChatColor.WHITE + islandOwnerName);
        player.sendMessage(ChatColor.YELLOW + "Oluşturulma Tarihi: " + ChatColor.WHITE + formatter.format(islandToInfo.getCreationDate()));
        player.sendMessage(ChatColor.YELLOW + "Ziyaret Durumu: " + (islandToInfo.isPublic() ? ChatColor.GREEN + "Herkese Açık" : ChatColor.RED + "Özel"));
        player.sendMessage(ChatColor.YELLOW + "Sınırlar: " + (islandToInfo.areBoundariesEnforced() ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Pasif"));

        Location baseLoc = islandToInfo.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) {
            player.sendMessage(ChatColor.YELLOW + "Konum (Merkez): " + ChatColor.WHITE + "X: " + baseLoc.getBlockX() + ", Y: " + baseLoc.getBlockY() + ", Z: " + baseLoc.getBlockZ() + ChatColor.GRAY + " (" + baseLoc.getWorld().getName() + ")");
        }

        List<OfflinePlayer> members = this.islandMemberManager.getIslandMembers(islandToInfo);
        if (members.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Üyeler: " + ChatColor.GRAY + "Yok");
        } else {
            String memberNames = members.stream().map(op -> op.getName() != null ? op.getName() : op.getUniqueId().toString().substring(0,8)).collect(Collectors.joining(", "));
            player.sendMessage(ChatColor.YELLOW + "Üyeler (" + members.size() + "): " + ChatColor.WHITE + memberNames);
        }
        player.sendMessage(ChatColor.YELLOW + "Ev Sayısı: " + ChatColor.WHITE + islandToInfo.getNamedHomes().size() + "/" + plugin.getConfig().getInt("island.max-named-homes", 5));
    }

    private void handleSettingsCommand(Player player, String[] args) {
        // ... (metodun başı aynı) ...
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
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

                // SETTINGS NAME MALİYET KONTROLÜ
                double settingsNameCost = plugin.getConfig().getDouble("commands.settings_name.cost", 50000.0);
                if (this.economy != null && settingsNameCost > 0) {
                    if (economy.getBalance(player) < settingsNameCost) {
                        player.sendMessage(ChatColor.RED + "Ada ismini değiştirmek için yeterli paran yok! Gereken: " + economy.format(settingsNameCost));
                        return; // Para yoksa işlemi burada sonlandır
                    }
                    EconomyResponse r = economy.withdrawPlayer(player, settingsNameCost);
                    if (r.transactionSuccess()) {
                        player.sendMessage(ChatColor.AQUA + economy.format(settingsNameCost) + " isim değiştirme ücreti olarak hesabından çekildi.");
                    } else {
                        player.sendMessage(ChatColor.RED + "İsim değiştirme ücreti çekilirken bir hata oluştu: " + r.errorMessage);
                        return; // Para çekilemezse işlemi burada sonlandır
                    }
                }

                String newName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                this.islandSettingsManager.setIslandName(player, island, newName);
                break;
            case "visibility":
            case "gizlilik":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island settings visibility <public|private>");
                    return;
                }
                String visibility = args[2].toLowerCase();
                if (visibility.equals("public") || visibility.equals("herkeseacik")) {
                    this.islandSettingsManager.setIslandVisibility(player, island, true);
                } else if (visibility.equals("private") || visibility.equals("ozel")) {
                    this.islandSettingsManager.setIslandVisibility(player, island, false);
                } else {
                    player.sendMessage(ChatColor.RED + "Geçersiz görünürlük türü. Kullanılabilir: public, private");
                }
                break;
            case "boundary":
            case "sınır":
                // DÜZELTME: Koşul mantığı korunuyor, IDE uyarısı muhtemelen spesifik bir senaryoya işaret ediyor.
                // Eğer island.areBoundariesEnforced() her zaman false dönüyorsa, bu komut dışında bir sorun vardır.
                if (args.length == 2) {
                    this.islandSettingsManager.toggleIslandBoundaryEnforcement(player, island);
                } else if (args.length == 3) {
                    String desiredState = args[2].toLowerCase();
                    boolean currentBoundaryState = island.areBoundariesEnforced(); // Bu değerin doğruluğunu kontrol et
                    if ((desiredState.equals("on") || desiredState.equals("aktif"))) {
                        if (!currentBoundaryState) { // Sadece kapalıysa açmak için toggle et
                            this.islandSettingsManager.toggleIslandBoundaryEnforcement(player, island);
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "Ada sınırları zaten aktif.");
                        }
                    } else if ((desiredState.equals("off") || desiredState.equals("pasif"))) {
                        if (currentBoundaryState) { // Sadece açıksa kapatmak için toggle et
                            this.islandSettingsManager.toggleIslandBoundaryEnforcement(player, island);
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "Ada sınırları zaten pasif.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Geçersiz sınır komutu. Kullanım: /island settings boundary [on|off] veya sadece /island settings boundary");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island settings boundary [on|off]");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen ayar türü. Kullanılabilir: name, visibility, boundary");
                break;
        }
    }

    @SuppressWarnings("deprecation") // Bukkit.getOfflinePlayer(String) kullanımı için
    private void handleTeamCommand(Player player, String[] args) {
        // ... (metodun başı aynı) ...
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null && !(args.length > 1 && (args[1].equalsIgnoreCase("accept") || args[1].equalsIgnoreCase("deny")) ) ) {
            player.sendMessage(ChatColor.RED + "Takım komutlarını kullanabilmek için bir adanız olmalı.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island team <add|remove|list>");
            return;
        }
        String teamAction = args[1].toLowerCase();
        switch (teamAction) {
            case "add":
            case "ekle":
                if (island == null) {
                    player.sendMessage(ChatColor.RED + "Üye eklemek için ada sahibi olmalısınız."); return;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island team add <oyuncu_adı>");
                    return;
                }
                OfflinePlayer targetToAdd = Bukkit.getOfflinePlayer(args[2]);
                // DÜZELTME: OfflinePlayer null ve UUID null kontrolü
                if (targetToAdd == null || targetToAdd.getUniqueId() == null) {
                    player.sendMessage(ChatColor.RED + "'" + args[2] + "' adında geçerli bir oyuncu profili bulunamadı.");
                    return;
                }
                this.islandMemberManager.addMember(island, targetToAdd, player);
                break;
            case "remove":
            case "kick":
            case "at":
            case "çıkar":
                if (island == null) {
                    player.sendMessage(ChatColor.RED + "Üye çıkarmak için ada sahibi olmalısınız."); return;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island team remove <oyuncu_adı>");
                    return;
                }
                OfflinePlayer targetToRemove = Bukkit.getOfflinePlayer(args[2]);
                // DÜZELTME: OfflinePlayer null ve UUID null kontrolü (IslandMemberManager içinde daha detaylı kontrol var)
                if (targetToRemove == null || targetToRemove.getUniqueId() == null) {
                    player.sendMessage(ChatColor.RED + "'" + args[2] + "' adında geçerli bir oyuncu profili bulunamadı.");
                    return;
                }
                this.islandMemberManager.removeMember(island, targetToRemove, player);
                break;
            case "list":
            case "liste":
                // ... (liste kısmı aynı) ...
                if (island == null) {
                    player.sendMessage(ChatColor.RED + "Listelenecek bir adan yok.");
                    return;
                }
                List<OfflinePlayer> members = this.islandMemberManager.getIslandMembers(island);
                if (members.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Adanızda hiç üye yok.");
                } else {
                    String memberNames = members.stream()
                            .map(op -> op.getName() != null ? op.getName() : op.getUniqueId().toString().substring(0,8))
                            .collect(Collectors.joining(ChatColor.GRAY + ", " + ChatColor.WHITE));
                    player.sendMessage(ChatColor.GREEN + "Ada Üyeleri ("+ members.size() + "/" + plugin.getConfig().getInt("island.max-members",3) +"): " + ChatColor.WHITE + memberNames);
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen takım komutu. Kullanılabilir: add, remove, list");
                break;
        }
    }

    @SuppressWarnings("deprecation") // Bukkit.getOfflinePlayer(String) kullanımı için
    private void handleVisitCommand(Player player, String[] args) {
        // ... (metod aynı, önceki isBanned düzeltmesiyle) ...
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island visit <oyuncu_adı>");
            return;
        }
        String ownerName = args[1];
        OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(ownerName);
        if (targetOwner == null || targetOwner.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "'" + ownerName + "' adında geçerli bir oyuncu profili bulunamadı.");
            return;
        }

        Island targetIsland = this.islandDataHandler.getIslandByOwner(targetOwner.getUniqueId());
        if (targetIsland == null) {
            player.sendMessage(ChatColor.RED + "'" + (targetOwner.getName() != null ? targetOwner.getName() : ownerName) + "' adlı oyuncunun bir adası bulunmuyor.");
            return;
        }

        if (targetIsland.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Zaten kendi adandasın. Gitmek için " + ChatColor.GOLD + "/island go" + ChatColor.YELLOW + " kullan.");
            return;
        }

        if (!targetIsland.canPlayerVisit(player)) {
            player.sendMessage(ChatColor.RED + "'" + (targetOwner.getName() != null ? targetOwner.getName() : ownerName) + "' adlı oyuncunun adası ziyaret edilemez (muhtemelen özel).");
            return;
        }
        this.islandTeleportManager.teleportPlayerToVisitIsland(player, targetIsland);
    }

    private void handleBiomeCommand(Player player, String[] args) {
        // ... (metod aynı) ...
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için bir adanız olmalı.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island biome <set <biyom_adı> | get | list>");
            return;
        }

        String biomeAction = args[1].toLowerCase();
        if (this.islandBiomeManager == null) {
            player.sendMessage(ChatColor.RED + "Biyom yöneticisi yüklenemedi. Lütfen sunucu yöneticisine bildirin.");
            plugin.getLogger().severe("IslandCommand: IslandBiomeManager null geldi!");
            return;
        }

        switch (biomeAction) {
            case "set":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island biome set <biyom_adı>");
                    return;
                }
                // BIOME SET MALİYET KONTROLÜ
                double biomeSetCost = plugin.getConfig().getDouble("commands.biome_set.cost", 10000.0);
                if (this.economy != null && biomeSetCost > 0) {
                    if (economy.getBalance(player) < biomeSetCost) {
                        player.sendMessage(ChatColor.RED + "Ada biyomunu değiştirmek için yeterli paran yok! Gereken: " + economy.format(biomeSetCost));
                        return; // Para yoksa işlemi burada sonlandır
                    }
                    EconomyResponse r = economy.withdrawPlayer(player, biomeSetCost);
                    if (r.transactionSuccess()) {
                        player.sendMessage(ChatColor.AQUA + economy.format(biomeSetCost) + " biyom değiştirme ücreti olarak hesabından çekildi.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Biyom değiştirme ücreti çekilirken bir hata oluştu: " + r.errorMessage);
                        return; // Para çekilemezse işlemi burada sonlandır
                    }
                }


                String biomeName = args[2].toUpperCase();
                this.islandBiomeManager.setIslandBiome(player, island, biomeName);
                break;

            case "get":
                String currentBiome = this.islandBiomeManager.getIslandBiome(island);
                player.sendMessage(ChatColor.YELLOW + "Adanızın mevcut biyomu: " + ChatColor.AQUA + currentBiome);
                break;
            case "list":
                this.islandBiomeManager.sendAvailableBiomes(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen biyom komutu. Kullanım: set, get, list");
                break;
        }
    }

    private void handleWelcomeCommand(Player player, String[] args) {
        // ... (metod aynı) ...
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için bir adanız olmalı.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /island welcome <set <mesaj...> | clear | view>");
            return;
        }

        String welcomeAction = args[1].toLowerCase();
        if (this.islandWelcomeManager == null) {
            player.sendMessage(ChatColor.RED + "Karşılama mesajı yöneticisi yüklenemedi.");
            plugin.getLogger().severe("IslandCommand: IslandWelcomeManager null!");
            return;
        }

        switch (welcomeAction) {
            case "set":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /island welcome set <mesaj...>");
                    return;
                }
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    messageBuilder.append(args[i]).append(" ");
                }
                String message = messageBuilder.toString().trim();
                this.islandWelcomeManager.setWelcomeMessage(player, island, message);
                break;
            case "clear":
                this.islandWelcomeManager.clearWelcomeMessage(player, island);
                break;
            case "view":
                this.islandWelcomeManager.viewWelcomeMessage(player, island);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen welcome komutu. Kullanım: set, clear, view");
                break;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // ... (metod aynı) ...
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        List<String> subCommands = Arrays.asList("create", "go", "sethome", "home", "delhome", "delete", "reset", "flags", "info", "settings", "team", "visit", "help", "biome", "welcome", "upgrade"); // "upgrade" eklendi
        if (args.length == 1) {
            String arg0Lower = args[0].toLowerCase();
            for (String sc : subCommands) {
                if (sc.startsWith(arg0Lower)) {
                    completions.add(sc);
                }
            }
            return completions;
        }

        String subCmd = args[0].toLowerCase();
        // arg1Lower ve arg2Lower tanımlamaları ilgili if bloklarının içine alındı
        if (args.length == 2) {
            String arg1Lower = args[1].toLowerCase(); // Tanımlama burada
            if (subCmd.equals("home") || subCmd.equals("delhome") || subCmd.equals("sethome")) {
                Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
                if (island != null) {
                    List<String> homes = new ArrayList<>(island.getHomeNames());
                    if (subCmd.equals("home")) homes.add("list");
                    for (String home : homes) {
                        if (home.toLowerCase().startsWith(arg1Lower)) {
                            completions.add(home);
                        }
                    }
                }
            } else if (subCmd.equals("settings")) {
                List<String> settingsOptions = Arrays.asList("name", "visibility", "boundary");
                for (String opt : settingsOptions) {
                    if (opt.startsWith(arg1Lower)) {
                        completions.add(opt);
                    }
                }
            } else if (subCmd.equals("team")) {
                List<String> teamOptions = Arrays.asList("add", "remove", "list", "leave");
                for (String opt : teamOptions) {
                    if (opt.startsWith(arg1Lower)) {
                        completions.add(opt);
                    }
                }
            } else if (subCmd.equals("info") || subCmd.equals("visit")) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (p.getName().toLowerCase().startsWith(arg1Lower)) {
                        completions.add(p.getName());
                    }
                });
            } else if (subCmd.equals("delete") || subCmd.equals("reset")) {
                if ("confirm".startsWith(arg1Lower)) {
                    completions.add("confirm");
                }
            } else if (subCmd.equals("biome")) {
                List<String> biomeActions = Arrays.asList("set", "get", "list");
                for (String action : biomeActions) {
                    if (action.startsWith(arg1Lower)) {
                        completions.add(action);
                    }
                }
            } else if (subCmd.equals("welcome")) {
                List<String> welcomeActions = Arrays.asList("set", "clear", "view");
                for (String action : welcomeActions) {
                    if (action.startsWith(arg1Lower)) {
                        completions.add(action);
                    }
                }
            }
            if (subCmd.equals("upgrade")) { // YENİ
                if ("homes".startsWith(arg1Lower)) {
                    completions.add("homes");
                }
            }
        }

        if (args.length == 3) {
            String actionOrSetting = args[1].toLowerCase();
            String arg2Lower = args[2].toLowerCase(); // Tanımlama burada
            if (subCmd.equals("settings")) {
                if (actionOrSetting.equals("visibility")) {
                    if ("public".startsWith(arg2Lower)) completions.add("public");
                    if ("private".startsWith(arg2Lower)) completions.add("private");
                } else if (actionOrSetting.equals("boundary")) {
                    if ("on".startsWith(arg2Lower)) completions.add("on");
                    if ("off".startsWith(arg2Lower)) completions.add("off");
                }
            } else if (subCmd.equals("team") && (actionOrSetting.equals("add") || actionOrSetting.equals("remove"))) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!p.equals(player) && p.getName().toLowerCase().startsWith(arg2Lower)) {
                        completions.add(p.getName());
                    }
                });
            } else if (subCmd.equals("biome") && actionOrSetting.equalsIgnoreCase("set")) {
                Arrays.stream(Biome.values())
                        .filter(b -> b != Biome.CUSTOM && !b.name().startsWith("THE_VOID"))
                        .map(Enum::name)
                        .filter(name ->
                                name.toLowerCase().startsWith(arg2Lower))
                        .forEach(completions::add);
            }
        }
        return completions;
    }
}