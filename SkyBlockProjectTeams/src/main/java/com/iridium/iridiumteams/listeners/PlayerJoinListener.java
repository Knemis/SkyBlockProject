package com.keviin.keviinteams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class PlayerJoinListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        U user = keviinTeams.getUserManager().getUser(player);
        user.setBypassing(false);
        user.initBukkitTask(keviinTeams);

        // Update the internal username in case of name change
        user.setName(event.getPlayer().getName());


        if (player.isOp() && keviinTeams.getConfiguration().patreonMessage) {
            Bukkit.getScheduler().runTaskLater(keviinTeams, () ->
                            player.sendMessage(StringUtils.color(keviinTeams.getConfiguration().prefix + " &7Thanks for using " + keviinTeams.getDescription().getName() + ", if you like the plugin, consider donating at " + keviinTeams.getCommandManager().getColor() + "www.patreon.com/Peaches_MLG"))
                    , 5);
        }

        // This isnt great, but as this requires database operations, we can pre-run it async, otherwise it will have to be loaded sync. I need to recode/rethink this eventually but this should fix some lag caused by missions for now
        keviinTeams.getTeamManager().getTeamViaID(user.getTeamID()).ifPresent(team -> Bukkit.getScheduler().runTaskAsynchronously(keviinTeams, () -> keviinTeams.getMissionManager().generateMissionData(team)));
    }

}
