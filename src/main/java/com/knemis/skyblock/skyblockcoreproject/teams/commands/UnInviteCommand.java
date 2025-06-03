package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class UnInviteCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public UnInviteCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }
        U offlinePlayer = skyblockTeams.getUserManager().getUser(Bukkit.getServer().getOfflinePlayer(args[0])); // TODO: Ensure UserManager is functional
        Optional<TeamInvite> teamInvite = skyblockTeams.getTeamManager().getTeamInvite(team, offlinePlayer); // TODO: Ensure TeamManager is functional
        if (!teamInvite.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noActiveInvite.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }

        skyblockTeams.getTeamManager().deleteTeamInvite(teamInvite.get()); // TODO: Ensure TeamManager is functional
        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamInviteRevoked
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%player%", offlinePlayer.getName())
        ));
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
