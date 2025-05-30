package com.knemis.skyblock.skyblockcoreproject.jobs;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.shop.EconomyManager; // For job payments
import com.knemis.skyblock.skyblockcoreproject.utils.ChatUtils;
import org.bukkit.ChatColor;
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

    // --- Inner Classes for Job Structure ---

    /**
     * Represents a specific action within a job that yields rewards.
     * e.g., breaking a STONE block, catching a COD fish.
     */
    public static class JobAction {
        public final Material material; // Relevant for block-based actions
        public final String itemTypeKey; // Relevant for item-based actions (e.g., fishing, crafting)
        public final boolean requiresFullyGrown; // For farmable blocks
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

    /**
     * Defines a type of rewardable action within a job, like breaking blocks or fishing.
     * Contains a map of specific materials/items to their corresponding JobAction details.
     */
    public static class JobRewardType {
        // Key: Material.name() for block actions, or a custom itemTypeKey for others (e.g., "COD", "SALMON")
        public final Map<String, JobAction> actions = new HashMap<>();
    }

    /**
     * Represents a job that players can choose.
     * Contains details about the job, its progression, and rewardable actions.
     */
    public static class Job {
        public final String internalName;
        public final String displayName;
        public final String description;
        public final Material guiMaterial;
        public final int maxLevel;
        public final double xpPerLevelBase;
        public final double xpPerLevelMultiplier;
        // Key: Action type string (e.g., "BLOCK_BREAK", "FISH_CAUGHT", "MOB_KILL")
        public final Map<String, JobRewardType> rewardableActions = new HashMap<>();

        public Job(String internalName, String displayName, String description, Material guiMaterial,
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

    /**
     * Stores a player's progress in their current job.
     */
    public static class PlayerJobData {
        public String jobName; // Null if no job
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

    // --- Constructor and Configuration Loading ---

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

        jobs.clear(); // Clear existing jobs before reloading
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
                String displayName = ChatUtils.translateAlternateColorCodes(jobConfig.getString("display_name", jobKey));
                String description = ChatUtils.translateAlternateColorCodes(jobConfig.getString("description", "A job."));
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
                    for (String actionTypeKey : rewardsSection.getKeys(false)) { // e.g., BLOCK_BREAK
                        JobRewardType rewardType = new JobRewardType();
                        ConfigurationSection actionTypeConfig = rewardsSection.getConfigurationSection(actionTypeKey);
                        if (actionTypeConfig != null) {
                            for (String specificActionKey : actionTypeConfig.getKeys(false)) { // e.g., STONE or "COD"
                                ConfigurationSection actionDetailConfig = actionTypeConfig.getConfigurationSection(specificActionKey);
                                if (actionDetailConfig != null) {
                                    double xp = actionDetailConfig.getDouble("xp", 0.0);
                                    double money = actionDetailConfig.getDouble("money", 0.0);
                                    boolean requiresFullyGrown = actionDetailConfig.getBoolean("requires_fully_grown", false);

                                    Material material = null;
                                    String itemType = null;

                                    // Determine if specificActionKey is a Material or a custom item key
                                    if (actionTypeKey.toUpperCase().contains("BLOCK")) { // Heuristic for block-based actions
                                        material = Material.matchMaterial(specificActionKey.toUpperCase());
                                        if (material == null) {
                                            plugin.getLogger().warning("Invalid material '" + specificActionKey + "' in job '" + jobKey + "', action type '" + actionTypeKey + "'. Skipping this action reward.");
                                            continue;
                                        }
                                    } else {
                                        itemType = specificActionKey.toUpperCase(); // For FISH_CAUGHT, MOB_KILL, etc.
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

    // --- Player Data Management ---

    /**
     * Gets the PlayerJobData for a given player. Creates a new one if not found.
     * @param playerId The UUID of the player.
     * @return The PlayerJobData instance.
     */
    public PlayerJobData getPlayerData(UUID playerId) {
        return playerJobDataMap.computeIfAbsent(playerId, uuid -> new PlayerJobData(null, 1, 0.0));
    }

    /**
     * Loads player job data on join. For now, just ensures an entry exists.
     * In a persistent system, this would load from a file or database.
     * @param player The player who joined.
     */
    public void loadPlayerJobDataOnJoin(Player player) {
        // Ensures an entry exists. If persistence was implemented, this would load actual data.
        getPlayerData(player.getUniqueId());
        plugin.getLogger().fine("Ensured PlayerJobData for " + player.getName() + " on join.");
    }

    /**
     * Saves player job data on quit. For now, it's a stub.
     * In a persistent system, this would save to a file or database.
     * @param player The player who quit.
     */
    public void savePlayerJobDataOnQuit(Player player) {
        // Stub for saving data. With current in-memory, data is lost on shutdown/reload without further implementation.
        // If playerJobDataMap was very large, might consider removing on quit if not persisting.
        plugin.getLogger().fine("PlayerJobData for " + player.getName() + " would be saved here if persistence was implemented.");
    }

    /**
     * Clears all in-memory player job data. Useful for reloads if not persisting.
     */
    public void clearAllPlayerJobData() {
        playerJobDataMap.clear();
        plugin.getLogger().info("All in-memory player job data cleared.");
    }


    // --- Core Job Logic ---

    /**
     * Sets or changes a player's current job.
     * @param player The player.
     * @param jobName The internal name of the job to set. Null or invalid to leave job.
     */
    public void setPlayerJob(Player player, String jobName) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        Job newJob = (jobName != null) ? getJob(jobName) : null;

        if (newJob == null && jobName != null) {
            player.sendMessage(ChatColor.RED + "The job '" + jobName + "' does not exist.");
            return;
        }

        if (newJob != null) {
            data.jobName = newJob.internalName;
            data.level = 1;
            data.currentXP = 0.0;
            player.sendMessage(ChatColor.GREEN + "You have started the " + newJob.displayName + ChatColor.GREEN + " job!");
        } else {
            String oldJobName = data.jobName;
            data.jobName = null;
            data.level = 1;
            data.currentXP = 0.0;
            if (oldJobName != null) {
                 Job oldJob = getJob(oldJobName);
                 player.sendMessage(ChatColor.YELLOW + "You have left the " + (oldJob != null ? oldJob.displayName : oldJobName) + ChatColor.YELLOW + " job.");
            } else {
                 player.sendMessage(ChatColor.YELLOW + "You are not currently in a job.");
            }
        }
    }

    /**
     * Gets a Job by its internal name (case-insensitive).
     * @param jobName The internal name of the job.
     * @return The Job object, or null if not found.
     */
    public Job getJob(String jobName) {
        if (jobName == null) return null;
        return jobs.get(jobName.toUpperCase());
    }


    /**
     * Calculates the total XP needed to reach a target level from level 1.
     * Note: This is total XP from level 1, not XP from current level to next.
     * @param job The job.
     * @param targetLevel The target level.
     * @return The total XP required.
     */
    public double calculateXpForLevel(Job job, int targetLevel) {
        if (targetLevel <= 1) return 0;
        // XP for level 2 is base. XP for level 3 is base * mult, level 4 is base * mult^2, etc.
        // Sum of geometric series or iterative addition.
        // This formula calculates XP needed to get *to* targetLevel from targetLevel-1
        // Example: To reach level 2, you need 'base'. To reach level 3, you need 'base * mult' more XP *after* reaching level 2.
        return job.xpPerLevelBase * Math.pow(job.xpPerLevelMultiplier, Math.max(0, targetLevel - 2));
    }

    /**
     * Adds XP to a player's current job progress and handles level-ups.
     * @param player The player.
     * @param xpAmount The amount of XP to add.
     */
    public void addXP(Player player, double xpAmount) {
        if (xpAmount <= 0) return;
        PlayerJobData data = getPlayerData(player.getUniqueId());
        if (!data.hasJob()) return;

        Job currentJob = getJob(data.jobName);
        if (currentJob == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has job '" + data.jobName + "' but job does not exist in config.");
            data.jobName = null; // Clear invalid job
            return;
        }

        if (data.level >= currentJob.maxLevel) {
            // Player is at max level, can optionally still gain XP if currentXP < xpNeededForMaxLevel for display
            // Or just return if no further progression. For now, let's allow XP to fill up for the max level.
            double xpForMaxLevel = calculateXpForLevel(currentJob, currentJob.maxLevel);
            if(data.currentXP < xpForMaxLevel) {
                 data.currentXP = Math.min(data.currentXP + xpAmount, xpForMaxLevel);
                 // TODO: Send XP gain message (even if at max level but not full bar)
            }
            return;
        }

        data.currentXP += xpAmount;
        // TODO: Send XP gain message (e.g., action bar)
        // player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "+" + String.format("%.1f", xpAmount) + " XP"));


        double xpNeededForNextLevel = calculateXpForLevel(currentJob, data.level + 1);
        while (data.currentXP >= xpNeededForNextLevel && data.level < currentJob.maxLevel) {
            data.level++;
            data.currentXP -= xpNeededForNextLevel;
            if (data.currentXP < 0) data.currentXP = 0; // Safety check

            player.sendMessage(ChatColor.AQUA + "Congratulations! Your " + currentJob.displayName + ChatColor.AQUA + " job is now level " + ChatColor.GOLD + data.level + ChatColor.AQUA + "!");
            // TODO: Placeholder for job-specific level up rewards (e.g., commands, items)
            // Could call a method here: currentJob.executeLevelUpRewards(player, data.level);

            if (data.level >= currentJob.maxLevel) {
                player.sendMessage(ChatColor.GOLD + "You have reached the maximum level for the " + currentJob.displayName + ChatColor.GOLD + " job!");
                data.currentXP = calculateXpForLevel(currentJob, currentJob.maxLevel); // Fill XP bar for max level
                break;
            }
            xpNeededForNextLevel = calculateXpForLevel(currentJob, data.level + 1);
        }
         // Ensure currentXP doesn't exceed XP needed for the current level's full bar (unless max level)
        if (data.level < currentJob.maxLevel && data.currentXP > xpNeededForNextLevel) {
             // This case should ideally not be hit if logic is correct, but as a safeguard:
            // data.currentXP = xpNeededForNextLevel - 1; // or some portion
        }
    }

    /**
     * Processes a job-related action performed by a player.
     * Grants XP and money if the action is relevant to the player's current job.
     * @param player The player performing the action.
     * @param actionTypeKey The type of action (e.g., "BLOCK_BREAK", "FISH_CAUGHT").
     * @param materialContext The material involved (for block breaks, mob drops, etc.). Can be null.
     * @param itemTypeKeyContext A custom key for the item (for fishing, specific mob types). Can be null.
     * @param blockContext The block involved, for checks like `requiresFullyGrown`. Can be null.
     */
    public void processJobAction(Player player, String actionTypeKey, @javax.annotation.Nullable Material materialContext, @javax.annotation.Nullable String itemTypeKeyContext, @javax.annotation.Nullable Block blockContext) {
        PlayerJobData data = getPlayerData(player.getUniqueId());
        if (!data.hasJob()) return;

        Job currentJob = getJob(data.jobName);
        if (currentJob == null) return; // Should not happen if data.hasJob() is true and jobName is valid

        JobRewardType rewardType = currentJob.rewardableActions.get(actionTypeKey.toUpperCase());
        if (rewardType == null) return; // This job doesn't reward this type of action

        String specificKey = (materialContext != null) ? materialContext.name() : itemTypeKeyContext;
        if (specificKey == null) return; // No specific item/material to check for

        JobAction action = rewardType.actions.get(specificKey.toUpperCase());
        if (action == null) return; // This specific material/item is not rewarded for this action type in this job

        // Check for `requiresFullyGrown`
        if (action.requiresFullyGrown && blockContext != null) {
            if (blockContext.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) blockContext.getBlockData();
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    return; // Not fully grown, no rewards
                }
            } else {
                // If requiresFullyGrown is true but block is not Ageable, it's a config error for this block type.
                // Or, it implies this check isn't relevant for this specific non-ageable block.
                // For safety, if requiresFullyGrown is true, we expect an Ageable block.
                // plugin.getLogger().finer("Job action for " + specificKey + " requires_fully_grown but block is not Ageable.");
                // Depending on strictness, could return here.
            }
        }

        // Grant Money
        if (action.money > 0 && EconomyManager.isEconomyAvailable()) {
            if (EconomyManager.deposit(player, action.money)) {
                // TODO: Send money gain message, perhaps formatted with currency symbol
                // player.sendMessage(ChatColor.GREEN + "+ " + EconomyManager.format(action.money));
            }
        }

        // Grant XP
        if (action.xp > 0) {
            addXP(player, action.xp);
        }
    }

    // --- Utility/Helper Methods ---

    /**
     * Gets a collection of all available jobs.
     * @return A collection of Job objects.
     */
    public Collection<Job> getAvailableJobs() {
        return jobs.values();
    }

    /**
     * Gets the map of all loaded jobs.
     * @return A map where keys are internal job names and values are Job objects.
     */
    public Map<String, Job> getAllJobs() {
        return jobs; // Consider returning Collections.unmodifiableMap(jobs) if external modification is a concern
    }
}
