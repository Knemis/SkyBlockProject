package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public final class StringTag extends Tag<String> {

    private final String value;

    public StringTag(String name, String value) {
        super(name);
        Objects.requireNonNull(value, "value cannot be null for StringTag");
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_STRING;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public String toString() {
        // Simple escape for quotes for readability, proper JSON escaping would be more complex
        String escapedValue = value.replace("\"", "\\\"");
        return "TAG_String(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") + "): \"" + escapedValue + "\"";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StringTag)) return false;
        if (!super.equals(obj)) return false;
        StringTag stringTag = (StringTag) obj;
        return value.equals(stringTag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public StringTag clone() {
        return new StringTag(getName(), value);
    }
}
