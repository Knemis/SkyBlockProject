package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.island.IslandInviteManager;
import com.knemis.skyblock.skyblockcoreproject.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class IslandVisitCommand implements CommandExecutor {

    private final IslandInviteManager inviteManager;
    private final IslandManager islandManager;
    private final HashSet<UUID> allowVisit = new HashSet<>();

    public IslandVisitCommand(IslandInviteManager inviteManager, IslandManager islandManager) {
        this.inviteManager = inviteManager;
        this.islandManager = islandManager;
    }

    public boolean toggleVisit(UUID playerUUID) {
        if (allowVisit.contains(playerUUID)) {
            allowVisit.remove(playerUUID);
            return false;
        } else {
            allowVisit.add(playerUUID);
            return true;
        }
    }

    public boolean isVisitEnabled(UUID playerUUID) {
        return allowVisit.contains(playerUUID);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player visitor = (Player) sender;

        if (args.length == 0) {
            visitor.sendMessage("§cHedef oyuncu veya on/off yazılmalı.");
            return true;
        }

        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
            boolean enabled = toggleVisit(visitor.getUniqueId());
            visitor.sendMessage(enabled ? "§aZiyaret izni açıldı!" : "§cZiyaret izni kapatıldı!");
            return true;
        }

        Player owner = Bukkit.getPlayer(args[0]);
        if (owner == null) {
            visitor.sendMessage("§cOyuncu bulunamadı.");
            return true;
        }

        if (!inviteManager.hasInvite(visitor)) {
            visitor.sendMessage("§cBu oyuncu seni davet etmedi.");
            return true;
        }

        if (!isVisitEnabled(owner.getUniqueId())) {
            visitor.sendMessage("§cOyuncu ziyaret iznini kapatmış.");
            return true;
        }

        Location islandSpawn = islandManager.getIslandSpawn(owner.getUniqueId()); // Bu metodu IslandManager'da oluşturman gerek
        if (islandSpawn != null) {
            visitor.teleport(islandSpawn);
            owner.sendMessage("§e" + visitor.getName() + " adanı ziyaret etti.");
            Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SkyBlockProject"), // Plugin ismi doğru olmalı
                () -> {
                    if (!visitor.isOnline()) return;
                    owner.sendMessage("§e" + visitor.getName() + " adandan ayrıldı.");
                },
                20 * 60 // 1 dakika sonra (örnek)
            );
        }

        inviteManager.removeInvite(visitor);
        return true;
    }
}
