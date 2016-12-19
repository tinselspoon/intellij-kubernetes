package com.github.tinselspoon.intellij.kubernetes;

import org.jetbrains.annotations.NotNull;

import com.github.tinselspoon.intellij.kubernetes.model.ArrayItems;
import com.github.tinselspoon.intellij.kubernetes.model.FieldType;
import com.github.tinselspoon.intellij.kubernetes.model.Property;

/**
 * Utilities for working with model classes.
 */
public final class ModelUtil {

    /** Static class private constructor. */
    private ModelUtil() {
    }

    /**
     * Get a string that describes the type of a particular property.
     * <p>
     * If the property is a basic type (string, integer etc) then the type name is returned. If the property type refers to a model, the model name is returned. If the property is an array, then the
     * process described previously is carried out for the array item type, and the string "[]" is appended to the end.
     *
     * @param propertySpec the property to obtain the type string for.
     * @return the type string.
     */
    @NotNull
    public static String typeStringFor(@NotNull final Property propertySpec) {
        final String typeText;
        final ArrayItems items = propertySpec.getItems();
        if (propertySpec.getType() == FieldType.ARRAY && items != null) {
            typeText = (items.getRef() == null ? items.getType().toString() : items.getRef()) + "[]";
        } else {
            typeText = propertySpec.getRef() == null ? propertySpec.getType().toString() : propertySpec.getRef();
        }
        return typeText;
    }
}
