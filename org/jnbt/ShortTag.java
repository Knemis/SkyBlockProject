package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class ShortTag extends Tag<Short> {

    private final short value;

    public ShortTag(String name, short value) {
        super(name);
        this.value = value;
    }

    @Override
    public Short getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_SHORT;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeShort(value);
    }

    @Override
    public String toString() {
        return "TAG_Short(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ShortTag)) return false;
        if (!super.equals(obj)) return false;
        ShortTag shortTag = (ShortTag) obj;
        return value == shortTag.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public ShortTag clone() {
        return new ShortTag(getName(), value);
    }
}
