package com.github.tinselspoon.intellij.kubernetes;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Static methods for tests.
 */
class TestUtil {

    /**
     * Gets the path to the given test data folder.
     *
     * @param directory the subdirectory within the test resources to search for.
     * @return the full absolute path to test data.
     */
    @NotNull
    static String getTestDataPath(final String directory) {
        try {
            final URL url = ClassLoader.getSystemClassLoader().getResource(directory);
            TestCase.assertNotNull("Unable to determine test resources directory.", url);
            return new File(url.toURI()).getAbsolutePath();
        } catch (final URISyntaxException e) {
            throw new AssertionError("Unable to determine test resources directory.", e);
        }
    }
}
