package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Reward;
import com.keviin.keviinteams.api.TeamLevelUpEvent;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamReward;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TeamLevelUpListener<T extends Team, U extends keviinUser<T>> implements Listener {
    private final keviinTeams<T, U> keviinTeams;

    @EventHandler(ignoreCancelled = true)
    public void onTeamLevelUp(TeamLevelUpEvent<T, U> event) {
        for (U member : keviinTeams.getTeamManager().getTeamMembers(event.getTeam())) {
            Player player = member.getPlayer();
            if(player == null) return;
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().teamLevelUp
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                    .replace("%level%", String.valueOf(event.getTeam().getLevel()))
            ));
        }

        if (event.isFirstTimeAsLevel() && event.getLevel() > 1) {
            if(!keviinTeams.getConfiguration().giveLevelRewards) return;
            Reward reward = null;
            List<Map.Entry<Integer, Reward>> entries = keviinTeams.getConfiguration().levelRewards.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
            for (Map.Entry<Integer, Reward> entry : entries) {
                if (event.getLevel() % entry.getKey() == 0) {
                    reward = entry.getValue();
                }
            }
            if (reward != null) {
                reward.item.lore = StringUtils.processMultiplePlaceholders(reward.item.lore, keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(event.getTeam()));
                reward.item.displayName = StringUtils.processMultiplePlaceholders(reward.item.displayName, keviinTeams.getTeamsPlaceholderBuilder().getPlaceholders(event.getTeam()));
                keviinTeams.getTeamManager().addTeamReward(new TeamReward(event.getTeam(), reward));
            }
        }
    }
}
