package com.keviin.keviinteams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.PermissionType;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
public class DescriptionCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public String adminPermission;

    public DescriptionCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
        super(args, description, syntax, permission, cooldownInSeconds);
        this.adminPermission = adminPermission;
    }

    @Override
    public boolean execute(U user, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length == 0) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaNameOrPlayer(args[0]);
        if (team.isPresent() && player.hasPermission(adminPermission)) {
            String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            changeDescription(team.get(), description, player, keviinTeams);
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().changedPlayerDescription
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%name%", team.get().getName())
                    .replace("%description%", description)
            ));
            return true;
        }
        return super.execute(user, args, keviinTeams);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.DESCRIPTION)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotChangeDescription
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        changeDescription(team, String.join(" ", arguments), player, keviinTeams);
        return true;
    }

    private void changeDescription(T team, String description, Player player, keviinTeams<T, U> keviinTeams) {
        team.setDescription(description);
        keviinTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member ->
                member.sendMessage(StringUtils.color(keviinTeams.getMessages().descriptionChanged
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                        .replace("%description%", description)
                ))
        );
    }
}
