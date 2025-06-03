package com.knemis.skyblock.skyblockcoreproject.teams.listeners;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Reward;
import com.knemis.skyblock.skyblockcoreproject.teams.api.TeamLevelUpEvent;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamReward;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TeamLevelUpListener<T extends Team, U extends SkyBlockProjectUser<T>> implements Listener {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    @EventHandler(ignoreCancelled = true)
    public void onTeamLevelUp(TeamLevelUpEvent<T, U> event) {
        for (U member : SkyBlockProjectTeams.getTeamManager().getTeamMembers(event.getTeam())) {
            Player player = member.getPlayer();
            if(player == null) return;
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().teamLevelUp
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                    .replace("%level%", String.valueOf(event.getTeam().getLevel()))
            ));
        }

        if (event.isFirstTimeAsLevel() && event.getLevel() > 1) {
            if(!SkyBlockProjectTeams.getConfiguration().giveLevelRewards) return;
            Reward reward = null;
            List<Map.Entry<Integer, Reward>> entries = SkyBlockProjectTeams.getConfiguration().levelRewards.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
            for (Map.Entry<Integer, Reward> entry : entries) {
                if (event.getLevel() % entry.getKey() == 0) {
                    reward = entry.getValue();
                }
            }
            if (reward != null) {
                reward.item.lore = StringUtils.processMultiplePlaceholders(reward.item.lore, SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(event.getTeam()));
                reward.item.displayName = StringUtils.processMultiplePlaceholders(reward.item.displayName, SkyBlockProjectTeams.getTeamsPlaceholderBuilder().getPlaceholders(event.getTeam()));
                SkyBlockProjectTeams.getTeamManager().addTeamReward(new TeamReward(event.getTeam(), reward));
            }
        }
    }
}
