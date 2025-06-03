package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
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
public class JoinCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public JoinCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        if (skyblockTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().alreadyHaveTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Optional<T> team = skyblockTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]); // TODO: Ensure TeamManager is functional
        if (!team.isPresent()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().teamDoesntExistByName
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        TeamSetting teamSetting = skyblockTeams.getTeamManager().getTeamSetting(team.get(), SettingType.TEAM_TYPE.getSettingKey()); // TODO: Ensure TeamManager and TeamSetting are functional
        Optional<TeamInvite> teamInvite = skyblockTeams.getTeamManager().getTeamInvite(team.get(), user); // TODO: Ensure TeamManager and TeamInvite are functional
        if (!teamInvite.isPresent() && !user.isBypassing() && teamSetting != null && !teamSetting.getValue().equalsIgnoreCase("public")) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().noActiveInvite
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        MembersEnhancementData data = skyblockTeams.getEnhancements().membersEnhancement.levels.get(skyblockTeams.getTeamManager().getTeamEnhancement(team.get(), "members").getLevel()); // TODO: Ensure Enhancements and TeamManager are functional
        if (skyblockTeams.getTeamManager().getTeamMembers(team.get()).size() >= (data == null ? 0 : data.members)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().memberLimitReached
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        user.setTeam(team.get());
        teamInvite.ifPresent(invite -> skyblockTeams.getTeamManager().deleteTeamInvite(invite)); // TODO: Ensure TeamManager is functional

        player.sendMessage(StringUtils.color(skyblockTeams.getMessages().joinedTeam
                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                .replace("%name%", team.get().getName())
        ));

        skyblockTeams.getTeamManager().getTeamMembers(team.get()).stream() // TODO: Ensure TeamManager is functional
                .map(U::getPlayer)
                .forEach(teamMember -> {
                    if (teamMember != null && teamMember != player) {
                        teamMember.sendMessage(StringUtils.color(skyblockTeams.getMessages().userJoinedTeam
                                .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                                .replace("%player%", player.getName())
                        ));
                    }
                });
        // player.sendMessage("Join command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
