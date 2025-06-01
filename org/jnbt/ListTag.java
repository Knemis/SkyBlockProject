package org.jnbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
// Added import for NBTUtils
import org.jnbt.NBTUtils;

public final class ListTag<T extends Tag<?>> extends Tag<List<T>> {

    private final Class<T> elementType;
    private final List<T> value;

    @SuppressWarnings("unchecked")
    public ListTag(String name, Class<? extends Tag<?>> elementType, List<T> value) {
        super(name);
        Objects.requireNonNull(elementType, "Element type cannot be null for ListTag");
        Objects.requireNonNull(value, "Value list cannot be null for ListTag");

        this.elementType = (Class<T>) elementType;

        // Ensure all tags in the list are of the specified type and not null
        // And create a new list to ensure immutability if the provided list is modified externally
        List<T> tempValue = new ArrayList<>(value.size());
        for (Tag<?> tag : value) {
            Objects.requireNonNull(tag, "ListTag cannot contain null elements");
            if (!this.elementType.isInstance(tag)) {
                throw new IllegalArgumentException(
                    "All tags in ListTag must be of type " + this.elementType.getSimpleName() +
                    ", found " + tag.getClass().getSimpleName());
            }
            tempValue.add((T)tag);
        }
        this.value = Collections.unmodifiableList(tempValue);
    }

    public Class<T> getElementType() {
        return elementType;
    }

    @Override
    public List<T> getValue() {
        // The list is already unmodifiable
        return value;
    }

    public T get(int index) {
        return value.get(index);
    }

    public int size() {
        return value.size();
    }

    @Override
    public int getID() {
        return NBTConstants.TYPE_LIST;
    }

    @Override
    protected void writePayload(DataOutput out) throws IOException {
        if (value.isEmpty()) {
            out.writeByte(NBTConstants.TYPE_END); // Default type for an empty list
        } else {
            out.writeByte(value.get(0).getID()); // Type of elements in the list
        }
        out.writeInt(value.size());
        for (T tag : value) {
            // Elements in a list are unnamed and only their payload is written.
            // The Tag.write() method handles writing the type ID and name, which is not what we want here.
            // NBT specification: A list tag contains the type of the elements and then the payload of each element.
            tag.writePayload(out);
        }
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",\n", "[\n", "\n]");
        sj.setEmptyValue("[]");
        for (T item : value) {
            // Indent sub-elements for better readability
            String itemStr = item.toString();
            sj.add(itemStr.replaceAll("\n", "\n  "));
        }
        return "TAG_List(" + (getName() == null || getName().isEmpty() ? "\"\"" : "\"" + getName() + "\"") +
               "): " + value.size() + " entries of type " +
               (value.isEmpty() ? "TAG_End" : NBTUtils.getTypeName(value.get(0).getID())) +
               "\n{" + sj.toString() + "}";
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ListTag)) return false;
        if (!super.equals(obj)) return false; // Checks name
        ListTag<?> that = (ListTag<?>) obj;

        // If both lists are empty, they are equal regardless of their declared element type.
        if (this.value.isEmpty() && that.value.isEmpty()) {
            return true;
        }
        // If one is empty and the other is not, they are not equal.
        if (this.value.isEmpty() || that.value.isEmpty()) {
            return false;
        }
        // If both are non-empty, their element types and content must match.
        return elementType.equals(that.elementType) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        // Hash code should be consistent with equals: if lists are empty, hash based on that.
        // Otherwise, include element type.
        if (value.isEmpty()) {
            return Objects.hash(super.hashCode(), NBTConstants.TYPE_END, value);
        }
        return Objects.hash(super.hashCode(), elementType, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListTag<T> clone() {
        List<T> clonedList = new ArrayList<>(value.size());
        for (T tag : value) {
            clonedList.add((T) tag.clone()); // Assuming T's clone method returns T
        }
        // Pass this.elementType directly to the constructor
        return new ListTag<>(getName(), this.elementType, clonedList);
    }
}
