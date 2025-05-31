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
import com.knemis.skyblock.skyblockcoreproject.economy.worth.IslandWorthManager;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import com.sk89q.worldedit.math.BlockVector3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
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
    private final IslandWorthManager islandWorthManager;

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
                         FlagGUIManager flagGUIManager, IslandWorthManager islandWorthManager) {
        this.plugin = plugin;
        this.islandDataHandler = islandDataHandler;
        this.islandLifecycleManager = islandLifecycleManager;
        this.islandSettingsManager = islandSettingsManager;
        this.islandMemberManager = islandMemberManager;
        this.islandTeleportManager = islandTeleportManager;
        this.islandBiomeManager = islandBiomeManager;
        this.islandWelcomeManager = islandWelcomeManager;
        this.flagGUIManager = flagGUIManager;
        this.economy = plugin.getEconomy();
        this.islandWorthManager = islandWorthManager;

        this.createCooldowns = new HashMap<>();
        this.CREATE_COOLDOWN_SECONDS = plugin.getConfig().getLong("island.creation-cooldown-seconds", 300);
        this.deleteConfirmations = new HashMap<>();
        this.resetConfirmations = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.getLogger().info(String.format("%s executed /%s with args: %s", sender.getName(), command.getName(), String.join(" ", args)));
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Bu komutu sadece oyuncular kullanabilir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Non-player %s attempted to execute /%s. Command aborted.", sender.getName(), command.getName()));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            plugin.getLogger().info(String.format("Sent help message to %s for /%s (no subcommand)", player.getName(), command.getName()));
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
                plugin.getLogger().info(String.format("Successfully processed /%s help for %s", command.getName(), player.getName()));
                break;
            case "biome":
                handleBiomeCommand(player, args);
                break;
            case "welcome":
                handleWelcomeCommand(player, args);
                break;
            case "upgrade":
                handleUpgradeCommand(player, args);
                break;
            case "level":
            case "worth":
                handleLevelCommand(player, args);
                break;
            default:
                player.sendMessage(Component.text("Bilinmeyen alt komut: " + subCommand, NamedTextColor.RED));
                sendHelpMessage(player);
                plugin.getLogger().warning(String.format("Player %s failed to execute /%s: Unknown subcommand '%s'", player.getName(), command.getName(), subCommand));
                break;
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(Component.text("--- Skyblock Komutları ---", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/island create", NamedTextColor.YELLOW).append(Component.text(" - Yeni ada oluştur.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island go", NamedTextColor.YELLOW).append(Component.text(" - Adanın ana spawn noktasına git.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island sethome <isim>", NamedTextColor.YELLOW).append(Component.text(" - Ada içinde ev belirle.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island home <isim>", NamedTextColor.YELLOW).append(Component.text(" - Belirtilen evine git.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island home list", NamedTextColor.YELLOW).append(Component.text(" - Evlerini listele.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island delhome <isim>", NamedTextColor.YELLOW).append(Component.text(" - Belirtilen evini sil.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island biome <set|get|list> [biyom]", NamedTextColor.YELLOW).append(Component.text(" - Ada biyomunu yönet.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island welcome <set|clear|view> [mesaj]", NamedTextColor.YELLOW).append(Component.text(" - Ada karşılama mesajını yönet.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island flags", NamedTextColor.YELLOW).append(Component.text(" - Ada bayraklarını yönet (Genel).", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island info [oyuncu]", NamedTextColor.YELLOW).append(Component.text(" - Kendi adanın veya başkasının adasının bilgilerini gör.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island settings name <yeni_isim>", NamedTextColor.YELLOW).append(Component.text(" - Adanın ismini değiştir.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island settings visibility <public|private>", NamedTextColor.YELLOW).append(Component.text(" - Adanın ziyaretçi durumunu ayarla.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island settings boundary", NamedTextColor.YELLOW).append(Component.text(" - Ada sınırlarını aç/kapa (toggle).", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island team add <oyuncu>", NamedTextColor.YELLOW).append(Component.text(" - Adana üye ekle.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island team remove <oyuncu>", NamedTextColor.YELLOW).append(Component.text(" - Adadan üye çıkar.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island team list", NamedTextColor.YELLOW).append(Component.text(" - Ada üyelerini listele.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island upgrade homes", NamedTextColor.YELLOW).append(Component.text(" - Maksimum ev sayını artır.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island visit <oyuncu_adı>", NamedTextColor.YELLOW).append(Component.text(" - Başka bir oyuncunun adasını ziyaret et.", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island reset", NamedTextColor.YELLOW).append(Component.text(" - Adanı sıfırla (onay gerekir).", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island delete", NamedTextColor.YELLOW).append(Component.text(" - Adanı sil (onay gerekir).", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/island help", NamedTextColor.YELLOW).append(Component.text(" - Bu yardım mesajını göster.", NamedTextColor.GRAY)));
    }

    private void handleCreateCommand(Player player) {
        if (createCooldowns.containsKey(player.getUniqueId())) {
            long timeElapsedMillis = System.currentTimeMillis() - createCooldowns.get(player.getUniqueId());
            long cooldownMillis = CREATE_COOLDOWN_SECONDS * 1000;
            if (timeElapsedMillis < cooldownMillis) {
                long secondsLeft = (cooldownMillis - timeElapsedMillis) / 1000;
                player.sendMessage(Component.text("Bu komutu tekrar kullanmak için " + secondsLeft + " saniye beklemelisiniz.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island create: Cooldown active (%d seconds left)", player.getName(), secondsLeft));
                return;
            } else {
                createCooldowns.remove(player.getUniqueId());
            }
        }

        if (this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(Component.text("Zaten bir adanız var! Sıfırlamak için ", NamedTextColor.RED)
                    .append(Component.text("/island reset", NamedTextColor.GOLD))
                    .append(Component.text(" kullanabilirsiniz.", NamedTextColor.RED)));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island create: Already has an island", player.getName()));
        } else {
            this.islandLifecycleManager.createIsland(player);
            if (CREATE_COOLDOWN_SECONDS > 0) {
                createCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
            plugin.getLogger().info(String.format("Successfully processed /island create for %s", player.getName()));
        }
    }

    private void handleHomeCommand(Player player, String[] args) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island %s: Island object was null for player UUID: %s", player.getName(), args[0], player.getUniqueId()));
            return;
        }
        String subCommand = args[0].toLowerCase();

        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("spawn"))) {
            this.islandTeleportManager.teleportPlayerToIslandSpawn(player);
            plugin.getLogger().info(String.format("Successfully processed /island %s (spawn) for %s", subCommand, player.getName()));
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                List<String> homes = island.getHomeNames();
                int currentIslandMaxHomes = island.getMaxHomesLimit();
                if (homes.isEmpty()) {
                    player.sendMessage(Component.text("Ayarlanmış hiç ev noktan yok. ", NamedTextColor.YELLOW)
                            .append(Component.text("(Maks: " + currentIslandMaxHomes + ")", NamedTextColor.GRAY)));
                } else {
                    player.sendMessage(Component.text("Ev Noktaların (" + homes.size() + "/" + currentIslandMaxHomes + "): ", NamedTextColor.GREEN)
                            .append(Component.text(String.join(", ", homes), NamedTextColor.GOLD)));
                }
                plugin.getLogger().info(String.format("Successfully processed /island %s list for %s", subCommand, player.getName()));
            } else {
                this.islandTeleportManager.teleportPlayerToNamedHome(player, args[1]);
                plugin.getLogger().info(String.format("Attempted /island %s %s for %s", subCommand, args[1], player.getName()));
            }
        } else {
            player.sendMessage(Component.text("Kullanım: /island home [isim|list|spawn]", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island %s: Invalid arguments. Usage: /island home [isim|list|spawn]", player.getName(), subCommand));
        }
    }

    private void handleLevelCommand(Player player, String[] args) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island %s: Island object was null. UUID: %s", player.getName(), args[0].toLowerCase(), player.getUniqueId()));
            return;
        }
        String subCommand = args[0].toLowerCase();
        if (args.length > 1 && args[1].equalsIgnoreCase("calculate")) {
            if (islandWorthManager != null) {
                islandWorthManager.calculateAndSetIslandWorth(island, player);
                plugin.getLogger().info(String.format("Successfully processed /island %s calculate for %s", subCommand, player.getName()));
            } else {
                player.sendMessage(Component.text("Ada değer sistemi şu anda kullanılamıyor.", NamedTextColor.RED));
                plugin.getLogger().severe("IslandWorthManager IslandCommand içinde null!");
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s calculate: IslandWorthManager is null", player.getName(), subCommand));
            }
        } else {
            player.sendMessage(Component.text("--- Ada Değerin & Seviyen ---", NamedTextColor.AQUA));
            player.sendMessage(Component.text("Mevcut Değer: ", NamedTextColor.YELLOW).append(Component.text(String.format("%.2f", island.getIslandWorth()), NamedTextColor.GOLD)));
            player.sendMessage(Component.text("Mevcut Seviye: ", NamedTextColor.YELLOW).append(Component.text(String.valueOf(island.getIslandLevel()), NamedTextColor.GOLD)));
            player.sendMessage(Component.text("Değeri yeniden hesaplamak için: /island level calculate", NamedTextColor.GRAY));
            plugin.getLogger().info(String.format("Successfully processed /island %s (view) for %s", subCommand, player.getName()));
        }
    }

    private void handleSetHomeCommand(Player player, String[] args) {
        double setHomeCost = plugin.getConfig().getDouble("commands.sethome.cost", 50.0);
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());

        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }

        if (this.economy != null && setHomeCost > 0) {
            if (economy.getBalance(player) < setHomeCost) {
                player.sendMessage(Component.text("/island sethome kullanmak için yeterli paran yok! Gereken: " + economy.format(setHomeCost), NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Insufficient funds. Needed: %s", player.getName(), economy.format(setHomeCost)));
                return;
            }
            EconomyResponse r = economy.withdrawPlayer(player, setHomeCost);
            if (r.transactionSuccess()) {
                player.sendMessage(Component.text(economy.format(setHomeCost) + " sethome kullanım ücreti olarak hesabından çekildi.", NamedTextColor.AQUA));
            } else {
                player.sendMessage(Component.text("SetHome ücreti çekilirken bir hata oluştu: " + r.errorMessage, NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Economy withdraw error: %s", player.getName(), r.errorMessage));
                return;
            }
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island sethome <ev_ismi>", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Missing home name argument.", player.getName()));
            return;
        }
        String homeNameToSet = args[1];
        String homeNamePattern = plugin.getConfig().getString("island.home-name-pattern", "^[a-zA-Z0-9_]{2,16}$");
        if (!homeNameToSet.toLowerCase().matches(homeNamePattern)) {
            player.sendMessage(Component.text("Ev adı 2-16 karakter uzunluğunda olmalı ve sadece harf, rakam veya alt çizgi içerebilir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Invalid home name format for '%s'", player.getName(), homeNameToSet));
            return;
        }

        Map<String, Location> homes = island.getNamedHomes();
        int currentIslandMaxHomes = island.getMaxHomesLimit();
        if (!homes.containsKey(homeNameToSet.toLowerCase()) && homes.size() >= currentIslandMaxHomes) {
            player.sendMessage(Component.text("Maksimum ev sayısına (" + currentIslandMaxHomes + ") ulaştın. Yeni bir ev ayarlamak için önce birini silmelisin veya /island upgrade homes ile limiti artırmalısın.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Max homes limit reached (%d)", player.getName(), currentIslandMaxHomes));
            return;
        }

        try {
            if (!islandLifecycleManager.getIslandTerritoryRegion(island.getBaseLocation()).contains(BlockVector3.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
                player.sendMessage(Component.text("Ev noktanı sadece adanın (genişletilmiş) sınırları içinde ayarlayabilirsin!", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island sethome: Location is outside island boundaries.", player.getName()));
                return;
            }
        } catch (IOException e) {
            player.sendMessage(Component.text("Ada sınırları kontrol edilirken bir hata oluştu.", NamedTextColor.RED));
            plugin.getLogger().log(Level.SEVERE, "Error checking island boundaries for sethome command for player " + player.getName(), e);
            return;
        }

        island.setNamedHome(homeNameToSet, player.getLocation());
        islandDataHandler.addOrUpdateIslandData(island);
        islandDataHandler.saveChangesToDisk();
        player.sendMessage(Component.text("'" + homeNameToSet + "' adlı ev noktan ayarlandı!", NamedTextColor.GREEN));
        plugin.getLogger().info(String.format("Successfully processed /island sethome %s for %s", homeNameToSet, player.getName()));
    }

    private void handleUpgradeCommand(Player player, String[] args) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island upgrade: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }
        String subCommand = args[0].toLowerCase();
        String upgradeType = (args.length > 1) ? args[1].toLowerCase() : "";

        if (args.length < 2 || !args[1].equalsIgnoreCase("homes")) {
            player.sendMessage(Component.text("Kullanım: /island upgrade homes", NamedTextColor.RED));
            player.sendMessage(Component.text("Bu komut, adanızdaki maksimum ev sayısını artırmanızı sağlar.", NamedTextColor.YELLOW));
            int currentLimit = island.getMaxHomesLimit();
            int maxPossible = plugin.getConfig().getInt("island.upgrades.homes.max_possible_total_homes", 10);
            double costPerUpgrade = plugin.getConfig().getDouble("island.upgrades.homes.cost_per_upgrade", 2000.0);
            int incrementAmount = plugin.getConfig().getInt("island.upgrades.homes.increment_amount", 1);

            player.sendMessage(Component.text("Mevcut maksimum ev limitin: ", NamedTextColor.AQUA).append(Component.text(String.valueOf(currentLimit), NamedTextColor.GOLD)));
            if (currentLimit < maxPossible) {
                player.sendMessage(Component.text("Sonraki yükseltme ile limitin ", NamedTextColor.AQUA)
                        .append(Component.text(String.valueOf(currentLimit + incrementAmount), NamedTextColor.GOLD))
                        .append(Component.text(" olacak.", NamedTextColor.AQUA)));
                player.sendMessage(Component.text("Maliyet: ", NamedTextColor.AQUA).append(Component.text(economy.format(costPerUpgrade), NamedTextColor.GOLD)));
            } else {
                player.sendMessage(Component.text("Maksimum ev limitine zaten ulaşmışsın!", NamedTextColor.GREEN));
            }
            plugin.getLogger().warning(String.format("Player %s failed to execute /island %s: Invalid arguments or just viewing help. Args: %s", player.getName(), subCommand, String.join(" ", args)));
            return;
        }

        if (upgradeType.equals("homes")) {
            int currentLimit = island.getMaxHomesLimit();
            int incrementAmount = plugin.getConfig().getInt("island.upgrades.homes.increment_amount", 1);
            double costPerUpgrade = plugin.getConfig().getDouble("island.upgrades.homes.cost_per_upgrade", 2000.0);
            int maxPossibleTotalHomes = plugin.getConfig().getInt("island.upgrades.homes.max_possible_total_homes", 10);

            if (currentLimit >= maxPossibleTotalHomes) {
                player.sendMessage(Component.text("Zaten ulaşabileceğin maksimum ev limitine (" + maxPossibleTotalHomes + ") sahipsin!", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s homes: Already at max limit (%d)", player.getName(), subCommand, maxPossibleTotalHomes));
                return;
            }

            if (this.economy == null) {
                player.sendMessage(Component.text("Ekonomi sistemi aktif değil. Yükseltme yapılamıyor.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s homes: Economy is null.", player.getName(), subCommand));
                return;
            }

            if (economy.getBalance(player) < costPerUpgrade) {
                player.sendMessage(Component.text("Ev limitini yükseltmek için yeterli paran yok! Gereken: " + economy.format(costPerUpgrade), NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s homes: Insufficient funds. Needed: %s", player.getName(), subCommand, economy.format(costPerUpgrade)));
                return;
            }

            EconomyResponse r = economy.withdrawPlayer(player, costPerUpgrade);
            if (r.transactionSuccess()) {
                int newLimit = Math.min(currentLimit + incrementAmount, maxPossibleTotalHomes);
                island.setMaxHomesLimit(newLimit);
                islandDataHandler.addOrUpdateIslandData(island);
                islandDataHandler.saveChangesToDisk();
                player.sendMessage(Component.text("Tebrikler! Maksimum ev limitin ", NamedTextColor.GREEN)
                        .append(Component.text(String.valueOf(newLimit), NamedTextColor.GOLD))
                        .append(Component.text(" olarak yükseltildi.", NamedTextColor.GREEN)));
                player.sendMessage(Component.text(economy.format(costPerUpgrade) + " hesabından çekildi.", NamedTextColor.GRAY));
                plugin.getLogger().info(String.format("Successfully processed /island %s homes for %s. New limit: %d", subCommand, player.getName(), newLimit));
            } else {
                player.sendMessage(Component.text("Yükseltme ücreti çekilirken bir hata oluştu: " + r.errorMessage, NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s homes: Economy withdraw error: %s", player.getName(), subCommand, r.errorMessage));
            }
        }
    }
    private void handleDelHomeCommand(Player player, String[] args) {
        Island island = islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island delhome: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island delhome <ev_ismi>", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island delhome: Missing home name argument.", player.getName()));
            return;
        }
        String homeNameToDelete = args[1];
        if (island.deleteNamedHome(homeNameToDelete)) {
            islandDataHandler.addOrUpdateIslandData(island);
            islandDataHandler.saveChangesToDisk();
            player.sendMessage(Component.text("'" + homeNameToDelete + "' adlı ev noktanız başarıyla silindi.", NamedTextColor.GREEN));
            plugin.getLogger().info(String.format("Successfully processed /island delhome %s for %s", homeNameToDelete, player.getName()));
        } else {
            player.sendMessage(Component.text("'" + homeNameToDelete + "' adında bir ev noktanız bulunmuyor.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island delhome %s: Home not found.", player.getName(), homeNameToDelete));
        }
    }

    private void handleDeleteCommand(Player player, String[] args) {
        if (!this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(Component.text("Silebileceğin bir adan yok!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island delete: No island to delete.", player.getName()));
            return;
        }
        String subCommand = args.length > 1 ? args[1].toLowerCase() : "";

        if (args.length == 1) {
            deleteConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(Component.text("Adanı silmek istediğinden emin misin? Onaylamak için ", NamedTextColor.YELLOW)
                    .append(Component.text("/island delete confirm", NamedTextColor.GOLD))
                    .append(Component.text(" yaz. Bu işlem geri alınamaz!", NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("(Onaylama isteğin " + DELETE_CONFIRM_TIMEOUT_SECONDS + " saniye sonra zaman aşımına uğrayacak.)", NamedTextColor.GRAY));
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (deleteConfirmations.remove(player.getUniqueId()) != null) {
                        if (player.isOnline()) {
                            player.sendMessage(Component.text("Ada silme onaylama isteğin zaman aşımına uğradı.", NamedTextColor.RED));
                            plugin.getLogger().info(String.format("/island delete confirmation for %s timed out.", player.getName()));
                        }
                    }
                }
            }.runTaskLater(plugin, DELETE_CONFIRM_TIMEOUT_SECONDS * 20L);
        } else if (args.length == 2 && subCommand.equalsIgnoreCase("confirm")) {
            Long requestTime = deleteConfirmations.remove(player.getUniqueId());
            if (requestTime == null || (System.currentTimeMillis() - requestTime) > (DELETE_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                player.sendMessage(Component.text("Onaylanacak aktif bir ada silme isteğin bulunmuyor veya isteğin zaman aşımına uğramış.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island delete confirm: No active request or timed out.", player.getName()));
                return;
            }
            if (this.islandLifecycleManager.deleteIsland(player)) {
                createCooldowns.remove(player.getUniqueId());
                plugin.getLogger().info(String.format("Successfully processed /island delete confirm for %s", player.getName()));
            } else {
                plugin.getLogger().warning(String.format("Processing /island delete confirm for %s failed (reason should be logged by IslandLifecycleManager).", player.getName()));
            }
        } else {
            player.sendMessage(Component.text("Kullanım: /island delete veya /island delete confirm", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island delete: Invalid arguments. Usage: /island delete or /island delete confirm", player.getName()));
        }
    }

    private void handleResetCommand(Player player, String[] args) {
        if (!this.islandDataHandler.playerHasIsland(player.getUniqueId())) {
            player.sendMessage(Component.text("Sıfırlayabileceğin bir adan yok!", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island reset: No island to reset.", player.getName()));
            return;
        }
        String subCommand = args.length > 1 ? args[1].toLowerCase() : "";

        if (args.length == 1) {
            resetConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(Component.text("Adanı sıfırlamak istediğinden emin misin? Bu işlem adandaki her şeyi silip adayı başlangıç haline döndürecek ve TÜM EV NOKTALARINI SİLECEKTİR. Onaylamak için ", NamedTextColor.YELLOW)
                    .append(Component.text("/island reset confirm", NamedTextColor.GOLD))
                    .append(Component.text(" yaz. Bu işlem geri alınamaz!", NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("(Onaylama isteğin " + RESET_CONFIRM_TIMEOUT_SECONDS + " saniye sonra zaman aşımına uğrayacak.)", NamedTextColor.GRAY));
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (resetConfirmations.remove(player.getUniqueId()) != null) {
                        if (player.isOnline()) {
                            player.sendMessage(Component.text("Ada sıfırlama onaylama isteğin zaman aşımına uğradı.", NamedTextColor.RED));
                            plugin.getLogger().info(String.format("/island reset confirmation for %s timed out.", player.getName()));
                        }
                    }
                }
            }.runTaskLater(plugin, RESET_CONFIRM_TIMEOUT_SECONDS * 20L);
        } else if (args.length == 2 && subCommand.equalsIgnoreCase("confirm")) {
            Long requestTime = resetConfirmations.remove(player.getUniqueId());
            if (requestTime == null || (System.currentTimeMillis() - requestTime) > (RESET_CONFIRM_TIMEOUT_SECONDS * 1000)) {
                player.sendMessage(Component.text("Onaylanacak aktif bir ada sıfırlama isteğin bulunmuyor veya isteğin zaman aşımına uğramış.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island reset confirm: No active request or timed out.", player.getName()));
                return;
            }
            this.islandLifecycleManager.resetIsland(player);
            plugin.getLogger().info(String.format("Successfully processed /island reset confirm for %s", player.getName()));
        } else {
            player.sendMessage(Component.text("Kullanım: /island reset veya /island reset confirm", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island reset: Invalid arguments. Usage: /island reset or /island reset confirm", player.getName()));
        }
    }

    private void handleFlagsCommand(Player player) {
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island flags: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }
        this.flagGUIManager.openFlagsGUI(player);
        plugin.getLogger().info(String.format("Successfully processed /island flags for %s", player.getName()));
    }

    @SuppressWarnings("deprecation") // Bukkit.getOfflinePlayer(String) kullanımı için
    private void handleInfoCommand(Player player, String[] args) {
        Island islandToInfo;
        String islandOwnerName;
        String subCommand = args[0].toLowerCase();
        String targetPlayerName = (args.length > 1) ? args[1] : player.getName();

        if (args.length == 1) {
            islandToInfo = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
            if (islandToInfo == null) {
                player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s (self): Island object was null. UUID: %s", player.getName(), subCommand, player.getUniqueId()));
                return;
            }
            islandOwnerName = player.getName();
        } else if (args.length == 2) {
            OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(args[1]);
            if (targetOwner == null || targetOwner.getUniqueId() == null) {
                player.sendMessage(Component.text("'" + args[1] + "' adında geçerli bir oyuncu profili bulunamadı.", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island %s %s: Target player profile not found.", player.getName(), subCommand, args[1]));
                return;
            }
            islandToInfo = this.islandDataHandler.getIslandByOwner(targetOwner.getUniqueId());
            if (islandToInfo == null) {
                player.sendMessage(Component.text("'" + (targetOwner.getName() != null ? targetOwner.getName() : args[1]) + "' adlı oyuncunun bir adası bulunmuyor.", NamedTextColor.RED));
                plugin.getLogger().info(String.format("Player %s executed /island %s %s: Target player has no island.", player.getName(), subCommand, args[1]));
                return;
            }
            islandOwnerName = targetOwner.getName() != null ? targetOwner.getName() : args[1];
        } else {
            player.sendMessage(Component.text("Kullanım: /island info [oyuncu_adı]", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island %s: Invalid arguments. Usage: /island info [oyuncu_adı]", player.getName(), subCommand));
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(new Locale("tr", "TR"))
                .withZone(ZoneId.systemDefault());
        player.sendMessage(Component.text("--- Ada Bilgileri: ", NamedTextColor.GOLD).append(Component.text(islandToInfo.getIslandName(), NamedTextColor.AQUA)).append(Component.text(" ---", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("Sahibi: ", NamedTextColor.YELLOW).append(Component.text(islandOwnerName, NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Oluşturulma Tarihi: ", NamedTextColor.YELLOW).append(Component.text(formatter.format(islandToInfo.getCreationDate()), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Ziyaret Durumu: ", NamedTextColor.YELLOW).append(islandToInfo.isPublic() ? Component.text("Herkese Açık", NamedTextColor.GREEN) : Component.text("Özel", NamedTextColor.RED)));
        player.sendMessage(Component.text("Sınırlar: ", NamedTextColor.YELLOW).append(islandToInfo.areBoundariesEnforced() ? Component.text("Aktif", NamedTextColor.GREEN) : Component.text("Pasif", NamedTextColor.RED)));

        Location baseLoc = islandToInfo.getBaseLocation();
        if (baseLoc != null && baseLoc.getWorld() != null) {
            player.sendMessage(Component.text("Konum (Merkez): ", NamedTextColor.YELLOW)
                    .append(Component.text("X: " + baseLoc.getBlockX() + ", Y: " + baseLoc.getBlockY() + ", Z: " + baseLoc.getBlockZ(), NamedTextColor.WHITE))
                    .append(Component.text(" (" + baseLoc.getWorld().getName() + ")", NamedTextColor.GRAY)));
        }

        List<OfflinePlayer> members = this.islandMemberManager.getIslandMembers(islandToInfo);
        if (members.isEmpty()) {
            player.sendMessage(Component.text("Üyeler: ", NamedTextColor.YELLOW).append(Component.text("Yok", NamedTextColor.GRAY)));
        } else {
            String memberNames = members.stream().map(op -> op.getName() != null ? op.getName() : op.getUniqueId().toString().substring(0,8)).collect(Collectors.joining(", "));
            player.sendMessage(Component.text("Üyeler (" + members.size() + "): ", NamedTextColor.YELLOW).append(Component.text(memberNames, NamedTextColor.WHITE)));
        }
        player.sendMessage(Component.text("Ev Sayısı: ", NamedTextColor.YELLOW).append(Component.text(islandToInfo.getNamedHomes().size() + "/" + plugin.getConfig().getInt("island.max-named-homes", 5), NamedTextColor.WHITE)));
        plugin.getLogger().info(String.format("Successfully processed /island %s for %s (target: %s)", subCommand, player.getName(), targetPlayerName));
    }

    private void handleSettingsCommand(Player player, String[] args) {
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island settings: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island settings <name|visibility|boundary> [değer]", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island settings: Not enough arguments.", player.getName()));
            return;
        }

        String settingType = args[1].toLowerCase();
        String settingValue = (args.length > 2) ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";

        switch (settingType) {
            case "name":
                if (args.length < 3) {
                    player.sendMessage(Component.text("Kullanım: /island settings name <yeni_isim>", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island settings name: Missing new name argument.", player.getName()));
                    return;
                }

                double settingsNameCost = plugin.getConfig().getDouble("commands.settings_name.cost", 50000.0);
                if (this.economy != null && settingsNameCost > 0) {
                    if (economy.getBalance(player) < settingsNameCost) {
                        player.sendMessage(Component.text("Ada ismini değiştirmek için yeterli paran yok! Gereken: " + economy.format(settingsNameCost), NamedTextColor.RED));
                        plugin.getLogger().warning(String.format("Player %s failed to execute /island settings name: Insufficient funds. Needed: %s", player.getName(), economy.format(settingsNameCost)));
                        return; 
                    }
                    EconomyResponse r = economy.withdrawPlayer(player, settingsNameCost);
                    if (r.transactionSuccess()) {
                        player.sendMessage(Component.text(economy.format(settingsNameCost) + " isim değiştirme ücreti olarak hesabından çekildi.", NamedTextColor.AQUA));
                    } else {
                        player.sendMessage(Component.text("İsim değiştirme ücreti çekilirken bir hata oluştu: " + r.errorMessage, NamedTextColor.RED));
                        plugin.getLogger().warning(String.format("Player %s failed to execute /island settings name: Economy withdraw error: %s", player.getName(), r.errorMessage));
                        return; 
                    }
                }

                String newName = settingValue;
                this.islandSettingsManager.setIslandName(player, island, newName);
                plugin.getLogger().info(String.format("Processed /island settings name %s for %s (actual logging in IslandSettingsManager)", newName, player.getName()));
                break;
            case "visibility":
            case "gizlilik":
                if (args.length < 3) {
                    player.sendMessage(Component.text("Kullanım: /island settings visibility <public|private>", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island settings %s: Missing visibility argument.", player.getName(), settingType));
                    return;
                }
                String visibility = args[2].toLowerCase();
                if (visibility.equals("public") || visibility.equals("herkeseacik")) {
                    this.islandSettingsManager.setIslandVisibility(player, island, true);
                    plugin.getLogger().info(String.format("Processed /island settings %s public for %s (actual logging in IslandSettingsManager)", settingType, player.getName()));
                } else if (visibility.equals("private") || visibility.equals("ozel")) {
                    this.islandSettingsManager.setIslandVisibility(player, island, false);
                    plugin.getLogger().info(String.format("Processed /island settings %s private for %s (actual logging in IslandSettingsManager)", settingType, player.getName()));
                } else {
                    player.sendMessage(Component.text("Geçersiz görünürlük türü. Kullanılabilir: public, private", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island settings %s: Invalid visibility type '%s'", player.getName(), settingType, visibility));
                }
                break;
            case "boundary":
            case "sınır":
                if (args.length == 2) {
                    this.islandSettingsManager.toggleIslandBoundaryEnforcement(player, island);
                    plugin.getLogger().info(String.format("Processed /island settings %s (toggle) for %s (actual logging in IslandSettingsManager)", settingType, player.getName()));
                } else if (args.length == 3) {
                    String desiredState = args[2].toLowerCase();
                    boolean currentBoundaryState = island.areBoundariesEnforced();
                    if ((desiredState.equals("on") || desiredState.equals("aktif"))) {
                        if (!currentBoundaryState) {
                            this.islandSettingsManager.toggleIslandBoundaryEnforcement(player, island);
                            plugin.getLogger().info(String.format("Processed /island settings %s on for %s (actual logging in IslandSettingsManager)", settingType, player.getName()));
                        } else {
                            player.sendMessage(Component.text("Ada sınırları zaten aktif.", NamedTextColor.YELLOW));
                            plugin.getLogger().info(String.format("Player %s attempted /island settings %s on, but already on.", player.getName(), settingType));
                        }
                    } else if ((desiredState.equals("off") || desiredState.equals("pasif"))) {
                        if (currentBoundaryState) {
                            this.islandSettingsManager.toggleIslandBoundaryEnforcement(player, island);
                            plugin.getLogger().info(String.format("Processed /island settings %s off for %s (actual logging in IslandSettingsManager)", settingType, player.getName()));
                        } else {
                            player.sendMessage(Component.text("Ada sınırları zaten pasif.", NamedTextColor.YELLOW));
                            plugin.getLogger().info(String.format("Player %s attempted /island settings %s off, but already off.", player.getName(), settingType));
                        }
                    } else {
                        player.sendMessage(Component.text("Geçersiz sınır komutu. Kullanım: /island settings boundary [on|off] veya sadece /island settings boundary", NamedTextColor.RED));
                        plugin.getLogger().warning(String.format("Player %s failed to execute /island settings %s: Invalid state '%s'", player.getName(), settingType, desiredState));
                    }
                } else {
                    player.sendMessage(Component.text("Kullanım: /island settings boundary [on|off]", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island settings %s: Invalid arguments.", player.getName(), settingType));
                }
                break;
            default:
                player.sendMessage(Component.text("Bilinmeyen ayar türü. Kullanılabilir: name, visibility, boundary", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island settings: Unknown setting type '%s'", player.getName(), settingType));
                break;
        }
    }

    @SuppressWarnings("deprecation") // Bukkit.getOfflinePlayer(String) kullanımı için
    private void handleTeamCommand(Player player, String[] args) {
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());

        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island team <add|remove|list>", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island team: Not enough arguments.", player.getName()));
            return;
        }
        String teamAction = args[1].toLowerCase();
        String targetPlayerName = (args.length > 2) ? args[2] : null;

        switch (teamAction) {
            case "add":
            case "ekle":
                if (island == null) {
                    player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s: Island object was null. UUID: %s", player.getName(), teamAction, player.getUniqueId()));
                    return;
                }
                if (args.length < 3) {
                    player.sendMessage(Component.text("Kullanım: /island team add <oyuncu_adı>", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s: Missing target player name.", player.getName(), teamAction));
                    return;
                }
                OfflinePlayer targetToAdd = Bukkit.getOfflinePlayer(targetPlayerName);
                if (targetToAdd == null || targetToAdd.getUniqueId() == null) {
                    player.sendMessage(Component.text("'" + targetPlayerName + "' adında geçerli bir oyuncu profili bulunamadı.", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s %s: Target player profile not found.", player.getName(), teamAction, targetPlayerName));
                    return;
                }
                this.islandMemberManager.addMember(island, targetToAdd, player);
                plugin.getLogger().info(String.format("Processed /island team %s %s for %s (actual logging in IslandMemberManager)", teamAction, targetPlayerName, player.getName()));
                break;
            case "remove":
            case "kick":
            case "at":
            case "çıkar":
                if (island == null) {
                    player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s: Island object was null. UUID: %s", player.getName(), teamAction, player.getUniqueId()));
                    return;
                }
                if (args.length < 3) {
                    player.sendMessage(Component.text("Kullanım: /island team remove <oyuncu_adı>", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s: Missing target player name.", player.getName(), teamAction));
                    return;
                }
                OfflinePlayer targetToRemove = Bukkit.getOfflinePlayer(targetPlayerName);
                if (targetToRemove == null || targetToRemove.getUniqueId() == null) {
                    player.sendMessage(Component.text("'" + targetPlayerName + "' adında geçerli bir oyuncu profili bulunamadı.", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s %s: Target player profile not found.", player.getName(), teamAction, targetPlayerName));
                    return;
                }
                this.islandMemberManager.removeMember(island, targetToRemove, player);
                plugin.getLogger().info(String.format("Processed /island team %s %s for %s (actual logging in IslandMemberManager)", teamAction, targetPlayerName, player.getName()));
                break;
            case "list":
            case "liste":
                if (island == null) {
                    player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island team %s: Island object was null. UUID: %s", player.getName(), teamAction, player.getUniqueId()));
                    return;
                }
                List<OfflinePlayer> members = this.islandMemberManager.getIslandMembers(island);
                if (members.isEmpty()) {
                    player.sendMessage(Component.text("Adanızda hiç üye yok.", NamedTextColor.YELLOW));
                } else {
                    String memberNames = members.stream()
                            .map(op -> op.getName() != null ? op.getName() : op.getUniqueId().toString().substring(0,8))
                            .collect(Collectors.joining(", "));
                    player.sendMessage(Component.text("Ada Üyeleri ("+ members.size() + "/" + plugin.getConfig().getInt("island.max-members",3) +"): ", NamedTextColor.GREEN)
                            .append(Component.text(memberNames, NamedTextColor.WHITE)));
                }
                plugin.getLogger().info(String.format("Successfully processed /island team %s for %s", teamAction, player.getName()));
                break;
            default:
                player.sendMessage(Component.text("Bilinmeyen takım komutu. Kullanılabilir: add, remove, list", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island team: Unknown team action '%s'", player.getName(), teamAction));
                break;
        }
    }

    @SuppressWarnings("deprecation") // Bukkit.getOfflinePlayer(String) kullanımı için
    private void handleVisitCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island visit <oyuncu_adı>", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island visit: Missing target player name.", player.getName()));
            return;
        }
        String ownerName = args[1];
        OfflinePlayer targetOwner = Bukkit.getOfflinePlayer(ownerName);
        if (targetOwner == null || targetOwner.getUniqueId() == null) {
            player.sendMessage(Component.text("'" + ownerName + "' adında geçerli bir oyuncu profili bulunamadı.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island visit %s: Target player profile not found.", player.getName(), ownerName));
            return;
        }

        Island targetIsland = this.islandDataHandler.getIslandByOwner(targetOwner.getUniqueId());
        if (targetIsland == null) {
            player.sendMessage(Component.text("'" + (targetOwner.getName() != null ? targetOwner.getName() : ownerName) + "' adlı oyuncunun bir adası bulunmuyor.", NamedTextColor.RED));
            plugin.getLogger().info(String.format("Player %s attempted to /island visit %s: Target player has no island.", player.getName(), ownerName));
            return;
        }

        if (targetIsland.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Zaten kendi adandasın. Gitmek için ", NamedTextColor.YELLOW)
                    .append(Component.text("/island go", NamedTextColor.GOLD))
                    .append(Component.text(" kullan.", NamedTextColor.YELLOW)));
            plugin.getLogger().info(String.format("Player %s attempted to /island visit %s, but it's their own island.", player.getName(), ownerName));
            return;
        }

        if (!targetIsland.canPlayerVisit(player)) {
            player.sendMessage(Component.text("'" + (targetOwner.getName() != null ? targetOwner.getName() : ownerName) + "' adlı oyuncunun adası ziyaret edilemez (muhtemelen özel).", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to /island visit %s: Island is not visitable (private or other restriction).", player.getName(), ownerName));
            return;
        }
        this.islandTeleportManager.teleportPlayerToVisitIsland(player, targetIsland);
        plugin.getLogger().info(String.format("Processed /island visit %s for %s (actual logging in IslandTeleportManager)", ownerName, player.getName()));
    }

    private void handleBiomeCommand(Player player, String[] args) {
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island biome: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island biome <set <biyom_adı> | get | list>", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island biome: Not enough arguments.", player.getName()));
            return;
        }

        String biomeAction = args[1].toLowerCase();
        if (this.islandBiomeManager == null) {
            player.sendMessage(Component.text("Biyom yöneticisi yüklenemedi. Lütfen sunucu yöneticisine bildirin.", NamedTextColor.RED));
            plugin.getLogger().severe("IslandCommand: IslandBiomeManager null geldi!");
            plugin.getLogger().warning(String.format("Player %s failed to execute /island biome %s: IslandBiomeManager is null.", player.getName(), biomeAction));
            return;
        }

        switch (biomeAction) {
            case "set":
                if (args.length < 3) {
                    player.sendMessage(Component.text("Kullanım: /island biome set <biyom_adı>", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island biome set: Missing biome name argument.", player.getName()));
                    return;
                }
                double biomeSetCost = plugin.getConfig().getDouble("commands.biome_set.cost", 10000.0);
                if (this.economy != null && biomeSetCost > 0) {
                    if (economy.getBalance(player) < biomeSetCost) {
                        player.sendMessage(Component.text("Ada biyomunu değiştirmek için yeterli paran yok! Gereken: " + economy.format(biomeSetCost), NamedTextColor.RED));
                        plugin.getLogger().warning(String.format("Player %s failed to execute /island biome set: Insufficient funds. Needed: %s", player.getName(), economy.format(biomeSetCost)));
                        return; 
                    }
                    EconomyResponse r = economy.withdrawPlayer(player, biomeSetCost);
                    if (r.transactionSuccess()) {
                        player.sendMessage(Component.text(economy.format(biomeSetCost) + " biyom değiştirme ücreti olarak hesabından çekildi.", NamedTextColor.AQUA));
                    } else {
                        player.sendMessage(Component.text("Biyom değiştirme ücreti çekilirken bir hata oluştu: " + r.errorMessage, NamedTextColor.RED));
                        plugin.getLogger().warning(String.format("Player %s failed to execute /island biome set: Economy withdraw error: %s", player.getName(), r.errorMessage));
                        return; 
                    }
                }
                String biomeName = args[2].toUpperCase();
                this.islandBiomeManager.setIslandBiome(player, island, biomeName);
                plugin.getLogger().info(String.format("Processed /island biome set %s for %s (actual logging in IslandBiomeManager)", biomeName, player.getName()));
                break;

            case "get":
                String currentBiome = this.islandBiomeManager.getIslandBiome(island);
                player.sendMessage(Component.text("Adanızın mevcut biyomu: ", NamedTextColor.YELLOW).append(Component.text(currentBiome, NamedTextColor.AQUA)));
                plugin.getLogger().info(String.format("Successfully processed /island biome get for %s. Current biome: %s", player.getName(), currentBiome));
                break;
            case "list":
                this.islandBiomeManager.sendAvailableBiomes(player);
                plugin.getLogger().info(String.format("Successfully processed /island biome list for %s", player.getName()));
                break;
            default:
                player.sendMessage(Component.text("Bilinmeyen biyom komutu. Kullanım: set, get, list", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island biome: Unknown biome action '%s'", player.getName(), biomeAction));
                break;
        }
    }

    private void handleWelcomeCommand(Player player, String[] args) {
        Island island = this.islandDataHandler.getIslandByOwner(player.getUniqueId());
        if (island == null) {
            player.sendMessage(Component.text("Ada verilerin bulunamadı. Lütfen tekrar dene veya sorun devam ederse bir yetkiliye bildir.", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island welcome: Island object was null. UUID: %s", player.getName(), player.getUniqueId()));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Kullanım: /island welcome <set <mesaj...> | clear | view>", NamedTextColor.RED));
            plugin.getLogger().warning(String.format("Player %s failed to execute /island welcome: Not enough arguments.", player.getName()));
            return;
        }

        String welcomeAction = args[1].toLowerCase();
        if (this.islandWelcomeManager == null) {
            player.sendMessage(Component.text("Karşılama mesajı yöneticisi yüklenemedi.", NamedTextColor.RED));
            plugin.getLogger().severe("IslandCommand: IslandWelcomeManager null!");
            plugin.getLogger().warning(String.format("Player %s failed to execute /island welcome %s: IslandWelcomeManager is null.", player.getName(), welcomeAction));
            return;
        }

        switch (welcomeAction) {
            case "set":
                if (args.length < 3) {
                    player.sendMessage(Component.text("Kullanım: /island welcome set <mesaj...>", NamedTextColor.RED));
                    plugin.getLogger().warning(String.format("Player %s failed to execute /island welcome set: Missing message argument.", player.getName()));
                    return;
                }
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    messageBuilder.append(args[i]).append(" ");
                }
                String message = messageBuilder.toString().trim();
                this.islandWelcomeManager.setWelcomeMessage(player, island, message);
                plugin.getLogger().info(String.format("Processed /island welcome set for %s (actual logging in IslandWelcomeManager)", player.getName()));
                break;
            case "clear":
                this.islandWelcomeManager.clearWelcomeMessage(player, island);
                plugin.getLogger().info(String.format("Processed /island welcome clear for %s (actual logging in IslandWelcomeManager)", player.getName()));
                break;
            case "view":
                this.islandWelcomeManager.viewWelcomeMessage(player, island);
                plugin.getLogger().info(String.format("Processed /island welcome view for %s (actual logging in IslandWelcomeManager)", player.getName()));
                break;
            default:
                player.sendMessage(Component.text("Bilinmeyen welcome komutu. Kullanım: set, clear, view", NamedTextColor.RED));
                plugin.getLogger().warning(String.format("Player %s failed to execute /island welcome: Unknown welcome action '%s'", player.getName(), welcomeAction));
                break;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        List<String> subCommands = new ArrayList<>(Arrays.asList(
                "create", "go", "sethome", "home", "delhome", "delete", "reset",
                "flags", "info", "settings", "team", "visit", "help", "biome", "welcome", "upgrade",
                "level", "worth"
        ));
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
        if (args.length == 2) {
            String arg1Lower = args[1].toLowerCase();
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
            if (subCmd.equals("upgrade")) {
                if ("homes".startsWith(arg1Lower)) {
                    completions.add("homes");
                }
            }
            switch (subCmd) {
                case "level":
                case "worth":
                    if ("calculate".startsWith(arg1Lower)) {
                        completions.add("calculate");
                    }
                    break;
            }
        }

        if (args.length == 3) {
            String actionOrSetting = args[1].toLowerCase();
            String arg2Lower = args[2].toLowerCase();
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
                Registry.BIOME.stream()
                        .filter(b -> !b.getKey().equals(NamespacedKey.minecraft("custom")) && !b.getKey().getKey().startsWith("the_void"))
                        .map(b -> b.getKey().getKey())
                        .filter(name ->
                                name.toLowerCase().startsWith(arg2Lower))
                        .forEach(completions::add);
            }
        }
        return completions;
    }
}