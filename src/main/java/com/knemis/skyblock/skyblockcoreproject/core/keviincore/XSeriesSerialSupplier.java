package com.knemis.skyblock.skyblockcoreproject.core.keviincore;

import com.cryptomorin.xseries.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Optional;
import org.bukkit.entity.EntityType;

public class XSeriesSerialSupplier {

    // Updated for XSeries 9.0.0+ compatibility

    public static class XMaterialSerializer extends StdSerializer<XMaterial> {
        public XMaterialSerializer() { super(XMaterial.class); }

        @Override
        public void serialize(XMaterial xMaterial, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(xMaterial.name());
        }
    }

    public static class XMaterialDeserializer extends StdDeserializer<XMaterial> {
        public XMaterialDeserializer() { super(XMaterial.class); }

        @Override
        public XMaterial deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String materialName = node.asText();
            Optional<XMaterial> material = XMaterial.matchXMaterial(materialName);
            if (!material.isPresent()) {
                SkyBlockSecondCore.getInstance().getLogger().warning("Could not deserialize " + materialName + " to a Material, defaulting to AIR");
            }
            return material.orElse(XMaterial.AIR);
        }
    }

    public static class XPotionSerializer extends StdSerializer<XPotion> {
        public XPotionSerializer() { super(XPotion.class); }

        @Override
        public void serialize(XPotion xPotion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(xPotion.name());
        }
    }

    public static class XPotionDeserializer extends StdDeserializer<XPotion> {
        public XPotionDeserializer() { super(XPotion.class); }

        @Override
        public XPotion deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String potionName = node.asText();
            Optional<XPotion> potion = XPotion.matchXPotion(potionName);
            if (!potion.isPresent()) {
                SkyBlockSecondCore.getInstance().getLogger().warning("Could not deserialize " + potionName + " to a Potion, defaulting to LUCK");
            }
            return potion.orElse(XPotion.LUCK);
        }
    }

    public static class XEnchantmentSerializer extends StdSerializer<XEnchantment> {
        public XEnchantmentSerializer() { super(XEnchantment.class); }

        @Override
        public void serialize(XEnchantment xEnchantment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(xEnchantment.getEnchant().getName());
        }
    }

    public static class XEnchantmentDeserializer extends StdDeserializer<XEnchantment> {
        public XEnchantmentDeserializer() { super(XEnchantment.class); }

        @Override
        public XEnchantment deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String enchantName = node.asText();
            Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(enchantName);
            if (!enchant.isPresent()) {
                SkyBlockSecondCore.getInstance().getLogger().warning("Could not deserialize " + enchantName + " to an Enchantment, defaulting to PROTECTION_ENVIRONMENTAL");
            }
            return enchant.orElse(XEnchantment.PROTECTION_ENVIRONMENTAL);
        }
    }

    public static class XBiomeSerializer extends StdSerializer<XBiome> {
        public XBiomeSerializer() { super(XBiome.class); }

        @Override
        public void serialize(XBiome xBiome, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(xBiome.name());
        }
    }

    public static class XBiomeDeserializer extends StdDeserializer<XBiome> {
        public XBiomeDeserializer() { super(XBiome.class); }

        @Override
        public XBiome deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String biomeName = node.asText();
            Optional<XBiome> biome = XBiome.matchXBiome(biomeName);
            if (!biome.isPresent()) {
                SkyBlockSecondCore.getInstance().getLogger().warning("Could not deserialize " + biomeName + " to a Biome, defaulting to PLAINS");
            }
            return biome.orElse(XBiome.PLAINS);
        }
    }

    public static class XSoundSerializer extends StdSerializer<XSound> {
        public XSoundSerializer() { super(XSound.class); }

        @Override
        public void serialize(XSound xSound, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(xSound.name());
        }
    }

    public static class XSoundDeserializer extends StdDeserializer<XSound> {
        public XSoundDeserializer() { super(XSound.class); }

        @Override
        public XSound deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String soundName = node.asText();
            Optional<XSound> sound = XSound.matchXSound(soundName);
            if (!sound.isPresent()) {
                SkyBlockSecondCore.getInstance().getLogger().warning("Could not deserialize " + soundName + " to a Sound, defaulting to ENTITY_PLAYER_LEVELUP");
            }
            return sound.orElse(XSound.ENTITY_PLAYER_LEVELUP);
        }
    }

    public static class EntityTypeSerializer extends StdSerializer<EntityType> {
        public EntityTypeSerializer() { super(EntityType.class); }

        @Override
        public void serialize(EntityType entityType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(entityType.name());
        }
    }

    public static class EntityTypeDeserializer extends StdDeserializer<EntityType> {
        public EntityTypeDeserializer() { super(EntityType.class); }

        @Override
        public EntityType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String entityTypeName = node.asText();
            try {
                return EntityType.valueOf(entityTypeName);
            } catch (IllegalArgumentException e) {
                SkyBlockSecondCore.getInstance().getLogger().warning("Could not deserialize " + entityTypeName + " to an EntityType, defaulting to WOLF");
                return EntityType.WOLF;
            }
        }
    }
}