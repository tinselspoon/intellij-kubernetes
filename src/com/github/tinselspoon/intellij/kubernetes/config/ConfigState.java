package com.github.tinselspoon.intellij.kubernetes.config;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * Persistent settings for the plugin.
 */
@State(name = "KubernetesSettings", storages = @Storage(value = "kubernetes.resources.settings.xml"))
public class ConfigState implements PersistentStateComponent<ConfigState> {

    /** The configuration for Kubernetes. */
    private ApiPackage kubernetesPackage = new ApiPackage(true, null);

    /** The configuration for OpenShift. */
    private ApiPackage openshiftPackage = new ApiPackage(false, null);

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ConfigState that = (ConfigState) o;
        return Objects.equals(kubernetesPackage, that.kubernetesPackage) && Objects.equals(openshiftPackage, that.openshiftPackage);
    }

    /**
     * Gets the configuration for Kubernetes.
     *
     * @return the kubernetes package.
     */
    public ApiPackage getKubernetesPackage() {
        return kubernetesPackage;
    }

    /**
     * Gets the configuration for OpenShift.
     *
     * @return the openshift package.
     */
    public ApiPackage getOpenshiftPackage() {
        return openshiftPackage;
    }

    @Nullable
    @Override
    public ConfigState getState() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kubernetesPackage, openshiftPackage);
    }

    @Override
    public void loadState(final ConfigState configState) {
        XmlSerializerUtil.copyBean(configState, this);
    }

    /**
     * Sets the configuration for Kubernetes.
     *
     * @param kubernetesPackage the new kubernetes package.
     */
    public void setKubernetesPackage(final ApiPackage kubernetesPackage) {
        this.kubernetesPackage = kubernetesPackage;
    }

    /**
     * Sets the configuration for OpenShift.
     *
     * @param openshiftPackage the new openshift package.
     */
    public void setOpenshiftPackage(final ApiPackage openshiftPackage) {
        this.openshiftPackage = openshiftPackage;
    }
}
