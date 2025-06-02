package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class JoinCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public JoinCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        if (SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().alreadyHaveTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]);
        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamDoesntExistByName
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        TeamSetting teamSetting = SkyBlockProjectTeams.getTeamManager().getTeamSetting(team.get(), SettingType.TEAM_TYPE.getSettingKey());
        Optional<TeamInvite> teamInvite = SkyBlockProjectTeams.getTeamManager().getTeamInvite(team.get(), user);
        if (!teamInvite.isPresent() && !user.isBypassing() && teamSetting != null && !teamSetting.getValue().equalsIgnoreCase("public")) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noActiveInvite
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        MembersEnhancementData data = SkyBlockProjectTeams.getEnhancements().membersEnhancement.levels.get(SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team.get(), "members").getLevel());
        if (SkyBlockProjectTeams.getTeamManager().getTeamMembers(team.get()).size() >= (data == null ? 0 : data.members)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().memberLimitReached
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        user.setTeam(team.get());
        teamInvite.ifPresent(invite -> SkyBlockProjectTeams.getTeamManager().deleteTeamInvite(invite));

        player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().joinedTeam
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%name%", team.get().getName())
        ));

        SkyBlockProjectTeams.getTeamManager().getTeamMembers(team.get()).stream()
                .map(U::getPlayer)
                .forEach(teamMember -> {
                    if (teamMember != null && teamMember != player) {
                        teamMember.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userJoinedTeam
                                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                                .replace("%player%", player.getName())
                        ));
                    }
                });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
