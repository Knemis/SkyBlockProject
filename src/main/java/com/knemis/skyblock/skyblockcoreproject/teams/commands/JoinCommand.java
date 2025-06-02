package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.SettingType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite; // TODO: Update to actual TeamInvite class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamSetting; // TODO: Update to actual TeamSetting class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData; // TODO: Update to actual MembersEnhancementData class
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class JoinCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public JoinCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", iridiumTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // if (iridiumTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().alreadyHaveTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // Optional<T> team = iridiumTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]); // TODO: Uncomment when TeamManager is refactored
        // if (!team.isPresent()) {
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamDoesntExistByName // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // TeamSetting teamSetting = iridiumTeams.getTeamManager().getTeamSetting(team.get(), SettingType.TEAM_TYPE.getSettingKey()); // TODO: Uncomment when TeamManager and TeamSetting are refactored
        // Optional<com.knemis.skyblock.skyblockcoreproject.teams.database.TeamInvite> teamInvite = iridiumTeams.getTeamManager().getTeamInvite(team.get(), user); // TODO: Uncomment when TeamManager and TeamInvite are refactored
        // if (!teamInvite.isPresent() && !user.isBypassing() && teamSetting != null && !teamSetting.getValue().equalsIgnoreCase("public")) { // TODO: Uncomment when teamInvite and teamSetting are available
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().noActiveInvite // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.MembersEnhancementData data = iridiumTeams.getEnhancements().membersEnhancement.levels.get(iridiumTeams.getTeamManager().getTeamEnhancement(team.get(), "members").getLevel()); // TODO: Uncomment when Enhancements and TeamManager are refactored
        // if (iridiumTeams.getTeamManager().getTeamMembers(team.get()).size() >= (data == null ? 0 : data.members)) { // TODO: Uncomment when TeamManager and data are available
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().memberLimitReached // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        // user.setTeam(team.get()); // TODO: Uncomment when team is available
        // teamInvite.ifPresent(invite -> iridiumTeams.getTeamManager().deleteTeamInvite(invite)); // TODO: Uncomment when teamInvite and TeamManager are available

        // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().joinedTeam // TODO: Replace StringUtils.color
                // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                // .replace("%name%", team.get().getName())
        // ));

        // iridiumTeams.getTeamManager().getTeamMembers(team.get()).stream() // TODO: Uncomment when TeamManager and team are available
                // .map(U::getPlayer)
                // .forEach(teamMember -> {
                    // if (teamMember != null && teamMember != player) {
                        // teamMember.sendMessage(StringUtils.color(iridiumTeams.getMessages().userJoinedTeam // TODO: Replace StringUtils.color
                                // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                                // .replace("%player%", player.getName())
                        // ));
                    // }
                // });
        player.sendMessage("Join command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
