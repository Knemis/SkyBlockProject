package org.jnbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public abstract class Tag<T> implements Cloneable {

    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public abstract T getValue();

    public abstract int getID();

    protected abstract void writePayload(DataOutput out) throws IOException;

    public final void write(DataOutput out) throws IOException {
        int typeId = getID();
        out.writeByte(typeId);
        if (typeId != NBTConstants.TYPE_END) {
            out.writeUTF(name == null ? "" : name);
        }
        writePayload(out);
    }

    // Abstract methods for reading payload, to be implemented by subclasses
    // static Tag<?> readNamedTag(DataInput in, int depth) throws IOException; // Example, might be in NBTInputStream
    // abstract void readPayload(DataInput in, int depth) throws IOException; // Example

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    @SuppressWarnings("unchecked")
    public Tag<T> clone() {
        try {
            return (Tag<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // Should not happen
        }
    }

    protected String valueToString(int maxDepth) {
        return getValue().toString();
    }
}
