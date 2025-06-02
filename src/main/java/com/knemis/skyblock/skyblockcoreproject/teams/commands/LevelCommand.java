package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class LevelCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {

    public LevelCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            Optional<T> userTeam = keviinTeams.getTeamManager().getTeamViaID(user.getTeamID());
            if (!userTeam.isPresent()) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().dontHaveTeam
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                ));
                return false;
            }
            sendTeamLevel(player, userTeam.get(), keviinTeams);
            return true;
        }

        Optional<T> team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(String.join(" ", args));
        if(args[0].equals("location")) {
            team = keviinTeams.getTeamManager().getTeamViaPlayerLocation(player);
        }

        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDoesntExistByName
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        sendTeamLevel(player, team.get(), keviinTeams);
        return true;
    }

    public void sendTeamLevel(Player player, T team, keviinTeams<T, U> keviinTeams) {
        List<Placeholder> placeholderList = keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(team);
        player.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(StringUtils.processMultiplePlaceholders(keviinTeams.getConfiguration().teamInfoTitle, placeholderList), keviinTeams.getConfiguration().teamInfoTitleFiller)));
        for (String line : keviinTeams.getConfiguration().levelInfo) {
            player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(line, placeholderList)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}