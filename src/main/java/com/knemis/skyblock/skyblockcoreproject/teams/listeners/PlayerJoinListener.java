package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class PlayerJoinListener<T extends Team, U extends SkyBlockProjectTeamsUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        user.setBypassing(false);
        user.initBukkitTask(SkyBlockProjectTeams);

        // Update the internal username in case of name change
        user.setName(event.getPlayer().getName());


        if (player.isOp() && SkyBlockProjectTeams.getConfiguration().patreonMessage) {
            Bukkit.getScheduler().runTaskLater(SkyBlockProjectTeams, () ->
                            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getConfiguration().prefix + " &7Thanks for using " + SkyBlockProjectTeams.getDescription().getName() + ", if you like the plugin, consider donating at " + SkyBlockProjectTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG"))
                    , 5);
        }

        // This isnt great, but as this requires database operations, we can pre-run it async, otherwise it will have to be loaded sync. I need to recode/rethink this eventually but this should fix some lag caused by missions for now
        SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> Bukkit.getScheduler().runTaskAsynchronously(SkyBlockProjectTeams, () -> SkyBlockProjectTeams.getMissionManager().generateMissionData(team)));
    }

}
