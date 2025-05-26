package com.knemis.skyblock.skyblockcoreproject.commands;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.missions.MissionCategory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MissionCommand implements CommandExecutor, TabCompleter {

    private final SkyBlockProject plugin;

    public MissionCommand(SkyBlockProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getMissionGUIManager() == null) {
            player.sendMessage(ChatColor.RED + "Missions system is currently unavailable. Please try again later.");
            plugin.getLogger().severe("MissionGUIManager is null! Cannot open missions GUI.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("open")) {
            plugin.getMissionGUIManager().openMainMissionGui(player, MissionCategory.AVAILABLE, 1);
            return true;
        }

        // Potentially add other subcommands later, e.g., /missions list [category]
        // For now, only the GUI is supported.
        player.sendMessage(ChatColor.RED + "Usage: /" + label + " [gui|open]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = Arrays.asList("gui", "open");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(); // No other arguments supported yet
    }
}
