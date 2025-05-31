package com.knemis.skyblock.skyblockcoreproject.schematics; // Adjusted package

import lombok.AllArgsConstructor; // Keep if project uses Lombok
import org.jnbt.*; // JNBT dependency

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor // Keep if project uses Lombok
public class SchematicData {
    public final short width;
    public final short length;
    public final short height;
    public List<Tag> tileEntities; // Changed from BlockEntities for version 2 in original, stick to tileEntities for simplicity from snippet
    public byte[] blockdata;
    public Map<String, Tag> palette;
    public Integer version; // Added as per snippet

    public static SchematicData loadSchematic(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        NBTInputStream nbtStream = new NBTInputStream(stream);

        CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
        stream.close();
        nbtStream.close();
        Map<String, Tag> schematic = schematicTag.getValue();

        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

        // Version handling - original snippet had "Version" and "DataVersion"
        // For now, using "Version" as explicitly listed in the snippet's SchematicData constructor
        Integer version = null;
        if (schematic.containsKey("Version")) {
            version = getChildTag(schematic, "Version", IntTag.class).getValue();
        } else if (schematic.containsKey("DataVersion")) { // Fallback or alternative
             // DataVersion might be for Minecraft data version, not schematic version.
             // Sticking to "Version" for schematic versioning as per constructor.
             // If DataVersion is truly needed, its usage would need clarification.
        }


        Map<String, Tag> palette = getChildTag(schematic, "Palette", CompoundTag.class).getValue();
        byte[] blockdata = getChildTag(schematic, "BlockData", ByteArrayTag.class).getValue();

        List<Tag> tileEntitiesList;
        // The provided snippet for SchematicData has a constructor that takes 'tileEntities'
        // and the loadSchematic method has logic for "TileEntities" (v1) and "BlockEntities" (v2)
        // For simplicity and directness from the snippet's constructor, let's try to unify this.
        // The original snippet's loadSchematic does:
        // if (version == 1) { tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue(); }
        // else if (version == 2) { BlockEntities = getChildTag(schematic, "BlockEntities", ListTag.class).getValue(); }
        // else { tileEntities = Collections.emptyList(); }
        // The constructor is `SchematicData(width, length, height, tileEntities, blockdata, palette, version)`
        // So, we should assign to one variable name.
        if (schematic.containsKey("TileEntities")) { // Prefer "TileEntities" if present
            tileEntitiesList = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
        } else if (schematic.containsKey("BlockEntities")) { // Fallback to "BlockEntities"
            tileEntitiesList = getChildTag(schematic, "BlockEntities", ListTag.class).getValue();
        } else {
            tileEntitiesList = Collections.emptyList();
        }

        return new SchematicData(width, length, height, tileEntitiesList, blockdata, palette, version);
    }

    public static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws
            IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
}
