package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class CreateCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public CreateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().alreadyHaveTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        if (args.length < 1) {
            if (SkyBlockProjectTeams.getConfiguration().createRequiresName) {
                player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
                return false;
            }
            SkyBlockProjectTeams.getTeamManager().createTeam(player, null).thenAccept(team -> {
                if (team == null) return;
                user.setUserRank(Rank.OWNER.getId());
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamCreated
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                getCooldownProvider().applyCooldown(player);
            });
            return false;
        }

        String teamName = String.join(" ", args);
        if (teamName.length() < SkyBlockProjectTeams.getConfiguration().minTeamNameLength) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameTooShort
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%min_length%", String.valueOf(SkyBlockProjectTeams.getConfiguration().minTeamNameLength))
            ));
            return false;
        }
        if (teamName.length() > SkyBlockProjectTeams.getConfiguration().maxTeamNameLength) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameTooLong
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%max_length%", String.valueOf(SkyBlockProjectTeams.getConfiguration().maxTeamNameLength))
            ));
            return false;
        }
        if (SkyBlockProjectTeams.getTeamManager().getTeamViaName(teamName).isPresent()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameAlreadyExists
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        SkyBlockProjectTeams.getTeamManager().createTeam(player, teamName).thenAccept(team -> {
            if (team == null) return;
            user.setUserRank(Rank.OWNER.getId());
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamCreated
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            getCooldownProvider().applyCooldown(player);
        });
        return false;
    }

}
