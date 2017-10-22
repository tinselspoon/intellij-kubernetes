package com.github.tinselspoon.intellij.kubernetes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.util.RecursionGuard;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.StubVirtualFile;

/**
 * File type descriptor representing a Kubernetes YAML file.
 */
public class KubernetesYamlFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {

    /** Singleton instance. */
    public static final KubernetesYamlFileType INSTANCE = new KubernetesYamlFileType();

    /** Number of bytes to read when guessing for the file type based on content. */
    private static final int BYTES_TO_READ = 4096;

    /** Identifier to use for the recursion guard. */
    private static final String GUARD_ID = "KubernetesYamlFileType";

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(KubernetesYamlFileType.class);

    /** Recursion guard for preventing cycles. */
    private final RecursionGuard recursionGuard = RecursionManager.createGuard(GUARD_ID);

    /** Singleton default constructor. */
    private KubernetesYamlFileType() {
        super(YAMLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "yaml";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Kubernetes Resource Definition YAML";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return SimpleIcons.FILE;
    }

    @NotNull
    @Override
    public String getName() {
        return "Kubernetes YAML";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isMyFileType(@NotNull final VirtualFile file) {
        if (file instanceof StubVirtualFile) {
            return false; // Helps New -> File get correct file type
        }

        return recursionGuard.doPreventingRecursion(GUARD_ID, true, () -> {
            if (file.isValid()) {
                final String extension = file.getExtension();
                if ("yml".equalsIgnoreCase(extension) || "yaml".equalsIgnoreCase(extension)) {
                    try (InputStream inputStream = file.getInputStream()) {
                        final byte[] bytes = new byte[BYTES_TO_READ];
                        final int n = inputStream.read(bytes, 0, BYTES_TO_READ);
                        return n > 0 && isKubernetesYaml(bytes);
                    } catch (final IOException e) {
                        logger.info("Error while determining file type.", e);
                    }
                }
            }
            return false;
        });
    }

    /**
     * Guess whether the file is a Kubernetes YAML file from a subset of the file content.
     *
     * @param bytes the bytes to check.
     * @return true if the file is a Kubernetes YAML file, otherwise, false.
     */
    private boolean isKubernetesYaml(final byte[] bytes) {
        try (Scanner scanner = new Scanner(new String(bytes, StandardCharsets.UTF_8))) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (line.startsWith("kind: ") || line.startsWith("apiVersion: ")) {
                    return true;
                }
            }
        }
        return false;
    }
}
