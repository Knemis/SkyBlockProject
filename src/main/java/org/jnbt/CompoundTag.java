package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.ArrayList; // Added for getList default return
// Added import for NBTUtils
import org.jnbt.NBTUtils;


public final class CompoundTag extends Tag<Map<String, Tag<?>>> {

    // Using LinkedHashMap to preserve insertion order, which can be important.
    private final Map<String, Tag<?>> value;

    public CompoundTag(String name, Map<String, Tag<?>> value) {
        super(name);
        Objects.requireNonNull(value, "Value map cannot be null for CompoundTag");
        // Create a new LinkedHashMap to ensure internal state is not affected by external map modifications
        // and to maintain a specific map implementation.
        this.value = new LinkedHashMap<>(value);
    }

    @Override
    public Map<String, Tag<?>> getValue() {
        return Collections.unmodifiableMap(value);
    }

    public void put(String key, Tag<?> tag) {
        Objects.requireNonNull(key, "Key cannot be null for CompoundTag put");
        Objects.requireNonNull(tag, "Tag cannot be null for CompoundTag put");
        if (tag.getID() == NBTConstants.TYPE_END) {
            throw new IllegalArgumentException("Cannot add TAG_End to a CompoundTag");
        }
        value.put(key, tag);
    }

    public void putByte(String key, byte val) { put(key, new ByteTag(key, val)); }
    public void putShort(String key, short val) { put(key, new ShortTag(key, val)); }
    public void putInt(String key, int val) { put(key, new IntTag(key, val)); }
    public void putLong(String key, long val) { put(key, new LongTag(key, val)); }
    public void putFloat(String key, float val) { put(key, new FloatTag(key, val)); }
    public void putDouble(String key, double val) { put(key, new DoubleTag(key, val)); }
    public void putString(String key, String val) { put(key, new StringTag(key, val)); }
    public void putByteArray(String key, byte[] val) { put(key, new ByteArrayTag(key, val)); }
    public void putIntArray(String key, int[] val) { put(key, new IntArrayTag(key, val)); }
    public void putLongArray(String key, long[] val) { put(key, new LongArrayTag(key, val)); }
    // For ListTag and CompoundTag, the caller should construct the tag itself.

    public Tag<?> get(String key) {
        return value.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends Tag<?>> T get(String key, Class<T> expectedTagClass) {
        Tag<?> tag = value.get(key);
        if (tag == null) {
            return null;
        }
        if (!expectedTagClass.isInstance(tag)) {
            throw new ClassCastException("Tag '" + key + "' is of type " + NBTUtils.getTypeName(tag.getID()) +
                                         " (" + tag.getClass().getSimpleName() + ")" +
                                         ", expected " + expectedTagClass.getSimpleName());
        }
        return (T) tag;
    }

    public byte getByte(String key) { Tag<?> t = get(key); return t instanceof ByteTag ? ((ByteTag)t).getValue() : 0; }
    public short getShort(String key) { Tag<?> t = get(key); return t instanceof ShortTag ? ((ShortTag)t).getValue() : 0; }
    public int getInt(String key) { Tag<?> t = get(key); return t instanceof IntTag ? ((IntTag)t).getValue() : 0; }
    public long getLong(String key) { Tag<?> t = get(key); return t instanceof LongTag ? ((LongTag)t).getValue() : 0; }
    public float getFloat(String key) { Tag<?> t = get(key); return t instanceof FloatTag ? ((FloatTag)t).getValue() : 0; }
    public double getDouble(String key) { Tag<?> t = get(key); return t instanceof DoubleTag ? ((DoubleTag)t).getValue() : 0; }
    public String getString(String key) { Tag<?> t = get(key); return t instanceof StringTag ? ((StringTag)t).getValue() : ""; }
    public byte[] getByteArray(String key) { Tag<?> t = get(key); return t instanceof ByteArrayTag ? ((ByteArrayTag)t).getValue() : new byte[0]; }
    public int[] getIntArray(String key) { Tag<?> t = get(key); return t instanceof IntArrayTag ? ((IntArrayTag)t).getValue() : new int[0]; }
    public long[] getLongArray(String key) { Tag<?> t = get(key); return t instanceof LongArrayTag ? ((LongArrayTag)t).getValue() : new long[0]; }
    public CompoundTag getCompound(String key) { Tag<?> t = get(key); return t instanceof CompoundTag ? (CompoundTag)t : new CompoundTag(key, new LinkedHashMap<>());}
    @SuppressWarnings("unchecked")
    public <E extends Tag<?>> ListTag<E> getList(String key, Class<E> elementType) {
        Tag<?> tag = get(key);
        if (tag instanceof ListTag) {
            ListTag<?> listTag = (ListTag<?>) tag;
            if (listTag.getElementType().equals(elementType) || listTag.isEmpty()) {
                 return (ListTag<E>) listTag;
            } else {
                throw new ClassCastException("ListTag '" + key + "' contains " + listTag.getElementType().getSimpleName() +
                                             ", expected " + elementType.getSimpleName());
            }
        }
        return new ListTag<>(key, elementType, new ArrayList<>()); // Return empty list of correct type
    }


    public boolean containsKey(String key) {
        return value.containsKey(key);
    }

    public boolean containsKey(String key, int typeId) {
        Tag<?> tag = value.get(key);
        return tag != null && tag.getID() == typeId;
    }

    public Tag<?> remove(String key) {
        return value.remove(key);
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(value.keySet());
    }

    public Collection<Tag<?>> tags() { // Renamed from values() to avoid confusion with getValue()
        return Collections.unmodifiableCollection(value.values());
    }

    public int size() {
        return value.size();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_COMPOUND;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        for (Tag<?> tag : value.values()) {
            // Each tag in a compound tag is written with its type, name, and payload
            tag.write(out);
        }
        // Mark the end of the CompoundTag with a TAG_End
        out.writeByte(NBTConstants.TYPE_END);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",\n", "{\n", "\n}");
        sj.setEmptyValue("{}");
        for (Map.Entry<String, Tag<?>> entry : value.entrySet()) {
            // Indent sub-elements for better readability
            String itemStr = entry.getValue().toString();
            sj.add("  " + itemStr.replaceAll("\n", "\n  "));
        }
        return "TAG_Compound(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") +
               "): " + value.size() + " entries\n" + sj.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CompoundTag)) return false;
        if (!super.equals(obj)) return false; // Checks name
        CompoundTag that = (CompoundTag) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public CompoundTag clone() {
        Map<String, Tag<?>> clonedMap = new LinkedHashMap<>(value.size());
        for (Map.Entry<String, Tag<?>> entry : value.entrySet()) {
            clonedMap.put(entry.getKey(), entry.getValue().clone());
        }
        return new CompoundTag(getName(), clonedMap);
    }
}
