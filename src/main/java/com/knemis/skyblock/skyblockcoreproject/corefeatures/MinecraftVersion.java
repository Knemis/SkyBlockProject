package com.knemis.skyblock.skyblockcoreproject.corefeatures;

import com.iridium.iridiumcore.multiversion.*;
import com.iridium.iridiumcore.nms.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

// Imports for version-specific NMS implementations
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_8_R2.corefeatures.nms.NMS_V1_8_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_8_R3.corefeatures.nms.NMS_V1_8_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_9_R1.corefeatures.nms.NMS_V1_9_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_9_R2.corefeatures.nms.NMS_V1_9_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_10_R1.corefeatures.nms.NMS_V1_10_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_11_R1.corefeatures.nms.NMS_V1_11_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_12_R1.corefeatures.nms.NMS_V1_12_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_13_R1.corefeatures.nms.NMS_V1_13_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_13_R2.corefeatures.nms.NMS_V1_13_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_14_R1.corefeatures.nms.NMS_V1_14_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_15_R1.corefeatures.nms.NMS_V1_15_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R1.multiversion.nms.NMS_V1_16_R1; // Note: v1_16_R1 has a slightly different path structure
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R2.corefeatures.nms.NMS_V1_16_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R3.corefeatures.nms.NMS_V1_16_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_17_R1.corefeatures.nms.NMS_V1_17_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R1.corefeatures.nms.NMS_V1_18_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R2.corefeatures.nms.NMS_V1_18_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R1.corefeatures.nms.NMS_V1_19_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R2.corefeatures.nms.NMS_V1_19_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R3.corefeatures.nms.NMS_V1_19_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R1.corefeatures.nms.NMS_V1_20_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R2.corefeatures.nms.NMS_V1_20_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R3.corefeatures.nms.NMS_V1_20_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R4.corefeatures.nms.NMS_V1_20_R4;

// Imports for version-specific MultiVersion implementations
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_8_R2.corefeatures.multiversion.MultiVersion_V1_8_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_8_R3.corefeatures.multiversion.MultiVersion_V1_8_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_9_R1.corefeatures.multiversion.MultiVersion_V1_9_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_9_R2.corefeatures.multiversion.MultiVersion_V1_9_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_10_R1.corefeatures.multiversion.MultiVersion_V1_10_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_11_R1.corefeatures.multiversion.MultiVersion_V1_11_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_12_R1.corefeatures.multiversion.MultiVersion_V1_12_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_13_R1.corefeatures.multiversion.MultiVersion_V1_13_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_13_R2.corefeatures.multiversion.MultiVersion_V1_13_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_14_R1.corefeatures.multiversion.MultiVersion_V1_14_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_15_R1.corefeatures.multiversion.MultiVersion_V1_15_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R1.multiversion.multiversion.MultiVersion_V1_16_R1; // Note: v1_16_R1 has a slightly different path structure
// Skipping MultiVersion_V1_20_R2 due to previous tool issues with its path/content
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R2.corefeatures.multiversion.MultiVersion_V1_16_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R3.corefeatures.multiversion.MultiVersion_V1_16_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_17_R1.corefeatures.multiversion.MultiVersion_V1_17_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R1.corefeatures.multiversion.MultiVersion_V1_18_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R2.corefeatures.multiversion.MultiVersion_V1_18_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R1.corefeatures.multiversion.MultiVersion_V1_19_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R2.corefeatures.multiversion.MultiVersion_V1_19_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R3.corefeatures.multiversion.MultiVersion_V1_19_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R1.corefeatures.multiversion.MultiVersion_V1_20_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R3.corefeatures.multiversion.MultiVersion_V1_20_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R4.corefeatures.multiversion.MultiVersion_V1_20_R4;

// Imports for version-specific SkyBlockInventory implementations
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_8_R2.corefeatures.multiversion.SkyBlockInventory_V1_8_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_8_R3.corefeatures.multiversion.SkyBlockInventory_V1_8_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_9_R1.corefeatures.multiversion.SkyBlockInventory_V1_9_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_9_R2.corefeatures.multiversion.SkyBlockInventory_V1_9_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_10_R1.corefeatures.multiversion.SkyBlockInventory_V1_10_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_11_R1.corefeatures.multiversion.SkyBlockInventory_V1_11_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_12_R1.corefeatures.multiversion.SkyBlockInventory_V1_12_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_13_R1.corefeatures.multiversion.SkyBlockInventory_V1_13_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_13_R2.corefeatures.multiversion.SkyBlockInventory_V1_13_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_14_R1.corefeatures.multiversion.SkyBlockInventory_V1_14_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_15_R1.corefeatures.multiversion.SkyBlockInventory_V1_15_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R1.multiversion.multiversion.SkyBlockInventory_V1_16_R1; // Note: v1_16_R1 has a slightly different path structure
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R2.corefeatures.multiversion.SkyBlockInventory_V1_16_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_16_R3.corefeatures.multiversion.SkyBlockInventory_V1_16_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_17_R1.corefeatures.multiversion.SkyBlockInventory_V1_17_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R1.corefeatures.multiversion.SkyBlockInventory_V1_18_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_18_R2.corefeatures.multiversion.SkyBlockInventory_V1_18_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R1.corefeatures.multiversion.SkyBlockInventory_V1_19_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R2.corefeatures.multiversion.SkyBlockInventory_V1_19_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_19_R3.corefeatures.multiversion.SkyBlockInventory_V1_19_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R1.corefeatures.multiversion.SkyBlockInventory_V1_20_R1;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R2.corefeatures.multiversion.SkyBlockInventory_V1_20_R2;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R3.corefeatures.multiversion.SkyBlockInventory_V1_20_R3;
import com.knemis.skyblock.skyblockcoreproject.corefeatures.multiversion.v1_20_R4.corefeatures.multiversion.SkyBlockInventory_V1_20_R4;

import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
public enum MinecraftVersion {

    /**
     * IntelliJ will recommend you to replace these with method reference.
     * However, this would break the plugins on some machines running the HotSpot VM.
     * Just leave this as it is and add new versions down below in the same way.
     */
    DEFAULT(() -> new NMSDefault(), MultiversionDefault::new, IridiumInventoryDefault::new),
    V1_8_R2(() -> new NMS_V1_8_R2(), MultiVersion_V1_8_R2::new, SkyBlockInventory_V1_8_R2::new),
    V1_8_R3(() -> new NMS_V1_8_R3(), MultiVersion_V1_8_R3::new, SkyBlockInventory_V1_8_R3::new),
    V1_9_R1(() -> new NMS_V1_9_R1(), MultiVersion_V1_9_R1::new, SkyBlockInventory_V1_9_R1::new),
    V1_9_R2(() -> new NMS_V1_9_R2(), MultiVersion_V1_9_R2::new, SkyBlockInventory_V1_9_R2::new),
    V1_10_R1(() -> new NMS_V1_10_R1(), MultiVersion_V1_10_R1::new, SkyBlockInventory_V1_10_R1::new),
    V1_11_R1(() -> new NMS_V1_11_R1(), MultiVersion_V1_11_R1::new, SkyBlockInventory_V1_11_R1::new),
    V1_12_R1(() -> new NMS_V1_12_R1(), MultiVersion_V1_12_R1::new, SkyBlockInventory_V1_12_R1::new),
    V1_13_R1(() -> new NMS_V1_13_R1(), MultiVersion_V1_13_R1::new, SkyBlockInventory_V1_13_R1::new),
    V1_13_R2(() -> new NMS_V1_13_R2(), MultiVersion_V1_13_R2::new, SkyBlockInventory_V1_13_R2::new),
    V1_14_R1(() -> new NMS_V1_14_R1(), MultiVersion_V1_14_R1::new, SkyBlockInventory_V1_14_R1::new),
    V1_15_R1(() -> new NMS_V1_15_R1(), MultiVersion_V1_15_R1::new, SkyBlockInventory_V1_15_R1::new),
    V1_16_R1(() -> new NMS_V1_16_R1(), MultiVersion_V1_16_R1::new, SkyBlockInventory_V1_16_R1::new),
    V1_16_R2(() -> new NMS_V1_16_R2(), MultiVersion_V1_16_R2::new, SkyBlockInventory_V1_16_R2::new),
    V1_16_R3(() -> new NMS_V1_16_R3(), MultiVersion_V1_16_R3::new, SkyBlockInventory_V1_16_R3::new),
    V1_17_R1(() -> new NMS_V1_17_R1(), MultiVersion_V1_17_R1::new, SkyBlockInventory_V1_17_R1::new),
    V1_18_R1(() -> new NMS_V1_18_R1(), MultiVersion_V1_18_R1::new, SkyBlockInventory_V1_18_R1::new),
    V1_18_R2(() -> new NMS_V1_18_R2(), MultiVersion_V1_18_R2::new, SkyBlockInventory_V1_18_R2::new),
    V1_19_R1(() -> new NMS_V1_19_R1(), MultiVersion_V1_19_R1::new, SkyBlockInventory_V1_19_R1::new),
    V1_19_R2(() -> new NMS_V1_19_R2(), MultiVersion_V1_19_R2::new, SkyBlockInventory_V1_19_R2::new),
    V1_19_R3(() -> new NMS_V1_19_R3(), MultiVersion_V1_19_R3::new, SkyBlockInventory_V1_19_R3::new),
    V1_20_R1(() -> new NMS_V1_20_R1(), MultiVersion_V1_20_R1::new, SkyBlockInventory_V1_20_R1::new),
    V1_20_R2(() -> new NMS_V1_20_R2(), MultiVersion_V1_20_R2::new, SkyBlockInventory_V1_20_R2::new),
    V1_20_R3(() -> new NMS_V1_20_R3(), MultiVersion_V1_20_R3::new, SkyBlockInventory_V1_20_R3::new),
    V1_20_R4(() -> new NMS_V1_20_R4(), MultiVersion_V1_20_R4::new, SkyBlockInventory_V1_20_R4::new);

    private final Supplier<NMS> nmsSupplier;
    private final JavaPluginSupplier<MultiVersion> multiVersionSupplier;
    private final Supplier<IridiumInventory> inventorySupplier;

    MinecraftVersion(Supplier<NMS> nmsSupplier, JavaPluginSupplier<MultiVersion> multiVersionSupplier, Supplier<IridiumInventory> inventorySupplier) {
        this.nmsSupplier = nmsSupplier;
        this.multiVersionSupplier = multiVersionSupplier;
        this.inventorySupplier = inventorySupplier;
    }

    public NMS getNms() {
        return nmsSupplier.get();
    }

    public MultiVersion getMultiVersion(JavaPlugin javaPlugin) {
        return multiVersionSupplier.get(javaPlugin);
    }

    public IridiumInventory getInventory() {
        return inventorySupplier.get();
    }

    @NotNull
    public static MinecraftVersion byName(String version) {
        for (MinecraftVersion minecraftVersion : values()) {
            if (minecraftVersion.name().equalsIgnoreCase(version)) {
                return minecraftVersion;
            }
        }

        return DEFAULT;
    }

}