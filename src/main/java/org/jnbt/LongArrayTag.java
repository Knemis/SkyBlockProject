package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class LongArrayTag extends Tag<long[]> {

    private final long[] value;

    public LongArrayTag(String name, long[] value) {
        super(name);
        Objects.requireNonNull(value, "value cannot be null for LongArrayTag");
        this.value = value;
    }

    @Override
    public long[] getValue() {
        return value.clone();
    }

    /**
     * Returns the underlying long array without cloning.
     * Use with caution as modifications to the returned array will affect this tag's state.
     * @return The internal long array.
     */
    public long[] getRawValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_LONG_ARRAY;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeInt(value.length);
        for (long val : value) {
            out.writeLong(val);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TAG_LongArray(").append(getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"").append("): [");
        for (int i = 0; i < Math.min(value.length, 32); i++) { // Print max 32 elements
            if (i > 0) {
                sb.append(',');
            }
            sb.append(value[i]).append("L");
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
        if (!(obj instanceof LongArrayTag)) return false;
        if (!super.equals(obj)) return false;
        LongArrayTag that = (LongArrayTag) obj;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public LongArrayTag clone() {
        return new LongArrayTag(getName(), value.clone());
    }
}
