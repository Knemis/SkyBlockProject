package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.PermissionType;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // Assuming Team is correct
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public class DescriptionCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public String adminPermission;

    public DescriptionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            // player.sendMessage("Invalid syntax."); // Placeholder
            return false;
        }
        Optional<T> team = skyblockTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]); // TODO: Ensure TeamManager is functional
        if (team.isPresent() && player.hasPermission(adminPermission)) {
            String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            changeDescription(team.get(), description, player, skyblockTeams);
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().changedPlayerDescription
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                    .replace("%name%", team.get().getName())
                    .replace("%description%", description)
            ));
            return true;
        }
        return super.execute(user, args, skyblockTeams);
        // player.sendMessage("Description admin command needs to be reimplemented after refactoring."); // Placeholder
        // return false; // Placeholder to align with the logic that super.execute might be called or not.
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (!skyblockTeams.getTeamManager().getTeamPermission(team, user, PermissionType.DESCRIPTION)) { // TODO: Ensure TeamManager is functional
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotChangeDescription
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        changeDescription(team, String.join(" ", arguments), player, skyblockTeams);
        // player.sendMessage("Description (user) command needs to be reimplemented after refactoring."); // Placeholder
        return true;
    }

    private void changeDescription(T team, String description, Player player, SkyBlockTeams<T, U> skyblockTeams) {
        team.setDescription(description); // TODO: Ensure Team set/save methods are functional
        skyblockTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member -> // TODO: Ensure TeamManager is functional
                member.sendMessage(StringUtils.color(skyblockTeams.getMessages().descriptionChanged
                        .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                        .replace("%description%", description)
                ))
        );
    }
}
