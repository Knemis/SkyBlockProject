package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class DoubleTag extends Tag<Double> {

    private final double value;

    public DoubleTag(String name, double value) {
        super(name);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_DOUBLE;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeDouble(value);
    }

    @Override
    public String toString() {
        return "TAG_Double(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DoubleTag)) return false;
        if (!super.equals(obj)) return false;
        DoubleTag doubleTag = (DoubleTag) obj;
        return Double.compare(doubleTag.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public DoubleTag clone() {
        return new DoubleTag(getName(), value);
    }
}
