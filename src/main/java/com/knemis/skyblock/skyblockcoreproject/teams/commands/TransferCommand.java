package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Rank;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.gui.ConfirmationGUI;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TransferCommand<T extends Team, U extends keviinUser<T>> extends Command<T, U> {
    public TransferCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, keviinTeams<T, U> keviinTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", keviinTeams.getConfiguration().prefix)));
            return false;
        }
        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().mustBeOwnerToTransfer
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().notAPlayer
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        U targetUser = keviinTeams.getUserManager().getUser(targetPlayer);
        if (targetUser.getTeamID() != team.getId()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().userNotInYourTeam
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (targetPlayer.getUniqueId().equals(player.getUniqueId()) && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotTransferToYourself
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.openInventory(new ConfirmationGUI<>(() -> {
            targetUser.setUserRank(Rank.OWNER.getId());
            keviinTeams.getTeamManager().getTeamMembers(team).forEach(user1 -> {
                if (user1.getUserRank() == Rank.OWNER.getId() && user1 != targetUser) {
                    user1.setUserRank(keviinTeams.getUserRanks().keySet().stream().max(Integer::compareTo).orElse(1));
                }
                Player p = user1.getPlayer();
                if (p != null) {
                    p.sendMessage(StringUtils.color(keviinTeams.getMessages().ownershipTransferred
                            .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                            .replace("%old_owner%", user.getName())
                            .replace("%new_owner%", targetUser.getName())
                    ));
                }
            });
            getCooldownProvider().applyCooldown(player);
        }, keviinTeams).getInventory());
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, keviinTeams<T, U> keviinTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
