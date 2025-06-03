package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public class DescriptionCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public String adminPermission;

    public DescriptionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
            player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]); // TODO: Uncomment when TeamManager is refactored
        // if (team.isPresent() && player.hasPermission(adminPermission)) {
            // String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            // changeDescription(team.get(), description, player, SkyBlockProjectTeams);
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().changedPlayerDescription // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%name%", team.get().getName())
                    // .replace("%description%", description)
            // ));
            // return true;
        // }
        // return super.execute(user, args, SkyBlockProjectTeams);
        player.sendMessage("Description admin command needs to be reimplemented after refactoring."); // Placeholder
        return false; // Placeholder to align with the logic that super.execute might be called or not.
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.DESCRIPTION)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotChangeDescription // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // changeDescription(team, String.join(" ", arguments), player, SkyBlockProjectTeams);
        player.sendMessage("Description (user) command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    private void changeDescription(T team, String description, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // team.setDescription(description); // TODO: Uncomment when Team is refactored
        // SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member -> // TODO: Uncomment when TeamManager is refactored
                // member.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().descriptionChanged // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%player%", player.getName())
                        // .replace("%description%", description)
                // ))
        // );
    }
}
