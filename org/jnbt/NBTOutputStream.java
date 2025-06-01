package org.jnbt;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class NBTOutputStream implements Closeable {

    private final DataOutput dataOutput;

    /**
     * Creates a new NBTOutputStream, which will write NBT data to the specified GZIP-compressed OutputStream.
     * Assumes Big Endian format.
     * @param outputStream The output stream.
     * @throws IOException if an I/O error occurs.
     */
    public NBTOutputStream(OutputStream outputStream) throws IOException {
        this(new DataOutputStream(new GZIPOutputStream(outputStream)), false); // Default: compressed, big-endian
    }

    /**
     * Creates a new NBTOutputStream, which will write NBT data to the specified OutputStream.
     * @param outputStream The output stream.
     * @param compressed Whether the stream should be GZIP compressed.
     * @param littleEndian If true, attempts to write in little-endian format (currently not fully supported for numerics).
     * @throws IOException if an I/O error occurs.
     */
    public NBTOutputStream(OutputStream outputStream, boolean compressed, boolean littleEndian) throws IOException {
        // Note: littleEndian parameter is currently a placeholder.
        // The underlying DataOutputStream and Tag write methods use big-endian.
        // True little-endian writing would require a custom DataOutput wrapper.
        if (littleEndian) {
            System.err.println("Warning: NBTOutputStream created with littleEndian=true, but writes are Big Endian. Full little-endian support for numeric types is not implemented in this basic version.");
        }
        this.dataOutput = new DataOutputStream(compressed ? new GZIPOutputStream(outputStream) : outputStream);
    }

    /**
     * Creates a new NBTOutputStream, which will write NBT data to the specified DataOutput.
     * @param dataOutput The data output.
     */
    public NBTOutputStream(DataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    /**
     * Writes a named NBT tag to the stream.
     * This is typically the root tag of an NBT structure (which must be a named CompoundTag).
     * The name provided here will be set on the tag before writing.
     *
     * @param name The name of the tag to write. Must not be null.
     * @param tag The tag to write. Must not be an EndTag.
     * @throws IOException If an I/O error occurs or if attempting to write an EndTag with a name.
     * @throws NullPointerException if name is null.
     */
    public void writeNamedTag(String name, Tag<?> tag) throws IOException {
        if (name == null) {
            throw new NullPointerException("Name cannot be null for a named tag.");
        }
        if (tag.getID() == NBTConstants.TYPE_END) {
            // While an unnamed EndTag terminates a CompoundTag, a *named* EndTag is invalid.
            throw new IOException("Cannot write a named EndTag. EndTags are unnamed terminators for CompoundTags.");
        }

        // Set or override the tag's name to ensure it's written correctly.
        tag.setName(name);
        tag.write(dataOutput); // Tag.write handles typeID, name, and payload
    }

    /**
     * Writes an NBT Tag to the stream.
     * The tag's internal name (if applicable) and payload will be written.
     * This is useful for writing elements of a ListTag or other scenarios where the tag object is already fully formed.
     * @param tag The tag to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTag(Tag<?> tag) throws IOException {
        // Assumes the tag's name (if it's not an EndTag) is already correctly set within the Tag object.
        tag.write(dataOutput);
    }

    @Override
    public void close() throws IOException {
        if (dataOutput instanceof Closeable) {
            ((Closeable) dataOutput).close();
        }
    }
}
