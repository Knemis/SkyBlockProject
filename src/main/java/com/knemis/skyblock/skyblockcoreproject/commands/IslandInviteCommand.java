package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.island.IslandInviteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandInviteCommand implements CommandExecutor {

    private final IslandInviteManager inviteManager;

    public IslandInviteCommand(IslandInviteManager manager) {
        this.inviteManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cKimi davet edeceğini yazmalısın!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cOyuncu bulunamadı.");
            return true;
        }

        inviteManager.invitePlayer(player, target);
        player.sendMessage("§a" + target.getName() + " adlı oyuncu davet edildi!");
        target.sendMessage("§e" + player.getName() + " seni adasına davet etti! /island visit komutunu kullan.");
        return true;
    }
}
