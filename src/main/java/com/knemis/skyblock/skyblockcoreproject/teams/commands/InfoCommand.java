package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class InfoCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public InfoCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            Optional<T> userTeam = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());
            if (!userTeam.isPresent()) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return false;
            }
            sendTeamInfo(player, userTeam.get(), SkyBlockProjectTeams);
            return true;
        }

        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(String.join(" ", args));
        if (args[0].equals("location")) {
            team = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player);
        }

        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        sendTeamInfo(player, team.get(), SkyBlockProjectTeams);
        return true;
    }

    public void sendTeamInfo(Player player, T team, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        List<Placeholder> placeholderList = SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(team);
        player.sendMessage(StringUtils.color(StringUtils.getCenteredMessage(StringUtils.processMultiplePlaceholders(SkyBlockProjectTeams.getConfiguration().teamInfoTitle, placeholderList), SkyBlockProjectTeams.getConfiguration().teamInfoTitleFiller)));
        for (String line : SkyBlockProjectTeams.getConfiguration().teamInfo) {
            player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(line, placeholderList)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
