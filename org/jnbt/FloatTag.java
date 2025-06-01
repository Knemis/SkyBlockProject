package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class FloatTag extends Tag<Float> {

    private final float value;

    public FloatTag(String name, float value) {
        super(name);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_FLOAT;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeFloat(value);
    }

    @Override
    public String toString() {
        return "TAG_Float(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FloatTag)) return false;
        if (!super.equals(obj)) return false;
        FloatTag floatTag = (FloatTag) obj;
        return Float.compare(floatTag.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public FloatTag clone() {
        return new FloatTag(getName(), value);
    }
}
