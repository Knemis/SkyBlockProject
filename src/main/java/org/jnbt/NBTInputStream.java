package org.jnbt;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class NBTInputStream implements Closeable {

    private final DataInput dataInput;
    private static final int MAX_DEPTH = 512; // Max nesting depth

    public NBTInputStream(InputStream inputStream) throws IOException {
        // Assumes GZIP compressed, big-endian stream by default
        this(new DataInputStream(new GZIPInputStream(inputStream)));
    }

    public NBTInputStream(InputStream inputStream, boolean compressed, boolean littleEndian) throws IOException {
        // Note: littleEndian is not fully supported by this simple reader for multi-byte types.
        // Tag classes currently write big-endian.
        if (littleEndian) {
            // For true little-endian, DataInput would need to be wrapped or handled differently.
            // This is a placeholder; by default, DataInputStream is big-endian.
            // Consider using a library like Apache Commons IO for EndianUtils if needed.
            System.err.println("Warning: NBTInputStream created with littleEndian=true, but underlying DataInputStream is big-endian. Full little-endian support for numeric types is not implemented in this basic version.");
        }
        this.dataInput = new DataInputStream(compressed ? new GZIPInputStream(inputStream) : inputStream);
    }

    public NBTInputStream(DataInput dataInput) {
        this.dataInput = dataInput;
    }

    /**
     * Reads a complete NBT tag, including its type, name, and payload.
     * This is the primary method to start reading an NBT structure.
     * @return The NamedTag read from the stream.
     * @throws IOException If an I/O error occurs or the NBT format is invalid.
     */
    public NamedTag readNamedTag() throws IOException {
        return readNamedTag(0);
    }

    protected NamedTag readNamedTag(int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IOException("NBT structure too deeply nested (max depth " + MAX_DEPTH + ")");
        }

        int typeId = dataInput.readByte() & 0xFF;
        if (typeId == NBTConstants.TYPE_END) {
            return new NamedTag(null, new EndTag()); // EndTag has no name
        }

        String name = dataInput.readUTF();
        Tag<?> payload = readTagPayload(typeId, name, depth);
        return new NamedTag(name, payload);
    }

    /**
     * Reads an unnamed NBT tag payload. Used for elements within a ListTag.
     * @param typeId The type ID of the tag to read.
     * @param depth The current nesting depth.
     * @return The tag payload.
     * @throws IOException If an I/O error occurs.
     */
    protected Tag<?> readUnnamedTag(int typeId, int depth) throws IOException {
         if (depth > MAX_DEPTH) {
            throw new IOException("NBT structure too deeply nested (max depth " + MAX_DEPTH + ")");
        }
        // Pass null for name as unnamed tags don't have one in this context
        return readTagPayload(typeId, null, depth);
    }


    private Tag<?> readTagPayload(int typeId, String name, int depth) throws IOException {
        switch (typeId) {
            case NBTConstants.TYPE_END:
                // Should not happen here if called from readNamedTag after checking typeId,
                // but could happen if an unnamed EndTag is attempted (e.g. in a ListTag of EndTags, which is invalid)
                if (name != null) { // Named EndTag is invalid except at the end of a compound.
                     throw new IOException("Unexpected TAG_End with name " + name + " encountered.");
                }
                return new EndTag();
            case NBTConstants.TYPE_BYTE:
                return new ByteTag(name, dataInput.readByte());
            case NBTConstants.TYPE_SHORT:
                return new ShortTag(name, dataInput.readShort());
            case NBTConstants.TYPE_INT:
                return new IntTag(name, dataInput.readInt());
            case NBTConstants.TYPE_LONG:
                return new LongTag(name, dataInput.readLong());
            case NBTConstants.TYPE_FLOAT:
                return new FloatTag(name, dataInput.readFloat());
            case NBTConstants.TYPE_DOUBLE:
                return new DoubleTag(name, dataInput.readDouble());
            case NBTConstants.TYPE_BYTE_ARRAY:
                int byteArrayLength = dataInput.readInt();
                byte[] byteArray = new byte[byteArrayLength];
                dataInput.readFully(byteArray);
                return new ByteArrayTag(name, byteArray);
            case NBTConstants.TYPE_STRING:
                return new StringTag(name, dataInput.readUTF());
            case NBTConstants.TYPE_LIST:
                int listElementTypeId = dataInput.readByte() & 0xFF;
                int listSize = dataInput.readInt();
                if (listSize < 0) throw new IOException("Negative list size " + listSize);

                List<Tag<?>> list = new ArrayList<>(listSize);
                Class<? extends Tag<?>> listElementType = NBTUtils.getTagClass(listElementTypeId);

                for (int i = 0; i < listSize; i++) {
                    // Tags in a list are unnamed and read by their payload type directly.
                    list.add(readUnnamedTag(listElementTypeId, depth + 1));
                }
                // The constructor of ListTag will validate if all elements are indeed of listElementType
                // For an empty list, listElementTypeId would be TYPE_END.
                // The ListTag constructor handles the type of the list based on the actual elements or specified type.
                return new ListTag<>(name, listElementTypeId == NBTConstants.TYPE_END && list.isEmpty() ? EndTag.class : listElementType, list);

            case NBTConstants.TYPE_COMPOUND:
                Map<String, Tag<?>> compoundMap = new LinkedHashMap<>();
                NamedTag namedTag;
                while (true) {
                    namedTag = readNamedTag(depth + 1);
                    if (namedTag.getTag().getID() == NBTConstants.TYPE_END) {
                        break; // End of compound
                    }
                    compoundMap.put(namedTag.getName(), namedTag.getTag());
                }
                return new CompoundTag(name, compoundMap);
            case NBTConstants.TYPE_INT_ARRAY:
                int intArrayLength = dataInput.readInt();
                int[] intArray = new int[intArrayLength];
                for (int i = 0; i < intArrayLength; i++) {
                    intArray[i] = dataInput.readInt();
                }
                return new IntArrayTag(name, intArray);
            case NBTConstants.TYPE_LONG_ARRAY: // Added in MC 1.12
                int longArrayLength = dataInput.readInt();
                long[] longArray = new long[longArrayLength];
                for (int i = 0; i < longArrayLength; i++) {
                    longArray[i] = dataInput.readLong();
                }
                return new LongArrayTag(name, longArray);
            default:
                throw new IOException("Unknown NBT type ID: " + typeId);
        }
    }

    @Override
    public void close() throws IOException {
        if (dataInput instanceof Closeable) {
            ((Closeable) dataInput).close();
        }
    }

    /**
     * Helper class to represent a named tag, as the root of an NBT structure is always a named CompoundTag.
     * Can also be used if one needs to read a single named tag not part of a compound.
     */
    public static class NamedTag {
        private final String name;
        private final Tag<?> tag;

        public NamedTag(String name, Tag<?> tag) {
            this.name = name; // Can be null if it's an unnamed EndTag at the root (though unusual)
            this.tag = tag;
        }

        public String getName() {
            return name;
        }

        public Tag<?> getTag() {
            return tag;
        }

        @SuppressWarnings("unchecked")
        public <T extends Tag<?>> T getTag(Class<T> expectedType) {
            if (!expectedType.isInstance(tag)) {
                throw new ClassCastException("Tag is of type " + tag.getClass().getSimpleName() +
                                             ", expected " + expectedType.getSimpleName());
            }
            return (T) tag;
        }
    }
}
