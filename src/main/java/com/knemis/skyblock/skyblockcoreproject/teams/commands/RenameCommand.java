package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser; // TODO: Update to actual SkyBlockProjectUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public class RenameCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and SkyBlockProjectUser to actual classes
    public String adminPermission;

    public RenameCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
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
            // String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            // if(changeName(team.get(), name, player, SkyBlockProjectTeams)){
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().changedPlayerName // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%name%", team.get().getName())
                        // .replace("%player%", args[0])
                // ));
                // return true;
            // }
            // return false;
        // }
        // return super.execute(user, args, SkyBlockProjectTeams);
        player.sendMessage("Rename admin command needs to be reimplemented after refactoring."); // Placeholder
        return false; // Placeholder to align with the logic that super.execute might be called or not.
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (!SkyBlockProjectTeams.getTeamManager().getTeamPermission(team, user, PermissionType.RENAME)) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotChangeName // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // return changeName(team, String.join(" ", arguments), player, SkyBlockProjectTeams);
        player.sendMessage("Rename (user) command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    private boolean changeName(T team, String name, Player player, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        // Optional<T> teamViaName = SkyBlockProjectTeams.getTeamManager().getTeamViaName(name); // TODO: Uncomment when TeamManager is refactored
        // if (teamViaName.isPresent() && teamViaName.get().getId() != team.getId()) {
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameAlreadyExists // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // if (name.length() < SkyBlockProjectTeams.getConfiguration().minTeamNameLength) { // TODO: Uncomment when Configuration is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameTooShort // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%min_length%", String.valueOf(SkyBlockProjectTeams.getConfiguration().minTeamNameLength))
            // ));
            // return false;
        // }
        // if (name.length() > SkyBlockProjectTeams.getConfiguration().maxTeamNameLength) { // TODO: Uncomment when Configuration is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameTooLong // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%max_length%", String.valueOf(SkyBlockProjectTeams.getConfiguration().maxTeamNameLength))
            // ));
            // return false;
        // }
        // team.setName(name); // TODO: Uncomment when Team is refactored
        // SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member -> // TODO: Uncomment when TeamManager is refactored
                // member.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().nameChanged // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        // .replace("%player%", player.getName())
                        // .replace("%name%", name)
                // ))
        // );
        return true;
    }

}
