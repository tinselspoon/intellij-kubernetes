package com.github.tinselspoon.intellij.kubernetes;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;

/**
 * {@link FileTypeFactory} implementation for Kubernetes YAML files.
 */
public class KubernetesFileTypeFactory extends FileTypeFactory {

    @Override
    public void createFileTypes(@NotNull final FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(KubernetesYamlFileType.INSTANCE, "kubernetes");
    }
}
