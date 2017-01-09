package com.github.tinselspoon.intellij.kubernetes.config;

import java.util.Objects;

/**
 * Defines options for configuring a set of API definitions used in completion.
 */
public class ApiPackage {

    /** Whether this API package should be used during completion. */
    private boolean enabled = true;

    /** The version of the package to use, or {@code null} to use the latest version. */
    private String version = null;

    /** Serialisation constructor. */
    @SuppressWarnings("unused")
    private ApiPackage() {
    }

    /**
     * Default constructor.
     *
     * @param enabled whether this API package should be used during completion.
     * @param version the version of the package to use, or {@code null} to use the latest version.
     */
    public ApiPackage(final boolean enabled, final String version) {
        this.enabled = enabled;
        this.version = version;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApiPackage that = (ApiPackage) o;
        return enabled == that.enabled && Objects.equals(version, that.version);
    }

    /**
     * Gets whether this API package should be used during completion.
     *
     * @return the enabled.
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Gets the version of the package to use, or {@code null} to use the latest version.
     *
     * @return the version.
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, version);
    }

    /**
     * Sets whether this API package should be used during completion.
     *
     * @param enabled the new enabled.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the version of the package to use, or {@code null} to use the latest version.
     *
     * @param version the new version.
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
