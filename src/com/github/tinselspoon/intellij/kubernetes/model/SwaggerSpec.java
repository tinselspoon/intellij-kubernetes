package com.github.tinselspoon.intellij.kubernetes.model;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * Represents a complete Swagger 1.2 specification.
 */
class SwaggerSpec {

    /** The API version this specification applies to. */
    private String apiVersion;

    /** The API definitions. */
    private final List<Api> apis = new ArrayList<>();

    /** The model definitions. */
    private final Map<String, Model> models = new HashMap<>();

    public static SwaggerSpec loadFrom(final Reader reader) {
        final Gson gson = new Gson();
        return gson.fromJson(reader, SwaggerSpec.class);
    }

    /**
     * Gets the API version this specification applies to.
     *
     * @return the api version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets the API definitions.
     *
     * @return the apis.
     */
    public List<Api> getApis() {
        return apis;
    }

    /**
     * Gets the model definitions.
     *
     * @return the models.
     */
    public Map<String, Model> getModels() {
        return models;
    }

    /**
     * Sets the API version this specification applies to.
     *
     * @param apiVersion the new api version.
     */
    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
