package com.knemis.skyblock.skyblockcoreproject.teams.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.enhancements.*;
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
public class SkyBlockProjectTeamsUser<T extends Team> extends DatabaseObject {

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
        if (player.hasPermission(SkyBlockProjectTeams.getCommands().flyCommand.getFlyAnywherePermission())) return true;

        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(getTeamID());
        Optional<T> visitor = SkyBlockProjectTeams.getTeamManager().getTeamViaPlayerLocation(player);
        if (player.hasPermission(SkyBlockProjectTeams.getCommands().flyCommand.permission) && team.isPresent() && team.map(T::getId).orElse(-1).equals(visitor.map(T::getId).orElse(-1))) {
            return true;
        }
        return canFly(team.orElse(null), SkyBlockProjectTeams) || canFly(visitor.orElse(null), SkyBlockProjectTeams);
    }

    private boolean canFly(T team, SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams) {
        if (team == null) return false;
        Enhancement<FlightEnhancementData> flightEnhancement = SkyBlockProjectTeams.getEnhancements().flightEnhancement;
        TeamEnhancement teamEnhancement = SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, "flight");
        FlightEnhancementData data = flightEnhancement.levels.get(teamEnhancement.getLevel());

        if (!teamEnhancement.isActive(flightEnhancement.type)) return false;
        if (data == null) return false;

        return canApply(SkyBlockProjectTeams, team, data.enhancementAffectsType);
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
        SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(player.getLocation()).ifPresent(t -> applyPotionEffects(SkyBlockProjectTeams, t));
        SkyBlockProjectTeams.getTeamManager().getTeamViaID(teamID).ifPresent(t -> applyPotionEffects(SkyBlockProjectTeams, t));
    }

    public void applyPotionEffects(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams, T team) {
        int duration = 10;
        Player player = getPlayer();
        if (player == null) return;
        HashMap<PotionEffectType, Integer> potionEffects = new HashMap<>();

        for (Map.Entry<String, Enhancement<?>> enhancement : SkyBlockProjectTeams.getEnhancementList().entrySet()) {
            TeamEnhancement teamEnhancement = SkyBlockProjectTeams.getTeamManager().getTeamEnhancement(team, enhancement.getKey());
            if (!teamEnhancement.isActive(enhancement.getValue().type)) continue;
            EnhancementData enhancementData = enhancement.getValue().levels.get(teamEnhancement.getLevel());
            if (enhancementData instanceof PotionEnhancementData) {
                PotionEnhancementData potionEnhancementData = (PotionEnhancementData) enhancementData;
                if (!canApply(SkyBlockProjectTeams, team, potionEnhancementData.enhancementAffectsType)) continue;
                PotionEffectType potionEffectType = potionEnhancementData.potion.getPotionEffectType();
                if (!potionEffects.containsKey(potionEffectType)) {
                    potionEffects.put(potionEffectType, potionEnhancementData.strength - 1);
                } else if (potionEffects.get(potionEffectType) < potionEnhancementData.strength - 1) {
                    potionEffects.put(potionEffectType, potionEnhancementData.strength - 1);
                }
            }
        }

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

    public boolean canApply(SkyBlockProjectTeams<T, ?> SkyBlockProjectTeams, T team, List<EnhancementAffectsType> enhancementAffectsTypes) {
        Player player = getPlayer();
        if (player == null) return false;
        int teamLocationID = SkyBlockProjectTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0);
        for (EnhancementAffectsType enhancementAffectsType : enhancementAffectsTypes) {
            if (enhancementAffectsType == EnhancementAffectsType.VISITORS && team.getId() == teamLocationID) {
                return true;
            }
            if (enhancementAffectsType == EnhancementAffectsType.MEMBERS_ANYWHERE && team.getId() == teamID) {
                return true;
            }
            if (enhancementAffectsType == EnhancementAffectsType.MEMBERS_IN_TERRITORY && team.getId() == teamID && team.getId() == teamLocationID) {
                return true;
            }
        }
        return false;
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
