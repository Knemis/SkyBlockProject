package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class LongTag extends Tag<Long> {

    private final long value;

    public LongTag(String name, long value) {
        super(name);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_LONG;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeLong(value);
    }

    @Override
    public String toString() {
        return "TAG_Long(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LongTag)) return false;
        if (!super.equals(obj)) return false;
        LongTag longTag = (LongTag) obj;
        return value == longTag.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public LongTag clone() {
        return new LongTag(getName(), value);
    }
}
