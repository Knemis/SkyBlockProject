package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;

public final class EndTag extends Tag<Void> {

    public EndTag() {
        super(null); // EndTag has no name, name is not written for EndTag
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_END;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        // No payload for EndTag
    }

    @Override
    public String toString() {
        return "TAG_End()";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof EndTag; // All EndTags are considered equal
    }

    @Override
    public int hashCode() {
        return getID(); // Based on type ID
    }

    @Override
    public EndTag clone() {
        return new EndTag();
    }
}
