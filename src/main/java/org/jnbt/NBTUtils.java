package org.jnbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class NBTUtils {

    private NBTUtils() {
        // Private constructor to prevent instantiation
    }

    public static String getTypeName(int typeId) {
        switch (typeId) {
            case NBTConstants.TYPE_END:         return "TAG_End";
            case NBTConstants.TYPE_BYTE:        return "TAG_Byte";
            case NBTConstants.TYPE_SHORT:       return "TAG_Short";
            case NBTConstants.TYPE_INT:         return "TAG_Int";
            case NBTConstants.TYPE_LONG:        return "TAG_Long";
            case NBTConstants.TYPE_FLOAT:       return "TAG_Float";
            case NBTConstants.TYPE_DOUBLE:      return "TAG_Double";
            case NBTConstants.TYPE_BYTE_ARRAY:  return "TAG_ByteArray";
            case NBTConstants.TYPE_STRING:      return "TAG_String";
            case NBTConstants.TYPE_LIST:        return "TAG_List";
            case NBTConstants.TYPE_COMPOUND:    return "TAG_Compound";
            case NBTConstants.TYPE_INT_ARRAY:   return "TAG_IntArray";
            case NBTConstants.TYPE_LONG_ARRAY:  return "TAG_LongArray"; // Minecraft 1.12
            default:
                return "UNKNOWN_TYPE_ID_" + typeId;
        }
    }

    public static Class<? extends Tag<?>> getTagClass(int typeId) {
        switch (typeId) {
            case NBTConstants.TYPE_END:         return EndTag.class;
            case NBTConstants.TYPE_BYTE:        return ByteTag.class;
            case NBTConstants.TYPE_SHORT:       return ShortTag.class;
            case NBTConstants.TYPE_INT:         return IntTag.class;
            case NBTConstants.TYPE_LONG:        return LongTag.class;
            case NBTConstants.TYPE_FLOAT:       return FloatTag.class;
            case NBTConstants.TYPE_DOUBLE:      return DoubleTag.class;
            case NBTConstants.TYPE_BYTE_ARRAY:  return ByteArrayTag.class;
            case NBTConstants.TYPE_STRING:      return StringTag.class;
            case NBTConstants.TYPE_LIST:        return ListTag.class; // Note: This is ListTag.class, not ListTag<T>.class
            case NBTConstants.TYPE_COMPOUND:    return CompoundTag.class;
            case NBTConstants.TYPE_INT_ARRAY:   return IntArrayTag.class;
            case NBTConstants.TYPE_LONG_ARRAY:  return LongArrayTag.class;
            default:
                throw new IllegalArgumentException("Unknown NBT type ID: " + typeId);
        }
    }

    /**
     * Reads a UTF-8 String from the DataInput.
     * NBT strings are prefixed with an unsigned short indicating the length in bytes.
     * @param in The DataInput stream.
     * @return The String read.
     * @throws IOException If an I/O error occurs.
     */
    public static String readString(DataInput in, int maxDepth) throws IOException {
        // Depth check isn't strictly necessary for string reading itself, but often part of a robust reader.
        // For now, keeping it simple as per original JNBT style.
        int length = in.readUnsignedShort();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Writes a UTF-8 String to the DataOutput.
     * NBT strings are prefixed with an unsigned short indicating the length in bytes.
     * @param out The DataOutput stream.
     * @param str The String to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeString(DataOutput out, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        out.writeShort(bytes.length);
        out.write(bytes);
    }
}
