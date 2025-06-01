package com.knemis.skyblock.skyblockcoreproject.jobs;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager; // For job payments
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable; // For checking if crops are fully grown
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class JobManager {

    private final SkyBlockProject plugin;
    private FileConfiguration jobsConfig;
    private File jobsFile;

    private final Map<String, Job> jobs = new HashMap<>();
    private final Map<UUID, PlayerJobData> playerJobDataMap = new HashMap<>(); // In-memory storage

    public static class JobAction {
        public final Material material;
        public final String itemTypeKey;
        public final boolean requiresFullyGrown;
        public final double xp;
        public final double money;

        public JobAction(Material material, String itemTypeKey, boolean requiresFullyGrown, double xp, double money) {
            this.material = material;
            this.itemTypeKey = itemTypeKey;
            this.requiresFullyGrown = requiresFullyGrown;
            this.xp = xp;
            this.money = money;
        }
    }

    public static class JobRewardType {
        public final Map<String, JobAction> actions = new HashMap<>();
    }

    public static class Job {
        public final String internalName;
        public final Component displayName; // Changed to Component
        public final Component description; // Changed to Component
        public final Material guiMaterial;
        public final int maxLevel;
        public final double xpPerLevelBase;
        public final double xpPerLevelMultiplier;
        public final Map<String, JobRewardType> rewardableActions = new HashMap<>();

        public Job(String internalName, Component displayName, Component description, Material guiMaterial,
                   int maxLevel, double xpPerLevelBase, double xpPerLevelMultiplier) {
            this.internalName = internalName;
            this.displayName = displayName;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.maxLevel = maxLevel;
            this.xpPerLevelBase = xpPerLevelBase;
            this.xpPerLevelMultiplier = xpPerLevelMultiplier;
        }
    }

    public static class PlayerJobData {
        public String jobName;
        public int level;
        public double currentXP;

        public PlayerJobData(String jobName, int level, double currentXP) {
            this.jobName = jobName;
            this.level = level;
            this.currentXP = currentXP;
        }

        public boolean hasJob() {
            return jobName != null && !jobName.isEmpty();
        }
    }

    public JobManager(SkyBlockProject plugin) {
        this.plugin = plugin;
        loadJobsConfiguration();
    }

    public void loadJobsConfiguration() {
        jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            plugin.saveResource("jobs.yml", false);
            plugin.getLogger().info("jobs.yml created successfully.");
        }

        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
        InputStream defaultConfigStream = plugin.getResource("jobs.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            jobsConfig.setDefaults(defaultConfig);
        }

        jobs.clear();
        ConfigurationSection jobsSection = jobsConfig.getConfigurationSection("jobs");
        if (jobsSection == null) {
            plugin.getLogger().warning("No 'jobs' section found in jobs.yml. No jobs will be loaded.");
            return;
        }

        for (String jobKey : jobsSection.getKeys(false)) {
            ConfigurationSection jobConfig = jobsSection.getConfigurationSection(jobKey);
            if (jobConfig == null) {
                plugin.getLogger().warning("Job configuration for '" + jobKey + "' is invalid. Skipping.");
                continue;
            }

            try {
                Component displayName = ChatUtils.deserializeLegacyColorCodes(jobConfig.getString("display_name", jobKey));
                Component description = ChatUtils.deserializeLegacyColorCodes(jobConfig.getString("description", "A job."));
                Material guiMaterial = Material.matchMaterial(jobConfig.getString("gui_material", "STONE"));
                if (guiMaterial == null) {
                    plugin.getLogger().warning("Invalid gui_material for job '" + jobKey + "'. Defaulting to STONE.");
                    guiMaterial = Material.STONE;
                }
                int maxLevel = jobConfig.getInt("max_level", 50);
                double xpPerLevelBase = jobConfig.getDouble("xp_per_level_base", 100.0);
                double xpPerLevelMultiplier = jobConfig.getDouble("xp_per_level_multiplier", 1.2);

                Job job = new Job(jobKey.toUpperCase(), displayName, description, guiMaterial, maxLevel, xpPerLevelBase, xpPerLevelMultiplier);

                ConfigurationSection rewardsSection = jobConfig.getConfigurationSection("rewards");
                if (rewardsSection != null) {
                    for (String actionTypeKey : rewardsSection.getKeys(false)) {
                        JobRewardType rewardType = new JobRewardType();
                        ConfigurationSection actionTypeConfig = rewardsSection.getConfigurationSection(actionTypeKey);
                        if (actionTypeConfig != null) {
                            for (String specificActionKey : actionTypeConfig.getKeys(false)) {
                                ConfigurationSection actionDetailConfig = actionTypeConfig.getConfigurationSection(specificActionKey);
                                if (actionDetailConfig != null) {
                                    double xp = actionDetailConfig.getDouble("xp", 0.0);
                                    double money = actionDetailConfig.getDouble("money", 0.0);
                                    boolean requiresFullyGrown = actionDetailConfig.getBoolean("requires_fully_grown", false);
                                    Material material = null;
                                    String itemType = null;
                                    if (actionTypeKey.toUpperCase().contains("BLOCK")) {
                                        material = Material.matchMaterial(specificActionKey.toUpperCase());
                                        if (material == null) {
                                            plugin.getLogger().warning("Invalid material '" + specificActionKey + "' in job '" + jobKey + "', action type '" + actionTypeKey + "'. Skipping this action reward.");
                                            continue;
                                        }
                                    } else {
                                        itemType = specificActionKey.toUpperCase();
                                    }
                                    JobAction jobAction = new JobAction(material, itemType, requiresFullyGrown, xp, money);
                                    rewardType.actions.put(specificActionKey.toUpperCase(), jobAction);
                                }
                            }
                        }
                        job.rewardableActions.put(actionTypeKey.toUpperCase(), rewardType);
                    }
                }
                jobs.put(job.internalName, job);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load job '" + jobKey + "'. Error: " + e.getMessage(), e);
            }
        }
        plugin.getLogger().info("Loaded " + jobs.size() + " jobs.");
    }

    public PlayerJobData getPlayerData(UUID playerId) {
        return playerJobDataMap.computeIfAbsent(playerId, uuid -> new PlayerJobData(null, 1, 0.0));
    }

    public void loadPlayerJobDataOnJoin(Player player) {
        getPlayerData(player.getUniqueId());
        plugin.getLogger().fine("Ensured PlayerJobData for " + player.getName() + " on join.");
    }

    public void savePlayerJobDataOnQuit(Player player) {
        plugin.getLogger().fine("PlayerJobData for " + player.getName() + " would be saved here if persistence was implemented.");
    }

    public void clearAllPlayerJobData() {
        playerJobDataMap.clear();
        plugin.getLogger().info("All in-memory player job data cleared.");
    }

    public void setPlayerJob(Player player, String jobName) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        Job newJob = (jobName != null) ? getJob(jobName) : null;

        if (newJob == null && jobName != null) {
            player.sendMessage(Component.text("The job '" + jobName + "' does not exist.", NamedTextColor.RED));
            return;
        }

        if (newJob != null) {
            data.jobName = newJob.internalName;
            data.level = 1;
            data.currentXP = 0.0;
            player.sendMessage(Component.text("You have started the ", NamedTextColor.GREEN)
                    .append(newJob.displayName) // displayName is already a Component
                    .append(Component.text(" job!", NamedTextColor.GREEN)));
        } else {
            String oldJobNameString = data.jobName;
            data.jobName = null;
            data.level = 1;
            data.currentXP = 0.0;
            if (oldJobNameString != null) {
                 Job oldJob = getJob(oldJobNameString);
                 Component oldJobDisplayName = (oldJob != null) ? oldJob.displayName : Component.text(oldJobNameString);
                 player.sendMessage(Component.text("You have left the ", NamedTextColor.YELLOW)
                         .append(oldJobDisplayName)
                         .append(Component.text(" job.", NamedTextColor.YELLOW)));
            } else {
                 player.sendMessage(Component.text("You are not currently in a job.", NamedTextColor.YELLOW));
            }
        }
    }

    public Job getJob(String jobName) {
        if (jobName == null) return null;
        return jobs.get(jobName.toUpperCase());
    }

    public double calculateXpForLevel(Job job, int targetLevel) {
        if (targetLevel <= 1) return 0;
        return job.xpPerLevelBase * Math.pow(job.xpPerLevelMultiplier, Math.max(0, targetLevel - 2));
    }

    public void addXP(Player player, double xpAmount) {
        if (xpAmount <= 0) return;
        PlayerJobData data = getPlayerData(player.getUniqueId());
        if (!data.hasJob()) return;

        Job currentJob = getJob(data.jobName);
        if (currentJob == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has job '" + data.jobName + "' but job does not exist in config.");
            data.jobName = null;
            return;
        }

        if (data.level >= currentJob.maxLevel) {
            double xpForMaxLevel = calculateXpForLevel(currentJob, currentJob.maxLevel);
            if(data.currentXP < xpForMaxLevel) {
                 data.currentXP = Math.min(data.currentXP + xpAmount, xpForMaxLevel);
            }
            return;
        }

        data.currentXP += xpAmount;
        // player.sendActionBar(Component.text("+" + String.format("%.1f", xpAmount) + " XP", NamedTextColor.GOLD));

        double xpNeededForNextLevel = calculateXpForLevel(currentJob, data.level + 1);
        while (data.currentXP >= xpNeededForNextLevel && data.level < currentJob.maxLevel) {
            data.level++;
            data.currentXP -= xpNeededForNextLevel;
            if (data.currentXP < 0) data.currentXP = 0;

            player.sendMessage(Component.text("Congratulations! Your ", NamedTextColor.AQUA)
                    .append(currentJob.displayName)
                    .append(Component.text(" job is now level ", NamedTextColor.AQUA))
                    .append(Component.text(String.valueOf(data.level), NamedTextColor.GOLD))
                    .append(Component.text("!", NamedTextColor.AQUA)));

            if (data.level >= currentJob.maxLevel) {
                player.sendMessage(Component.text("You have reached the maximum level for the ", NamedTextColor.GOLD)
                        .append(currentJob.displayName)
                        .append(Component.text(" job!", NamedTextColor.GOLD)));
                data.currentXP = calculateXpForLevel(currentJob, currentJob.maxLevel);
                break;
            }
            xpNeededForNextLevel = calculateXpForLevel(currentJob, data.level + 1);
        }
    }

    public void processJobAction(Player player, String actionTypeKey, @javax.annotation.Nullable Material materialContext, @javax.annotation.Nullable String itemTypeKeyContext, @javax.annotation.Nullable Block blockContext) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        if (!data.hasJob()) return;

        Job currentJob = getJob(data.jobName);
        if (currentJob == null) return;

        JobRewardType rewardType = currentJob.rewardableActions.get(actionTypeKey.toUpperCase());
        if (rewardType == null) return;

        String specificKey = (materialContext != null) ? materialContext.name() : itemTypeKeyContext;
        if (specificKey == null) return;

        JobAction action = rewardType.actions.get(specificKey.toUpperCase());
        if (action == null) return;

        if (action.requiresFullyGrown && blockContext != null) {
            if (blockContext.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) blockContext.getBlockData();
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    return;
                }
            }
        }

        if (action.money > 0 && EconomyManager.isEconomyAvailable()) {
            if (EconomyManager.deposit(player, action.money)) {
                // player.sendMessage(Component.text("+ " + EconomyManager.format(action.money), NamedTextColor.GREEN)); // Example message
            }
        }

        if (action.xp > 0) {
            addXP(player, action.xp);
        }
    }

    public Collection<Job> getAvailableJobs() {
        return jobs.values();
    }

    public Map<String, Job> getAllJobs() {
        return jobs;
    }
}
