package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.support.*;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashSet;

public class SupportManager<T extends Team, U extends SkyBlockProjectUser<T>> {

    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public SupportManager(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
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
            stackerSupport.add(new RoseStackerSupport<>(SkyBlockProjectTeams));

        if (supportedPluginEnabled("WildStacker"))
            stackerSupport.add(new WildStackerSupport<>(SkyBlockProjectTeams));

        if(supportedPluginEnabled("ObsidianStacker"))
            stackerSupport.add(new ObsidianStackerSupport<>(SkyBlockProjectTeams));
    }

    private void registerSpawnerSupport() {
        if (supportedPluginEnabled("RoseStacker"))
            spawnerSupport.add(new RoseStackerSupport<>(SkyBlockProjectTeams));

        if (supportedPluginEnabled("WildStacker"))
            spawnerSupport.add(new WildStackerSupport<>(SkyBlockProjectTeams));
    }

    private void registerSpawnSupport() {
        if (supportedPluginEnabled("EssentialsSpawn"))
            spawnSupport.add(new EssentialsSpawnSupport<>(SkyBlockProjectTeams));
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