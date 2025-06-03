package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.support.*;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashSet;

public class SupportManager<T extends Team, U extends keviinUser<T>> {

    private final keviinTeams<T, U> keviinTeams;

    public SupportManager(keviinTeams<T, U> keviinTeams) {
        this.keviinTeams = keviinTeams;
    }

    @Getter
    private HashSet<StackerSupport<T>> stackerSupport = new HashSet<>();
    @Getter
    private HashSet<SpawnerSupport<T>> spawnerSupport = new HashSet<>();
    @Getter
    private HashSet<SpawnSupport<T>> spawnSupport = new HashSet<>();
    @Getter
    private HashSet<String> providerList = new HashSet<>();

    public boolean supportedPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    private void registerBlockStackerSupport() {
        if (supportedPluginEnabled("RoseStacker"))
            stackerSupport.add(new RoseStackerSupport<>(keviinTeams));

        if (supportedPluginEnabled("WildStacker"))
            stackerSupport.add(new WildStackerSupport<>(keviinTeams));

        if(supportedPluginEnabled("ObsidianStacker"))
            stackerSupport.add(new ObsidianStackerSupport<>(keviinTeams));
    }

    private void registerSpawnerSupport() {
        if (supportedPluginEnabled("RoseStacker"))
            spawnerSupport.add(new RoseStackerSupport<>(keviinTeams));

        if (supportedPluginEnabled("WildStacker"))
            spawnerSupport.add(new WildStackerSupport<>(keviinTeams));
    }

    private void registerSpawnSupport() {
        if (supportedPluginEnabled("EssentialsSpawn"))
            spawnSupport.add(new EssentialsSpawnSupport<>(keviinTeams));
    }

    public void registerSupport() {
        registerBlockStackerSupport();
        registerSpawnerSupport();
        registerSpawnSupport();

        stackerSupport.forEach(provider -> providerList.add(provider.supportProvider()));
        spawnerSupport.forEach(provider -> providerList.add(provider.supportProvider()));
        spawnSupport.forEach(provider -> providerList.add(provider.supportProvider()));
    }
}