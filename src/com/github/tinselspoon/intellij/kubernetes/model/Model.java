package com.github.tinselspoon.intellij.kubernetes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Schema definition of a model. A model can be used to describe possible child properties.
 */
public class Model {
    /** An explanation of this model. */
    private String description;

    /** A unique identifier for this model within the definition file. */
    private String id;

    /** A map of property names to property specifications that may be given within this model. */
    private final Map<String, Property> properties = new HashMap<>();

    /** A list of property names which must be present in an instance of this model. */
    @SerializedName("required")
    private final List<String> requiredProperties = new ArrayList<>();

    /**
     * Gets an explanation of this model.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets a unique identifier for this model within the definition file.
     *
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets a map of property names to property specifications that may be given within this model.
     *
     * @return the properties.
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Gets a list of property names which must be present in an instance of this model.
     *
     * @return the required properties.
     */
    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * Sets an explanation of this model.
     *
     * @param description the new description.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets a unique identifier for this model within the definition file.
     *
     * @param id the new id.
     */
    public void setId(final String id) {
        this.id = id;
    }

}
