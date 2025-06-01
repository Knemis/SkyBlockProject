package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class IntTag extends Tag<Integer> {

    private final int value;

    public IntTag(String name, int value) {
        super(name);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_INT;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public String toString() {
        return "TAG_Int(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntTag)) return false;
        if (!super.equals(obj)) return false;
        IntTag intTag = (IntTag) obj;
        return value == intTag.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public IntTag clone() {
        return new IntTag(getName(), value);
    }
}
