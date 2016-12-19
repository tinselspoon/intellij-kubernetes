package com.github.tinselspoon.intellij.kubernetes.model;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the data type of a particular property's content.
 */
public enum FieldType {

    /** An integer property data type. */
    @SerializedName("integer")
    INTEGER,

    /** A number property data type (i.e. may have a decimal point). */
    @SerializedName("number")
    NUMBER,

    /** A string property data type. */
    @SerializedName("string")
    STRING,

    /** A boolean property data type. */
    @SerializedName("boolean")
    BOOLEAN,

    /** An array property data type. */
    @SerializedName("array")
    ARRAY,

    /** An unstructured object property data type. */
    @SerializedName("object")
    OBJECT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
