package com.knemis.skyblock.skyblockcoreproject.rankmanager.config;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RankConfigManager {

    private final SkyBlockProject plugin;
    private final File configFile;
    private final Map<String, RankTemplate> rankTemplates = new HashMap<>();

    public RankConfigManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "rank_templates.yml");
    }

    public boolean loadRankTemplates() {
        rankTemplates.clear();
        if (!configFile.exists()) {
            plugin.getLogger().info("rank_templates.yml bulunamadı, varsayılan oluşturuluyor...");
            plugin.saveResource("rank_templates.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (config.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("rank_templates.yml boş veya okunamaz durumda. Dahili varsayılan yüklenmeye çalışılıyor.");
            InputStream defaultConfigStream = plugin.getResource("rank_templates.yml");
            if (defaultConfigStream != null) {
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
            } else {
                plugin.getLogger().severe("Dahili varsayılan rank_templates.yml bulunamadı! Rütbe şablonları yüklenemiyor.");
                return false;
            }
        }

        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        if (ranksSection == null) {
            plugin.getLogger().severe("'ranks' bölümü rank_templates.yml dosyasında bulunamadı!");
            return false;
        }

        for (String rankKey : ranksSection.getKeys(false)) {
            ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankKey);
            if (rankSection == null) {
                plugin.getLogger().warning("Rütbe için geçersiz yapılandırma: " + rankKey + ". Atlanıyor.");
                continue;
            }

            String internalName = rankKey.toLowerCase();
            String displayName = rankSection.getString("display_name", internalName);
            String prefix = rankSection.getString("prefix", "");
            String suffix = rankSection.getString("suffix", "");
            int weight = rankSection.getInt("weight", 0);
            List<String> permissions = rankSection.getStringList("permissions");
            List<String> inheritance = rankSection.getStringList("inheritance");

            RankTemplate template = new RankTemplate(internalName, displayName, prefix, suffix, weight, permissions, inheritance);
            rankTemplates.put(internalName, template);
            plugin.getLogger().log(Level.INFO, "Rütbe için şablon yüklendi: " + internalName);
        }
        return !rankTemplates.isEmpty();
    }

    public RankTemplate getRankTemplate(String name) {
        return rankTemplates.get(name.toLowerCase());
    }

    public Map<String, RankTemplate> getAllRankTemplates() {
        return new HashMap<>(rankTemplates);
    }
}
