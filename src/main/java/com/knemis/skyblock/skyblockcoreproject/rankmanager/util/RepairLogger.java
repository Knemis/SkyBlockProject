package com.knemis.skyblock.skyblockcoreproject.rankmanager.util;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import org.bukkit.ChatColor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class RepairLogger {

    private final SkyBlockProject plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RepairLogger(SkyBlockProject plugin) {
        this.plugin = plugin;
        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        this.logFile = new File(logsFolder, "rank_manager_repair_log.yml");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "rank_manager_repair_log.yml oluşturulamadı!", e);
        }
    }

    private void writeToFile(String formattedMessage) {
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(formattedMessage);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "rank_manager_repair_log.yml dosyasına yazılamadı!", e);
        }
    }

    public void log(String level, String message) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        writeToFile(logEntry);

        String consoleMessage = SkyBlockProject.PLUGIN_PREFIX + message;
        if ("WARN".equals(level.toUpperCase())) {
            plugin.getLogger().warning(ChatColor.stripColor(consoleMessage));
        } else if ("SEVERE".equals(level.toUpperCase())) {
            plugin.getLogger().severe(ChatColor.stripColor(consoleMessage));
        } else if ("REPAIR".equals(level.toUpperCase())){
            plugin.getLogger().info(SkyBlockProject.PLUGIN_PREFIX + ChatColor.YELLOW + "[REPAIR] " + ChatColor.stripColor(message));
        }
        else {
            plugin.getLogger().info(ChatColor.stripColor(consoleMessage));
        }
    }

    public void logInfo(String message) {
        log("INFO", message);
    }

    public void logWarning(String message) {
        log("WARN", message);
    }

    public void logSevere(String message) {
        log("SEVERE", message);
    }

    public void logRepair(String groupName, String attribute, String oldValue, String newValue) {
        String message = String.format("Düzeltilen grup '%s': '%s' değiştirildi, eski: '%s', yeni: '%s'",
                groupName, attribute, oldValue == null || oldValue.isEmpty() ? "N/A" : oldValue, newValue == null || newValue.isEmpty() ? "N/A" : newValue);
        log("REPAIR", message);
    }
}
