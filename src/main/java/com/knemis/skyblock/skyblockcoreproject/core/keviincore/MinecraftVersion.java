package com.knemis.skyblock.skyblockcoreproject.core.keviincore;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.*;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.nms.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
public enum MinecraftVersion {

    /**
     * IntelliJ will recommend you to replace these with method reference.
     * However, this would break the plugins on some machines running the HotSpot VM.
     * Just leave this as it is and add new versions down below in the same way.
     */
    DEFAULT(() -> new NMSDefault(), MultiversionDefault::new, SkyBlockInventoryDefault::new),
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
    private final Supplier<SkyBlockInventory> skyBlockInventorySupplier;

    MinecraftVersion(Supplier<NMS> nmsSupplier, JavaPluginSupplier<MultiVersion> multiVersionSupplier, Supplier<SkyBlockInventory> inventorySupplier) {
        this.nmsSupplier = nmsSupplier;
        this.multiVersionSupplier = multiVersionSupplier;
        this.skyBlockInventorySupplier = inventorySupplier;
    }

    public NMS getNms() {
        return nmsSupplier.get();
    }

    public MultiVersion getMultiVersion(JavaPlugin javaPlugin) {
        return multiVersionSupplier.get(javaPlugin);
    }

    public SkyBlockInventory getSkyBlockInventory() {
        return skyBlockInventorySupplier.get();
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
