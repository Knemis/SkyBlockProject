package com.knemis.skyblock.skyblockcoreproject.core.keviincore;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.MultiVersion;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.SkyBlockInventory;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.SkyBlockInventoryDefault;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.SkyBlockMultiversionDefault;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_8_R2.SkyBlockInventory_V1_8_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_8_R2.SkyBlockMultiVersion_V1_8_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_8_R2.SkyBlockNMS_V1_8_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_8_R3.SkyBlockInventory_V1_8_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_8_R3.SkyBlockMultiVersion_V1_8_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_8_R3.SkyBlockNMS_V1_8_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_9_R1.SkyBlockInventory_V1_9_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_9_R1.SkyBlockMultiVersion_V1_9_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_9_R1.SkyBlockNMS_V1_9_R1;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_13_R1.SkyBlockInventory_V1_13_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_13_R1.SkyBlockMultiVersion_V1_13_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_13_R1.SkyBlockNMS_V1_13_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_13_R2.SkyBlockInventory_V1_13_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_13_R2.SkyBlockMultiVersion_V1_13_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_13_R2.SkyBlockNMS_V1_13_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_14_R1.SkyBlockInventory_V1_14_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_14_R1.SkyBlockMultiVersion_V1_14_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_14_R1.SkyBlockNMS_V1_14_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_15_R1.SkyBlockInventory_V1_15_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_15_R1.SkyBlockMultiVersion_V1_15_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_15_R1.SkyBlockNMS_V1_15_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R1.SkyBlockInventory_V1_16_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R1.SkyBlockMultiVersion_V1_16_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R1.SkyBlockNMS_V1_16_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R2.SkyBlockInventory_V1_16_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R2.SkyBlockMultiVersion_V1_16_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R2.SkyBlockNMS_V1_16_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R3.SkyBlockInventory_V1_16_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_16_R3.SkyBlockNMS_V1_16_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_17_R1.SkyBlockInventory_V1_17_R1;

import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_17_R1.SkyBlockNMS_V1_17_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_18_R1.SkyBlockInventory_V1_18_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_18_R1.SkyBlockMultiVersion_V1_18_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_18_R1.SkyBlockNMS_V1_18_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_18_R2.SkyBlockInventory_V1_18_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_18_R2.SkyBlockMultiVersion_V1_18_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_18_R2.SkyBlockNMS_V1_18_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R1.SkyBlockInventory_V1_19_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R1.SkyBlockMultiVersion_V1_19_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R1.SkyBlockNMS_V1_19_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R2.SkyBlockInventory_V1_19_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R2.SkyBlockMultiVersion_V1_19_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R2.SkyBlockNMS_V1_19_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R3.SkyBlockInventory_V1_19_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R3.SkyBlockMultiVersion_V1_19_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_19_R3.SkyBlockNMS_V1_19_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R1.SkyBlockInventory_V1_20_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R1.SkyBlockMultiVersion_V1_20_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R1.SkyBlockNMS_V1_20_R1;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R2.SkyBlockInventory_V1_20_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R2.SkyBlockMultiVersion_V1_20_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R2.SkyBlockNMS_V1_20_R2;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R3.SkyBlockInventory_V1_20_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R3.SkyBlockMultiVersion_V1_20_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R3.SkyBlockNMS_V1_20_R3;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R4.SkyBlockInventory_V1_20_R4;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R4.SkyBlockMultiVersion_V1_20_R4;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.multiversion.v1_20_R4.SkyBlockNMS_V1_20_R4;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.nms.NMS;
import com.knemis.skyblock.skyblockcoreproject.core.keviincore.nms.SkyBlockNMSDefault;

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
    DEFAULT(() -> new SkyBlockNMSDefault(), SkyBlockMultiversionDefault::new, SkyBlockInventoryDefault::new),
    V1_8_R2(() -> new SkyBlockNMS_V1_8_R2(), SkyBlockMultiVersion_V1_8_R2::new, SkyBlockInventory_V1_8_R2::new),
    V1_8_R3(() -> new SkyBlockNMS_V1_8_R3(), SkyBlockMultiVersion_V1_8_R3::new, SkyBlockInventory_V1_8_R3::new),
    V1_9_R1(() -> new SkyBlockNMS_V1_9_R1(), SkyBlockMultiVersion_V1_9_R1::new, SkyBlockInventory_V1_9_R1::new),

    V1_13_R1(() -> new SkyBlockNMS_V1_13_R1(), SkyBlockMultiVersion_V1_13_R1::new, SkyBlockInventory_V1_13_R1::new),
    V1_13_R2(() -> new SkyBlockNMS_V1_13_R2(), SkyBlockMultiVersion_V1_13_R2::new, SkyBlockInventory_V1_13_R2::new),
    V1_14_R1(() -> new SkyBlockNMS_V1_14_R1(), SkyBlockMultiVersion_V1_14_R1::new, SkyBlockInventory_V1_14_R1::new),
    V1_15_R1(() -> new SkyBlockNMS_V1_15_R1(), SkyBlockMultiVersion_V1_15_R1::new, SkyBlockInventory_V1_15_R1::new),
    V1_16_R1(() -> new SkyBlockNMS_V1_16_R1(), SkyBlockMultiVersion_V1_16_R1::new, SkyBlockInventory_V1_16_R1::new),
    V1_16_R2(() -> new SkyBlockNMS_V1_16_R2(), SkyBlockMultiVersion_V1_16_R2::new, SkyBlockInventory_V1_16_R2::new),

    V1_18_R1(() -> new SkyBlockNMS_V1_18_R1(), SkyBlockMultiVersion_V1_18_R1::new, SkyBlockInventory_V1_18_R1::new),
    V1_18_R2(() -> new SkyBlockNMS_V1_18_R2(), SkyBlockMultiVersion_V1_18_R2::new, SkyBlockInventory_V1_18_R2::new),
    V1_19_R1(() -> new SkyBlockNMS_V1_19_R1(), SkyBlockMultiVersion_V1_19_R1::new, SkyBlockInventory_V1_19_R1::new),
    V1_19_R2(() -> new SkyBlockNMS_V1_19_R2(), SkyBlockMultiVersion_V1_19_R2::new, SkyBlockInventory_V1_19_R2::new),
    V1_19_R3(() -> new SkyBlockNMS_V1_19_R3(), SkyBlockMultiVersion_V1_19_R3::new, SkyBlockInventory_V1_19_R3::new),
    V1_20_R1(() -> new SkyBlockNMS_V1_20_R1(), SkyBlockMultiVersion_V1_20_R1::new, SkyBlockInventory_V1_20_R1::new),
    V1_20_R2(() -> new SkyBlockNMS_V1_20_R2(), SkyBlockMultiVersion_V1_20_R2::new, SkyBlockInventory_V1_20_R2::new),
    V1_20_R3(() -> new SkyBlockNMS_V1_20_R3(), SkyBlockMultiVersion_V1_20_R3::new, SkyBlockInventory_V1_20_R3::new),
    V1_20_R4(() -> new SkyBlockNMS_V1_20_R4(), SkyBlockMultiVersion_V1_20_R4::new, SkyBlockInventory_V1_20_R4::new);

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
