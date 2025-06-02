package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.TimeUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.commands.Command;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


public abstract class CommandManager<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements CommandExecutor, TabCompleter {

    @Getter
    private final List<Command<T, U>> commands = new ArrayList<>();
    @Getter
    private final String command;
    @Getter
    private final String color;
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public CommandManager(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams, String color, String command) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
        this.command = command;
        this.color = color;
        SkyBlockProjectTeams.getCommand(command).setExecutor(this);
        SkyBlockProjectTeams.getCommand(command).setTabCompleter(this);
        registerCommands();
    }

    public void registerCommands() {
        registerCommand(SkyBlockProjectTeams.getCommands().aboutCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().createCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().membersCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().permissionsCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().setPermissionCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().promoteCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().demoteCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().helpCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().reloadCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().inviteCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().unInviteCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().invitesCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().trustCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().unTrustCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().trustsCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().kickCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().leaveCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().deleteCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().infoCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().descriptionCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().renameCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().setHomeCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().homeCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().bypassCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().transferCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().joinCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().bankCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().depositCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().withdrawCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().chatCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().boostersCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().upgradesCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().flyCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().topCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().recalculateCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().warpsCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().warpCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().setWarpCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().deleteWarpCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().editWarpCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().missionsCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().rewardsCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().experienceCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().shopCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().settingsCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().blockValueCommand);
        registerCommand(SkyBlockProjectTeams.getCommands().levelCommand);
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
        commandSender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().unknownCommand
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
        ));
        return false;
    }

    public boolean executeCommand(CommandSender commandSender, Command<T, U> command, String[] args) {
        if (!command.hasPermission(commandSender, SkyBlockProjectTeams)) {
            commandSender.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noPermission
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (command.isOnCooldown(commandSender, SkyBlockProjectTeams)) {
            Duration remainingTime = command.getCooldownProvider().getRemainingTime(commandSender);
            String formattedTime = TimeUtils.formatDuration(SkyBlockProjectTeams.getMessages().activeCooldown, remainingTime);

            commandSender.sendMessage(StringUtils.color(formattedTime
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (command.execute(commandSender, args, SkyBlockProjectTeams)) {
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
                    if (command.hasPermission(commandSender, SkyBlockProjectTeams)) {
                        result.add(alias);
                    }
                }
            }
            return result.stream().sorted().collect(Collectors.toList());
        }

        for (Command<T, U> command : commands) {
            if(command.isSuperSecretCommand()) continue;
            if (!command.aliases.contains(args[0].toLowerCase())) continue;
            if (command.hasPermission(commandSender, SkyBlockProjectTeams)) {
                return command.onTabComplete(commandSender, Arrays.copyOfRange(args, 1, args.length), SkyBlockProjectTeams);
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
