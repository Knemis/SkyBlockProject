package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor

public class SetHomeCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public SetHomeCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        // if (iridiumTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0) != team.getId()) { // TODO: Uncomment when TeamManager and Team are refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().notInTeamLand // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (!iridiumTeams.getTeamManager().getTeamPermission(team, user, PermissionType.SETHOME)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(iridiumTeams.getMessages().cannotSetHome // TODO: Replace StringUtils.color
                    // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // team.setHome(player.getLocation()); // TODO: Uncomment when Team is refactored
        // iridiumTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member -> // TODO: Uncomment when TeamManager is refactored
                // member.sendMessage(StringUtils.color(iridiumTeams.getMessages().homeSet // TODO: Replace StringUtils.color
                        // .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                        // .replace("%player%", player.getName())
                // ))
        // );
        player.sendMessage("SetHome command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

}
