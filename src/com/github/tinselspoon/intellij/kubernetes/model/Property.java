package com.github.tinselspoon.intellij.kubernetes.model;

import com.google.gson.annotations.SerializedName;

/**
 * Schema definition of a property.
 */
public class Property {

    /** A description of this property. */
    private String description;

    /** For properties that are arrays, describes the items that may appear in the array ({@code null} otherwise). */
    private ArrayItems items;

    /** The model ID which describes the possible child properties of this property, or {@code null} if not applicable. */
    @SerializedName("$ref")
    private String ref;

    /** The type of this property. Should not be set if {@link #getRef() ref} is populated. */
    private FieldType type;

    /**
     * Gets a description of this property.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets for properties that are arrays, describes the items that may appear in the array ({@code null} otherwise).
     *
     * @return the items.
     */
    public ArrayItems getItems() {
        return items;
    }

    /**
     * Gets the model ID which describes the possible child properties of this property, or {@code null} if not applicable.
     *
     * @return the ref.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets the type of this property. Should not be set if {@link #getRef() ref} is populated.
     *
     * @return the type.
     */
    public FieldType getType() {
        return type;
    }

    /**
     * Sets a description of this property.
     *
     * @param description the new description.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets for properties that are arrays, describes the items that may appear in the array ({@code null} otherwise).
     *
     * @param items the new items.
     */
    public void setItems(final ArrayItems items) {
        this.items = items;
    }

    /**
     * Sets the model ID which describes the possible child properties of this property, or {@code null} if not applicable.
     *
     * @param ref the new ref.
     */
    public void setRef(final String ref) {
        this.ref = ref;
    }

    /**
     * Sets the type of this property. Should not be set if {@link #getRef() ref} is populated.
     *
     * @param type the new type.
     */
    public void setType(final FieldType type) {
        this.type = type;
    }
}
