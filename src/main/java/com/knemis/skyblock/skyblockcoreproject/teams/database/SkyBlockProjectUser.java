package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement; // TODO: Update to actual Enhancement class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType; // TODO: Update to actual EnhancementAffectsType class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData; // TODO: Update to actual EnhancementData class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData; // TODO: Update to actual FlightEnhancementData class
// import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData; // TODO: Update to actual PotionEnhancementData class
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@DatabaseTable(tableName = "users")
public class SkyBlockProjectUser<T extends com.knemis.skyblock.skyblockcoreproject.teams.database.Team> extends DatabaseObject { // TODO: Update Team to actual class

    @DatabaseField(columnName = "uuid", canBeNull = false, id = true)
    private @NotNull UUID uuid;

    @DatabaseField(columnName = "name", canBeNull = false)
    private @NotNull String name;

    @DatabaseField(columnName = "team_id")
    private int teamID;
    @DatabaseField(columnName = "user_rank", canBeNull = false)
    private int userRank;

    @DatabaseField(columnName = "join_time")
    private LocalDateTime joinTime;
    @DatabaseField(columnName = "active_profile", canBeNull = false)
    private @NotNull UUID activeProfile = UUID.randomUUID();

    private boolean bypassing;
    private boolean flying;

    private String chatType = "";

    private BukkitTask bukkitTask;

    private int bukkitTaskTicks = 0;

    public void setTeam(T t) {
        this.teamID = t == null ? 0 : t.getId();
        setJoinTime(LocalDateTime.now());
        userRank = 1;
    }

    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(uuid);
    }

    public boolean canFly(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        Player player = getPlayer();

        if (isBypassing()) return true; // bypass should be checked first, since this is an admin permission
        // if (player.hasPermission(SkyBlockProjectTeams.getCommands().flyCommand.getFlyAnywherePermission())) return true; // TODO: Uncomment when Commands are refactored

        // Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(getTeamID()); // TODO: Uncomment when TeamManager is refactored
        // Optional<T> visitor = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player); // TODO: Uncomment when TeamManager is refactored
        // if (player.hasPermission(SkyBlockProjectTeams.getCommands().flyCommand.permission) && team.isPresent() && team.map(T::getId).orElse(-1).equals(visitor.map(T::getId).orElse(-1))) { // TODO: Uncomment when Commands and TeamManager are refactored
            // return true;
        // }
        // return canFly(team.orElse(null), SkyBlockProjectTeams) || canFly(visitor.orElse(null), SkyBlockProjectTeams); // TODO: Uncomment when team and visitor are available
        return false; // Placeholder
    }

    private boolean canFly(T team, SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        if (team == null) return false;
        // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData> flightEnhancement = SkyBlockProjectTeams.getEnhancements().flightEnhancement; // TODO: Uncomment when Enhancements are refactored
        // TeamEnhancement teamEnhancement = SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, "flight"); // TODO: Uncomment when TeamManager and TeamEnhancement are refactored
        // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.FlightEnhancementData data = flightEnhancement.levels.get(teamEnhancement.getLevel()); // TODO: Uncomment when flightEnhancement and teamEnhancement are available

        // if (!teamEnhancement.isActive(flightEnhancement.type)) return false; // TODO: Uncomment when flightEnhancement and teamEnhancement are available
        // if (data == null) return false;

        // return canApply(SkyBlockProjectTeams, team, data.enhancementAffectsType); // TODO: Uncomment when data is available
        return false; // Placeholder
    }

    public void initBukkitTask(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        if (bukkitTask != null) return;
        bukkitTask = Bukkit.getScheduler().runTaskTimer(SkyBlockProjectTeams, () -> bukkitTask(SkyBlockProjectTeams), 0, 20);
    }

    public void bukkitTask(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        bukkitTaskTicks++;
        applyPotionEffects(SkyBlockProjectTeams);
    }

    public void applyPotionEffects(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        Player player = getPlayer();
        if (player == null) return;
        // SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(player.getLocation()).ifPresent(t -> applyPotionEffects(SkyBlockProjectTeams, t)); // TODO: Uncomment when TeamManager is refactored
        // SkyBlockProjectTeams.getTeamManager().getTeamViaID(teamID).ifPresent(t -> applyPotionEffects(SkyBlockProjectTeams, t)); // TODO: Uncomment when TeamManager is refactored
    }

    public void applyPotionEffects(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams, T team) {
        int duration = 10;
        Player player = getPlayer();
        if (player == null) return;
        HashMap<PotionEffectType, Integer> potionEffects = new HashMap<>();

        // for (Map.Entry<String, com.knemis.skyblock.skyblockcoreproject.teams.enhancements.Enhancement<?>> enhancement : SkyBlockProjectTeams.getEnhancementList().entrySet()) { // TODO: Uncomment when getEnhancementList is available
            // TeamEnhancement teamEnhancement = SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, enhancement.getKey()); // TODO: Uncomment when TeamManager and TeamEnhancement are refactored
            // if (!teamEnhancement.isActive(enhancement.getValue().type)) continue;
            // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementData enhancementData = enhancement.getValue().levels.get(teamEnhancement.getLevel()); // TODO: Uncomment when EnhancementData is refactored
            // if (enhancementData instanceof com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData) { // TODO: Uncomment when PotionEnhancementData is refactored
                // com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData potionEnhancementData = (com.knemis.skyblock.skyblockcoreproject.teams.enhancements.PotionEnhancementData) enhancementData;
                // if (!canApply(SkyBlockProjectTeams, team, potionEnhancementData.enhancementAffectsType)) continue;
                // PotionEffectType potionEffectType = potionEnhancementData.potion.getPotionEffectType();
                // if (!potionEffects.containsKey(potionEffectType)) {
                    // potionEffects.put(potionEffectType, potionEnhancementData.strength - 1);
                // } else if (potionEffects.get(potionEffectType) < potionEnhancementData.strength - 1) {
                    // potionEffects.put(potionEffectType, potionEnhancementData.strength - 1);
                // }
            // }
        // }

        for (Map.Entry<PotionEffectType, Integer> potionEffectType : potionEffects.entrySet()) {
            Optional<PotionEffect> potionEffect = player.getActivePotionEffects().stream()
                    .filter(effect -> effect.getType().equals(potionEffectType.getKey()))
                    .findFirst();
            if (potionEffect.isPresent()) {
                if (potionEffect.get().getAmplifier() <= potionEffectType.getValue() && potionEffect.get().getDuration() <= duration * 20) {
                    player.removePotionEffect(potionEffectType.getKey());
                }
            }
            player.addPotionEffect(potionEffectType.getKey().createEffect(duration * 20, potionEffectType.getValue()));
        }
    }

    public boolean canApply(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams, T team, List<com.knemis.skyblock.skyblockcoreproject.teams.enhancements.EnhancementAffectsType> enhancementAffectsTypes) {
        Player player = getPlayer();
        if (player == null) return false;

        // Temporarily return true until the commented code is ready
        return true;

        /* When you uncomment the TODO sections, remove the "return true" above and uncomment this:
        for (EnhancementAffectsType enhancementAffectsType : enhancementAffectsTypes) {
            if (enhancementAffectsType == EnhancementAffectsType.MEMBERS_IN_TERRITORY &&
                team.getId() == teamID && team.getId() == teamLocationID) {
                return true;
            }
        }
        return false;
        */
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
        setChanged(true);
    }

    public void setName(@NotNull String name) {
        this.name = name;
        setChanged(true);
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
        setChanged(true);
    }

    public void setUserRank(int userRank) {
        this.userRank = userRank;
        setChanged(true);
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
        setChanged(true);
    }

    public void setBypassing(boolean bypassing) {
        this.bypassing = bypassing;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }
}