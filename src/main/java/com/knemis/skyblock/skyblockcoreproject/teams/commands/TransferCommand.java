package com.knemis.skyblock.skyblockcoreproject.teams.commands;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ConfirmationGUI;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TransferCommand<T extends Team, U extends SkyBlockProjectTeamsUser<T>> extends Command<T, U> {
    public TransferCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
            return false;
        }
        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().mustBeOwnerToTransfer
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notAPlayer
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        U targetUser = SkyBlockProjectTeams.getUserManager().getUser(targetPlayer);
        if (targetUser.getTeamID() != team.getId()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().userNotInYourTeam
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (targetPlayer.getUniqueId().equals(player.getUniqueId()) && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotTransferToYourself
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.openInventory(new ConfirmationGUI<>(() -> {
            targetUser.setUserRank(Rank.OWNER.getId());
            SkyBlockProjectTeams.getTeamManager().getTeamMembers(team).forEach(user1 -> {
                if (user1.getUserRank() == Rank.OWNER.getId() && user1 != targetUser) {
                    user1.setUserRank(SkyBlockProjectTeams.getUserRanks().keySet().stream().max(Integer::compareTo).orElse(1));
                }
                Player p = user1.getPlayer();
                if (p != null) {
                    p.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().ownershipTransferred
                            .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                            .replace("%old_owner%", user.getName())
                            .replace("%new_owner%", targetUser.getName())
                    ));
                }
            });
            getCooldownProvider().applyCooldown(player);
        }, SkyBlockProjectTeams).getInventory());
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
