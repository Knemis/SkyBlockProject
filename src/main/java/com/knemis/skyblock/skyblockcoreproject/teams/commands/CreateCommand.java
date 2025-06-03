package com.knemis.skyblock.skyblockcoreproject.teams.commands;

// import com.keviin.keviincore.utils.StringUtils; // TODO: Replace StringUtils.color
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
// import com.knemis.skyblock.skyblockcoreproject.teams.database.IridiumUser; // TODO: Update to actual IridiumUser class
// import com.knemis.skyblock.skyblockcoreproject.teams.database.Team; // TODO: Update to actual Team class
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class CreateCommand<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team, U extends com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser<T>> extends Command<T, U> { // TODO: Update Team and IridiumUser to actual classes
    public CreateCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        // if (SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).isPresent()) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().alreadyHaveTeam // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }

        if (args.length < 1) {
            // if (SkyBlockProjectTeams.getConfiguration().createRequiresName) { // TODO: Uncomment when Configuration is refactored
                // player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix))); // TODO: Replace StringUtils.color
                // return false;
            // }
            // SkyBlockProjectTeams.getTeamManager().createTeam(player, null).thenAccept(team -> { // TODO: Uncomment when TeamManager is refactored
                // if (team == null) return;
                // user.setUserRank(Rank.OWNER.getId());
                // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamCreated // TODO: Replace StringUtils.color
                        // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                // ));
                // getCooldownProvider().applyCooldown(player); // TODO: Uncomment when CooldownProvider is refactored
            // });
            player.sendMessage("Create command (no name) needs to be reimplemented after refactoring."); // Placeholder
            return false;
        }

        String teamName = String.join(" ", args);
        // if (teamName.length() < SkyBlockProjectTeams.getConfiguration().minTeamNameLength) { // TODO: Uncomment when Configuration is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameTooShort // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%min_length%", String.valueOf(SkyBlockProjectTeams.getConfiguration().minTeamNameLength))
            // ));
            // return false;
        // }
        // if (teamName.length() > SkyBlockProjectTeams.getConfiguration().maxTeamNameLength) { // TODO: Uncomment when Configuration is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameTooLong // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    // .replace("%max_length%", String.valueOf(SkyBlockProjectTeams.getConfiguration().maxTeamNameLength))
            // ));
            // return false;
        // }
        // if (SkyBlockProjectTeams.getTeamManager().getTeamViaName(teamName).isPresent()) { // TODO: Uncomment when TeamManager is refactored
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamNameAlreadyExists // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // return false;
        // }
        // SkyBlockProjectTeams.getTeamManager().createTeam(player, teamName).thenAccept(team -> { // TODO: Uncomment when TeamManager is refactored
            // if (team == null) return;
            // user.setUserRank(Rank.OWNER.getId());
            // player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamCreated // TODO: Replace StringUtils.color
                    // .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            // ));
            // getCooldownProvider().applyCooldown(player); // TODO: Uncomment when CooldownProvider is refactored
        // });
        player.sendMessage("Create command (with name) needs to be reimplemented after refactoring."); // Placeholder
        return false;
    }

}
