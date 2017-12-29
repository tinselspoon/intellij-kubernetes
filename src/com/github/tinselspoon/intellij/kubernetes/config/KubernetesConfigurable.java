package com.github.tinselspoon.intellij.kubernetes.config;

import java.util.Objects;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

/**
 * Defines the configuration UI for the plugin in the IntelliJ settings window.
 */
public class KubernetesConfigurable implements Configurable {

    /** The list item text that represents that the latest version of an API should be used. */
    private static final String LATEST_API_VERSION_ITEM = "<Latest API Version>";

    /** The underlying configuration being edited. */
    private final ConfigState configState = ServiceManager.getService(ConfigState.class);

    /** Checkbox to enable native Kubernetes completion. */
    private JCheckBox enableKubernetesCompletion;

    /** Checkbox to enable OpenShift completion. */
    private JCheckBox enableOpenshiftCompletion;

    /** List of available Kubernetes versions. */
    private JComboBox<String> kubernetesVersions;

    /** List of available OpenShift versions. */
    private JComboBox<String> openshiftVersions;

    /** Overall container. */
    private JPanel panel;

    /**
     * Create an {@link ApiPackage} object from the "enable completion" and "version" UI elements.
     *
     * @param enableCompletion the enable completion check box.
     * @param versions the version combo box selector.
     * @return an {@code ApiPackage} configured from the UI.
     */
    @NotNull
    private static ApiPackage makeApiPackage(final JCheckBox enableCompletion, final JComboBox<String> versions) {
        final ApiPackage kubernetesPackage = new ApiPackage(enableCompletion.isSelected(), null);
        if (!Objects.equals(versions.getSelectedItem(), LATEST_API_VERSION_ITEM)) {
            kubernetesPackage.setVersion((String) versions.getSelectedItem());
        }
        return kubernetesPackage;
    }

    @Override
    public void apply() throws ConfigurationException {
        populateConfigState(configState);
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        enableKubernetesCompletion.addActionListener(e -> updateEnabled());
        enableOpenshiftCompletion.addActionListener(e -> updateEnabled());
        kubernetesVersions.addItem(LATEST_API_VERSION_ITEM);
        kubernetesVersions.addItem("1.9");
        kubernetesVersions.addItem("1.8");
        kubernetesVersions.addItem("1.7");
        kubernetesVersions.addItem("1.6");
        kubernetesVersions.addItem("1.5");
        kubernetesVersions.addItem("1.4");
        kubernetesVersions.addItem("1.3");
        kubernetesVersions.addItem("1.2");
        openshiftVersions.addItem(LATEST_API_VERSION_ITEM);
        openshiftVersions.addItem("3.6");
        openshiftVersions.addItem("1.5");
        openshiftVersions.addItem("1.4");
        openshiftVersions.addItem("1.3");
        openshiftVersions.addItem("1.2");
        reset();
        return panel;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Kubernetes and OpenShift";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public boolean isModified() {
        final ConfigState effectiveConfigState = new ConfigState();
        populateConfigState(effectiveConfigState);
        return !effectiveConfigState.equals(configState);
    }

    @Override
    public void reset() {
        // Substitute defaults if not yet set
        final boolean enableKubernetes = Optional.of(configState.getKubernetesPackage()).map(ApiPackage::getEnabled).orElse(true);
        final boolean enableOpenshift = Optional.of(configState.getOpenshiftPackage()).map(ApiPackage::getEnabled).orElse(false);
        final String kubernetesVersion = Optional.of(configState.getKubernetesPackage()).map(ApiPackage::getVersion).orElse(LATEST_API_VERSION_ITEM);
        final String openshiftVersion = Optional.of(configState.getOpenshiftPackage()).map(ApiPackage::getVersion).orElse(LATEST_API_VERSION_ITEM);
        enableKubernetesCompletion.setSelected(enableKubernetes);
        enableOpenshiftCompletion.setSelected(enableOpenshift);
        kubernetesVersions.setSelectedItem(kubernetesVersion);
        openshiftVersions.setSelectedItem(openshiftVersion);
        updateEnabled();
    }

    /**
     * Modify a given {@link ConfigState} to reflect the options selected in the UI.
     *
     * @param configState the {@code ConfigState} to modify.
     */
    private void populateConfigState(final ConfigState configState) {
        configState.setKubernetesPackage(makeApiPackage(enableKubernetesCompletion, kubernetesVersions));
        configState.setOpenshiftPackage(makeApiPackage(enableOpenshiftCompletion, openshiftVersions));
    }

    /** Update the state of the UI from the checkboxes. */
    private void updateEnabled() {
        kubernetesVersions.setEnabled(enableKubernetesCompletion.isSelected());
        openshiftVersions.setEnabled(enableOpenshiftCompletion.isSelected());
    }
}
