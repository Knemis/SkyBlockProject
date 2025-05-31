package com.knemis.skyblock.skyblockcoreproject.rankmanager.luckperms;

import com.knemis.skyblock.skyblockcoreproject.SkyBlockProject;
import com.knemis.skyblock.skyblockcoreproject.rankmanager.config.RankConfigManager;
import com.knemis.skyblock.skyblockcoreproject.rankmanager.config.RankTemplate;
import com.knemis.skyblock.skyblockcoreproject.rankmanager.util.RepairLogger;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.node.types.WeightNode;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LuckPermsHelper {

    private final SkyBlockProject plugin;
    private final LuckPerms luckPermsApi;
    private final RankConfigManager rankConfigManager;
    private final RepairLogger repairLogger;

    public LuckPermsHelper(SkyBlockProject plugin, LuckPerms luckPermsApi, RankConfigManager rankConfigManager, RepairLogger repairLogger) {
        this.plugin = plugin;
        this.luckPermsApi = luckPermsApi;
        this.rankConfigManager = rankConfigManager;
        this.repairLogger = repairLogger;
    }

    public void initializeAndValidateRanks(Consumer<Boolean> callback) {
        AtomicBoolean anyRepairsMadeOverall = new AtomicBoolean(false);
        Map<String, RankTemplate> templates = rankConfigManager.getAllRankTemplates();

        if (templates.isEmpty()) {
            plugin.getLogger().warning("Yüklü rütbe şablonu yok. Rütbe başlatma ve doğrulama atlanıyor.");
            if (callback != null) callback.accept(false);
            return;
        }

        List<CompletableFuture<Void>> groupFutures = new ArrayList<>();

        for (RankTemplate template : templates.values()) {
            CompletableFuture<Void> groupFuture = luckPermsApi.getGroupManager().loadGroup(template.getInternalName())
                    .thenComposeAsync(optionalGroup -> {
                        Group group;
                        boolean isNewGroup = optionalGroup.isEmpty();
                        if (isNewGroup) {
                            plugin.getLogger().info("Grup " + template.getInternalName() + " mevcut değil. Oluşturuluyor...");
                            repairLogger.logInfo("Yeni grup oluşturuluyor: " + template.getInternalName());
                            anyRepairsMadeOverall.set(true);
                            return luckPermsApi.getGroupManager().createAndLoadGroup(template.getInternalName())
                                    .thenComposeAsync(newGroup -> {
                                        if (newGroup == null) {
                                            plugin.getLogger().severe("Grup oluşturulamadı: " + template.getInternalName());
                                            return CompletableFuture.completedFuture(null);
                                        }
                                        applyTemplateToGroup(newGroup, template, anyRepairsMadeOverall, true);
                                        return luckPermsApi.getGroupManager().saveGroup(newGroup);
                                    });
                        } else {
                            group = optionalGroup.get();
                            plugin.getLogger().info("Mevcut grup doğrulanıyor: " + group.getName());
                            applyTemplateToGroup(group, template, anyRepairsMadeOverall, false);
                            return luckPermsApi.getGroupManager().saveGroup(group);
                        }
                    }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
            groupFutures.add(groupFuture);
        }

        CompletableFuture.allOf(groupFutures.toArray(new CompletableFuture[0]))
                .whenCompleteAsync((unused, throwable) -> {
                    if (throwable != null) {
                        plugin.getLogger().log(Level.SEVERE, "Rütbe başlatma/doğrulama sırasında bir hata oluştu: " + throwable.getMessage(), throwable);
                        repairLogger.logSevere("Rütbe işleme sırasında istisna: " + throwable.getMessage());
                    }
                    if (callback != null) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(anyRepairsMadeOverall.get()));
                    }
                }, runnable -> plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    private void applyTemplateToGroup(Group group, RankTemplate template, AtomicBoolean overallRepairsMade, boolean isNewGroup) {
        AtomicBoolean currentGroupRepairsMade = new AtomicBoolean(isNewGroup);

        int currentWeight = group.getWeight().orElse(0);
        if (currentWeight != template.getWeight()) {
            clearNodesOfType(group, NodeType.WEIGHT);
            group.data().add(WeightNode.builder(template.getWeight()).build());
            if (!isNewGroup) repairLogger.logRepair(group.getName(), "Weight", String.valueOf(currentWeight), String.valueOf(template.getWeight()));
            currentGroupRepairsMade.set(true);
        } else if (isNewGroup) {
            group.data().add(WeightNode.builder(template.getWeight()).build());
        }

        String templatePrefix = ChatColor.translateAlternateColorCodes('&', template.getPrefix() != null ? template.getPrefix() : "");
        String currentPrefix = group.getCachedData().getMetaData().getPrefix();
        if (currentPrefix == null) currentPrefix = "";

        if (!templatePrefix.equals(currentPrefix)) {
            clearNodesOfType(group, NodeType.PREFIX);
            if (!templatePrefix.isEmpty()) {
                group.data().add(PrefixNode.builder(templatePrefix, template.getWeight()).build());
            }
            if (!isNewGroup) repairLogger.logRepair(group.getName(), "Prefix", currentPrefix, templatePrefix);
            currentGroupRepairsMade.set(true);
        } else if (isNewGroup && !templatePrefix.isEmpty()) {
            group.data().add(PrefixNode.builder(templatePrefix, template.getWeight()).build());
        }

        String templateSuffix = ChatColor.translateAlternateColorCodes('&', template.getSuffix() != null ? template.getSuffix() : "");
        String currentSuffix = group.getCachedData().getMetaData().getSuffix();
        if (currentSuffix == null) currentSuffix = "";

        if (!templateSuffix.equals(currentSuffix)) {
            clearNodesOfType(group, NodeType.SUFFIX);
            if (!templateSuffix.isEmpty()) {
                group.data().add(SuffixNode.builder(templateSuffix, template.getWeight()).build());
            }
            if (!isNewGroup) repairLogger.logRepair(group.getName(), "Suffix", currentSuffix, templateSuffix);
            currentGroupRepairsMade.set(true);
        } else if (isNewGroup && !templateSuffix.isEmpty()) {
            group.data().add(SuffixNode.builder(templateSuffix, template.getWeight()).build());
        }

        Set<String> templatePermissions = new HashSet<>(template.getPermissions());
        Set<String> currentPermissions = group.getNodes(NodeType.PERMISSION).stream()
                .map(node -> ((PermissionNode) node).getPermission())
                .collect(Collectors.toSet());

        for (String perm : templatePermissions) {
            if (!currentPermissions.contains(perm)) {
                group.data().add(PermissionNode.builder(perm).build());
                if (!isNewGroup) repairLogger.logRepair(group.getName(), "Permission Added", "", perm);
                currentGroupRepairsMade.set(true);
            }
        }

        for (String perm : currentPermissions) {
            if (!templatePermissions.contains(perm)) {
                group.data().remove(PermissionNode.builder(perm).build());
                if (!isNewGroup) repairLogger.logRepair(group.getName(), "Permission Removed", perm, "");
                currentGroupRepairsMade.set(true);
            }
        }

        Set<String> templateInheritance = template.getInheritance().stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> currentInheritance = group.getNodes(NodeType.INHERITANCE).stream()
                .map(node -> ((InheritanceNode) node).getGroupName().toLowerCase())
                .collect(Collectors.toSet());

        for (String groupToInherit : templateInheritance) {
            if (!currentInheritance.contains(groupToInherit)) {
                group.data().add(InheritanceNode.builder(groupToInherit).build());
                if (!isNewGroup) repairLogger.logRepair(group.getName(), "Inheritance Added", "", groupToInherit);
                currentGroupRepairsMade.set(true);
            }
        }

        for (String inheritedGroup : currentInheritance) {
            if (!templateInheritance.contains(inheritedGroup)) {
                group.data().remove(InheritanceNode.builder(inheritedGroup).build());
                if (!isNewGroup) repairLogger.logRepair(group.getName(), "Inheritance Removed", inheritedGroup, "");
                currentGroupRepairsMade.set(true);
            }
        }

        if (currentGroupRepairsMade.get()) {
            overallRepairsMade.set(true);
            if (!isNewGroup) plugin.getLogger().info("Grup için onarımlar yapıldı: " + group.getName());
        }
    }

    private void clearNodesOfType(Group group, NodeType<? extends Node> type) {
        Collection<? extends Node> nodesToClear = group.getNodes(type);
        for (Node node : nodesToClear) {
            group.data().remove(node);
        }
    }
}
