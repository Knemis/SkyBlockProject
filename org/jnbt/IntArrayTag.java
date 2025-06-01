package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class IntArrayTag extends Tag<int[]> {

    private final int[] value;

    public IntArrayTag(String name, int[] value) {
        super(name);
        Objects.requireNonNull(value, "value cannot be null for IntArrayTag");
        this.value = value;
    }

    @Override
    public int[] getValue() {
        return value.clone();
    }

    /**
     * Returns the underlying int array without cloning.
     * Use with caution as modifications to the returned array will affect this tag's state.
     * @return The internal int array.
     */
    public int[] getRawValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_INT_ARRAY;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeInt(value.length);
        for (int val : value) {
            out.writeInt(val);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TAG_IntArray(").append(getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"").append("): [");
        for (int i = 0; i < Math.min(value.length, 32); i++) { // Print max 32 elements
            if (i > 0) {
                sb.append(',');
            }
            sb.append(value[i]);
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
        if (!(obj instanceof IntArrayTag)) return false;
        if (!super.equals(obj)) return false;
        IntArrayTag that = (IntArrayTag) obj;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public IntArrayTag clone() {
        return new IntArrayTag(getName(), value.clone());
    }
}
