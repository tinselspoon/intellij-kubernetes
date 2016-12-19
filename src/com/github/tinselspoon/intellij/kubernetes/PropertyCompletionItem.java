package com.github.tinselspoon.intellij.kubernetes;

import com.github.tinselspoon.intellij.kubernetes.model.Property;

/**
 * Stores a property name alongside its schema information, with the name presented in the {@link #toString()} method.
 * <p>
 * This is used in completion lists - it allows for the property model to be passed alongside the suggested property name which the documentation provider can pick up.
 *
 * @see KubernetesYamlDocumentationProvider
 * @see KubernetesYamlCompletionContributor
 */
class PropertyCompletionItem {

    /** The name of the property. */
    private final String name;

    /** The schema definition of the property. */
    private final Property property;

    /**
     * Default constructor.
     *
     * @param name the name of the property.
     * @param property the schema definition of the property.
     */
    PropertyCompletionItem(final String name, final Property property) {
        this.name = name;
        this.property = property;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the name of the property.
     *
     * @return the name.
     */
    String getName() {
        return name;
    }

    /**
     * Gets the schema definition of the property.
     *
     * @return the property.
     */
    Property getProperty() {
        return property;
    }
}
