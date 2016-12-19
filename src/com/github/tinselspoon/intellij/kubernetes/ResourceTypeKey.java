package com.github.tinselspoon.intellij.kubernetes;

import java.util.Objects;

/**
 * Uniquely identifies the type of a resource in a Kubernetes definition. This is accomplished by the fields "apiVersion" and "kind", which are included in every Kubernetes resource.
 */
public class ResourceTypeKey {

    /** The value of the "apiVersion" field, e.g. {@code batch/v1}. */
    private final String apiVersion;

    /** The value of the "kind" field, e.g. {@code Job}. */
    private final String kind;

    /**
     * Default constructor.
     *
     * @param apiVersion the value of the "apiVersion" field, e.g. {@code batch/v1}.
     * @param kind the value of the "kind" field, e.g. {@code Job}.
     */
    public ResourceTypeKey(final String apiVersion, final String kind) {
        this.apiVersion = apiVersion;
        this.kind = kind;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResourceTypeKey that = (ResourceTypeKey) o;
        return Objects.equals(apiVersion, that.apiVersion) && Objects.equals(kind, that.kind);
    }

    /**
     * Gets the value of the "apiVersion" field, e.g. {@code batch/v1}.
     *
     * @return the api version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets the value of the "kind" field, e.g. {@code Job}.
     *
     * @return the kind.
     */
    public String getKind() {
        return kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, kind);
    }
}
