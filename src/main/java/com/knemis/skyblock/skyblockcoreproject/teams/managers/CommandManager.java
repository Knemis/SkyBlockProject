package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviincore.utils.TimeUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.commands.Command;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandManager<T extends Team, U extends keviinUser<T>> implements CommandExecutor, TabCompleter {

    @Getter
    private final List<Command<T, U>> commands = new ArrayList<>();
    @Getter
    private final String command;
    @Getter
    private final String color;
    private final keviinTeams<T, U> keviinTeams;

    public CommandManager(keviinTeams<T, U> keviinTeams, String color, String command) {
        this.keviinTeams = keviinTeams;
        this.command = command;
        this.color = color;
        keviinTeams.getCommand(command).setExecutor(this);
        keviinTeams.getCommand(command).setTabCompleter(this);
        registerCommands();
    }

    public void registerCommands() {
        registerCommand(keviinTeams.getCommands().aboutCommand);
        registerCommand(keviinTeams.getCommands().createCommand);
        registerCommand(keviinTeams.getCommands().membersCommand);
        registerCommand(keviinTeams.getCommands().permissionsCommand);
        registerCommand(keviinTeams.getCommands().setPermissionCommand);
        registerCommand(keviinTeams.getCommands().promoteCommand);
        registerCommand(keviinTeams.getCommands().demoteCommand);
        registerCommand(keviinTeams.getCommands().helpCommand);
        registerCommand(keviinTeams.getCommands().reloadCommand);
        registerCommand(keviinTeams.getCommands().inviteCommand);
        registerCommand(keviinTeams.getCommands().unInviteCommand);
        registerCommand(keviinTeams.getCommands().invitesCommand);
        registerCommand(keviinTeams.getCommands().trustCommand);
        registerCommand(keviinTeams.getCommands().unTrustCommand);
        registerCommand(keviinTeams.getCommands().trustsCommand);
        registerCommand(keviinTeams.getCommands().kickCommand);
        registerCommand(keviinTeams.getCommands().leaveCommand);
        registerCommand(keviinTeams.getCommands().deleteCommand);
        registerCommand(keviinTeams.getCommands().infoCommand);
        registerCommand(keviinTeams.getCommands().descriptionCommand);
        registerCommand(keviinTeams.getCommands().renameCommand);
        registerCommand(keviinTeams.getCommands().setHomeCommand);
        registerCommand(keviinTeams.getCommands().homeCommand);
        registerCommand(keviinTeams.getCommands().bypassCommand);
        registerCommand(keviinTeams.getCommands().transferCommand);
        registerCommand(keviinTeams.getCommands().joinCommand);
        registerCommand(keviinTeams.getCommands().bankCommand);
        registerCommand(keviinTeams.getCommands().depositCommand);
        registerCommand(keviinTeams.getCommands().withdrawCommand);
        registerCommand(keviinTeams.getCommands().chatCommand);
        registerCommand(keviinTeams.getCommands().boostersCommand);
        registerCommand(keviinTeams.getCommands().upgradesCommand);
        registerCommand(keviinTeams.getCommands().flyCommand);
        registerCommand(keviinTeams.getCommands().topCommand);
        registerCommand(keviinTeams.getCommands().recalculateCommand);
        registerCommand(keviinTeams.getCommands().warpsCommand);
        registerCommand(keviinTeams.getCommands().warpCommand);
        registerCommand(keviinTeams.getCommands().setWarpCommand);
        registerCommand(keviinTeams.getCommands().deleteWarpCommand);
        registerCommand(keviinTeams.getCommands().editWarpCommand);
        registerCommand(keviinTeams.getCommands().missionsCommand);
        registerCommand(keviinTeams.getCommands().rewardsCommand);
        registerCommand(keviinTeams.getCommands().experienceCommand);
        registerCommand(keviinTeams.getCommands().shopCommand);
        registerCommand(keviinTeams.getCommands().settingsCommand);
        registerCommand(keviinTeams.getCommands().blockValueCommand);
        registerCommand(keviinTeams.getCommands().levelCommand);
    }

    public void registerCommand(Command<T, U> command) {
        if (!command.enabled) return;
        int index = Collections.binarySearch(commands, command, Comparator.comparing(cmd -> cmd.aliases.get(0)));
        commands.add(index < 0 ? -(index + 1) : index, command);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            noArgsDefault(commandSender);
            return true;
        }

        for (Command<T, U> command : commands) {
            // We don't want to execute other commands or ones that are disabled
            if (!command.aliases.contains(args[0])) continue;

            return executeCommand(commandSender, command, Arrays.copyOfRange(args, 1, args.length));
        }

        // Unknown command message
        commandSender.sendMessage(StringUtils.color(keviinTeams.getMessages().unknownCommand
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
        ));
        return false;
    }

    public boolean executeCommand(CommandSender commandSender, Command<T, U> command, String[] args) {
        if (!command.hasPermission(commandSender, keviinTeams)) {
            commandSender.sendMessage(StringUtils.color(keviinTeams.getMessages().noPermission
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (command.isOnCooldown(commandSender, keviinTeams)) {
            Duration remainingTime = command.getCooldownProvider().getRemainingTime(commandSender);
            String formattedTime = TimeUtils.formatDuration(keviinTeams.getMessages().activeCooldown, remainingTime);

            commandSender.sendMessage(StringUtils.color(formattedTime
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (command.execute(commandSender, args, keviinTeams)) {
            command.getCooldownProvider().applyCooldown(commandSender);
        }
        return true;
    }

    public abstract void noArgsDefault(@NotNull CommandSender commandSender);

    private List<String> getTabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 1) {
            ArrayList<String> result = new ArrayList<>();
            for (Command<T, U> command : commands) {
                if(command.isSuperSecretCommand()) continue;
                for (String alias : command.aliases) {
                    if (!alias.toLowerCase().startsWith(args[0].toLowerCase())) continue;
                    if (command.hasPermission(commandSender, keviinTeams)) {
                        result.add(alias);
                    }
                }
            }
            return result.stream().sorted().collect(Collectors.toList());
        }

        for (Command<T, U> command : commands) {
            if(command.isSuperSecretCommand()) continue;
            if (!command.aliases.contains(args[0].toLowerCase())) continue;
            if (command.hasPermission(commandSender, keviinTeams)) {
                return command.onTabComplete(commandSender, Arrays.copyOfRange(args, 1, args.length), keviinTeams);
            }
        }

        // We currently don't want to tab-completion here
        // Return a new List so it isn't a list of online players
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> tabComplete = getTabComplete(commandSender, args);
        if (tabComplete == null) return null;
        return tabComplete.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
