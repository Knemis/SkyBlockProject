package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class ByteTag extends Tag<Byte> {

    private final byte value;

    public ByteTag(String name, byte value) {
        super(name);
        this.value = value;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_BYTE;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeByte(value);
    }

    @Override
    public String toString() {
        return "TAG_Byte(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ByteTag)) return false;
        if (!super.equals(obj)) return false; // Checks name equality from Tag class
        ByteTag byteTag = (ByteTag) obj;
        return value == byteTag.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public ByteTag clone() {
        return new ByteTag(getName(), value);
    }
}
