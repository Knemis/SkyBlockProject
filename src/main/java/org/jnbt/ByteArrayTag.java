package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class ByteArrayTag extends Tag<byte[]> {

    private final byte[] value;

    public ByteArrayTag(String name, byte[] value) {
        super(name);
        Objects.requireNonNull(value, "value cannot be null for ByteArrayTag");
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        // Return a clone to protect the internal array from external modification
        return value.clone();
    }

    /**
     * Returns the underlying byte array without cloning.
     * Use with caution as modifications to the returned array will affect this tag's state.
     * @return The internal byte array.
     */
    public byte[] getRawValue() {
        return value;
    }


    @Override
    public int getID() {
        return NBTConstants.TYPE_BYTE_ARRAY;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeInt(value.length);
        out.write(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TAG_ByteArray(").append(getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"").append("): [");
        for (int i = 0; i < Math.min(value.length, 32); i++) { // Print max 32 elements for brevity
            if (i > 0) {
                sb.append(',');
            }
            sb.append(String.format("0x%02X", value[i]));
        }
        if (value.length > 32) {
            sb.append("...");
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ByteArrayTag)) return false;
        if (!super.equals(obj)) return false;
        ByteArrayTag that = (ByteArrayTag) obj;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public ByteArrayTag clone() {
        return new ByteArrayTag(getName(), value.clone());
    }
}
