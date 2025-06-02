package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamInvite;
import com.keviin.keviinteams.database.TeamSetting;
import com.keviin.keviinteams.enhancements.MembersEnhancementData;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class JoinCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public JoinCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        if (keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().alreadyHaveTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]);
        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamDoesntExistByName
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        TeamSetting teamSetting = keviinTeams.getTeamManager().getTeamSetting(team.get(), SettingType.TEAM_TYPE.getSettingKey());
        Optional<TeamInvite> teamInvite = keviinTeams.getTeamManager().getTeamInvite(team.get(), user);
        if (!teamInvite.isPresent() && !user.isBypassing() && teamSetting != null && !teamSetting.getValue().equalsIgnoreCase("public")) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noActiveInvite
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        MembersEnhancementData data = keviinTeams.getEnhancements().membersEnhancement.levels.get(keviinTeams.getTeamManager().getTeamEnhancement(team.get(), "members").getLevel());
        if (keviinTeams.getTeamManager().getTeamMembers(team.get()).size() >= (data == null ? 0 : data.members)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().memberLimitReached
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        user.setTeam(team.get());
        teamInvite.ifPresent(invite -> keviinTeams.getTeamManager().deleteTeamInvite(invite));

        player.sendMessage(StringUtils.color(keviinTeams.getMessages().joinedTeam
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%name%", team.get().getName())
        ));

        keviinTeams.getTeamManager().getTeamMembers(team.get()).stream()
                .map(U::getPlayer)
                .forEach(teamMember -> {
                    if (teamMember != null && teamMember != player) {
                        teamMember.sendMessage(StringUtils.color(keviinTeams.getMessages().userJoinedTeam
                                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                                .replace("%player%", player.getName())
                        ));
                    }
                });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
