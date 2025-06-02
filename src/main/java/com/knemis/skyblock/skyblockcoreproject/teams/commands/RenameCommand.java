package com.knemis.skyblock.skyblockcoreproject.teams.commands;

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
public class RenameCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public String adminPermission;

    public RenameCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds, String adminPermission) {
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
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if(changeName(team.get(), name, player, keviinTeams)){
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().changedPlayerName
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%name%", team.get().getName())
                        .replace("%player%", args[0])
                ));
                return true;
            }
            return false;
        }
        return super.execute(user, args, keviinTeams);
    }

    @Override
    public boolean execute(U user, T team, String[] arguments, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (!keviinTeams.getTeamManager().getTeamPermission(team, user, PermissionType.RENAME)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotChangeName
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        return changeName(team, String.join(" ", arguments), player, keviinTeams);
    }

    private boolean changeName(T team, String name, Player player, keviinTeams<T, U> keviinTeams) {
        Optional<T> teamViaName = keviinTeams.getTeamManager().getTeamViaName(name);
        if (teamViaName.isPresent() && teamViaName.get().getId() != team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamNameAlreadyExists
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (name.length() < keviinTeams.getConfiguration().minTeamNameLength) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamNameTooShort
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%min_length%", String.valueOf(keviinTeams.getConfiguration().minTeamNameLength))
            ));
            return false;
        }
        if (name.length() > keviinTeams.getConfiguration().maxTeamNameLength) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamNameTooLong
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%max_length%", String.valueOf(keviinTeams.getConfiguration().maxTeamNameLength))
            ));
            return false;
        }
        team.setName(name);
        keviinTeams.getTeamManager().getTeamMembers(team).stream().map(U::getPlayer).filter(Objects::nonNull).forEach(member ->
                member.sendMessage(StringUtils.color(keviinTeams.getMessages().nameChanged
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%player%", player.getName())
                        .replace("%name%", name)
                ))
        );
        return true;
    }

}
