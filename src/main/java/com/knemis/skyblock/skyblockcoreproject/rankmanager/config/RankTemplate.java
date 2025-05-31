package com.knemis.skyblock.skyblockcoreproject.rankmanager.config;

import java.util.List;
import java.util.Objects;
import java.util.HashSet;

public class RankTemplate {
    private final String internalName;
    private final String displayName;
    private final String prefix;
    private final String suffix;
    private final int weight;
    private final List<String> permissions;
    private final List<String> inheritance;

    public RankTemplate(String internalName, String displayName, String prefix, String suffix, int weight, List<String> permissions, List<String> inheritance) {
        this.internalName = internalName.toLowerCase();
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.weight = weight;
        this.permissions = permissions != null ? List.copyOf(permissions) : List.of();
        this.inheritance = inheritance != null ? List.copyOf(inheritance) : List.of();
    }

    public String getInternalName() {
        return internalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getInheritance() {
        return inheritance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankTemplate that = (RankTemplate) o;
        return weight == that.weight &&
                Objects.equals(internalName, that.internalName) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(suffix, that.suffix) &&
                Objects.equals(new HashSet<>(permissions), new HashSet<>(that.permissions)) &&
                Objects.equals(new HashSet<>(inheritance), new HashSet<>(that.inheritance));
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalName, displayName, prefix, suffix, weight, new HashSet<>(permissions), new HashSet<>(inheritance));
    }
}
