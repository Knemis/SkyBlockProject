package com.knemis.skyblock.skyblockcoreproject.teams.commands;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Rank;
import com.knemis.skyblock.skyblockcoreproject.teams.database.User;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.gui.ConfirmationGUI;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TransferCommand<T extends Team, U extends User<T>> extends Command<T, U> {
    public TransferCommand(List<String> args, String description, String syntax, String permission, long cooldownInSeconds) {
        super(args, description, syntax, permission, cooldownInSeconds);
    }

    @Override
    public boolean execute(U user, T team, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        Player player = user.getPlayer();
        if (args.length != 1) {
            player.sendMessage(StringUtils.color(syntax.replace("%prefix%", skyblockTeams.getConfiguration().prefix)));
            return false;
        }
        if (user.getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().mustBeOwnerToTransfer
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().notAPlayer
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        U targetUser = skyblockTeams.getUserManager().getUser(targetPlayer);
        if (targetUser.getTeamID() != team.getId()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().userNotInYourTeam
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }
        if (targetPlayer.getUniqueId().equals(player.getUniqueId()) && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(skyblockTeams.getMessages().cannotTransferToYourself
                    .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
            ));
            return false;
        }

        player.openInventory(new ConfirmationGUI<>(() -> {
            targetUser.setUserRank(Rank.OWNER.getId());
            skyblockTeams.getTeamManager().getTeamMembers(team).forEach(user1 -> { // TODO: Ensure TeamManager is functional
                if (user1.getUserRank() == Rank.OWNER.getId() && user1 != targetUser) {
                    user1.setUserRank(skyblockTeams.getUserRanks().keySet().stream().max(Integer::compareTo).orElse(1)); // TODO: Ensure getUserRanks is functional
                }
                Player p = user1.getPlayer();
                if (p != null) {
                    p.sendMessage(StringUtils.color(skyblockTeams.getMessages().ownershipTransferred
                            .replace("%prefix%", skyblockTeams.getConfiguration().prefix)
                            .replace("%old_owner%", user.getName())
                            .replace("%new_owner%", targetUser.getName())
                    ));
                }
            });
            getCooldownProvider().applyCooldown(player);  // TODO: Ensure CooldownProvider is functional
        }, skyblockTeams).getInventory());
        return false; // Typically, opening a GUI means the command execution might not be "complete" in the traditional sense.
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] args, SkyBlockTeams<T, U> skyblockTeams) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}
