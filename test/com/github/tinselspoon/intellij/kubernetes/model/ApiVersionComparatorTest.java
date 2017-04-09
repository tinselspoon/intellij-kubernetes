package com.github.tinselspoon.intellij.kubernetes.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit test for the {@link ApiVersionComparator}.
 */
@RunWith(Parameterized.class)
public class ApiVersionComparatorTest {

    /** The string that is expected to be greater, i.e. later in sort order. */
    @Parameter(1)
    public String greater;

    /** The string that is expected to be lesser, i.e. earlier in sort order. */
    @Parameter
    public String lesser;

    /** Test cases. */
    @Parameters(name = "{0} should be before {1}")
    public static Collection<Object[]> params() {
        return Arrays.asList(// latest stable version
                             new Object[] { "v1", "v2" },
                             // latest stable version with two digits
                             new Object[] { "v2", "v10" },
                             // alpha version should be before a stable version
                             new Object[] { "v1alpha1", "v1" },
                             // beta version should be before a stable version
                             new Object[] { "v1beta1", "v1" },
                             // alpha should be before beta
                             new Object[] { "v1alpha1", "v1beta1" },
                             // alpha should be before beta
                             new Object[] { "v1alpha2", "v1beta1" },
                             // second iteration of beta should be before an earlier beta
                             new Object[] { "v1beta1", "v1beta2" },
                             // same minor version in a previous major version should be before a later major version
                             new Object[] { "v1beta1", "v2beta1" },
                             // beta in previous major version should be before later stable major version
                             new Object[] { "v1beta2", "v2" },
                             // above logic should work with api groups
                             new Object[] { "some.api.group/v1beta2", "some.api.group/v1" },
                             // compare different groups lexicographically
                             new Object[] { "lexicographically.earlier/v1", "lexicographically.later/v1" },
                             // version without a group should be before a version with a group
                             new Object[] { "v1", "has.a.group/v1" },
                             // fallback to simple lexicographic comparison when not in the proper format
                             new Object[] { "hello", "world" });
    }

    /** Test the comparator gives the expected result - {@code lesser} is before {@code greater}, and {@code greater} is after {@code lesser}. */
    @Test
    public void testComparisionIsCorrectAndSymmetric() {
        assertEquals("Expected '" + lesser + "' to be considered lesser than '" + greater + "'.", -1, Integer.signum(ApiVersionComparator.INSTANCE.compare(lesser, greater)));
        assertEquals("Expected '" + greater + "' to be considered greater than '" + lesser + "'.", 1, Integer.signum(ApiVersionComparator.INSTANCE.compare(greater, lesser)));
    }
}
