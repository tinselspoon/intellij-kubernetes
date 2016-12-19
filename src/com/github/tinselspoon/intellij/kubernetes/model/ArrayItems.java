package com.github.tinselspoon.intellij.kubernetes.model;

import com.google.gson.annotations.SerializedName;

/**
 * Describes the items that may appear as elements of an array property.
 */
public class ArrayItems {

    /** A reference to a model ID which the items conform to. Either this property or {@link #getType() type} should be set. */
    @SerializedName("$ref")
    private String ref;

    /** The type of the items. Either this property or {@link #getRef() ref} should be set. */
    private FieldType type;

    /**
     * Gets a reference to a model ID which the items conform to. Either this property or {@link #getType() type} should be set.
     *
     * @return the ref.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets the type of the items. Either this property or {@link #getRef() ref} should be set.
     *
     * @return the type.
     */
    public FieldType getType() {
        return type;
    }

    /**
     * Sets a reference to a model ID which the items conform to. Either this property or {@link #getType() type} should be set.
     *
     * @param ref the new ref.
     */
    public void setRef(final String ref) {
        this.ref = ref;
    }

    /**
     * Sets the type of the items. Either this property or {@link #getRef() ref} should be set.
     *
     * @param type the new type.
     */
    public void setType(final FieldType type) {
        this.type = type;
    }
}
