package com.github.tinselspoon.intellij.kubernetes.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of a {@link Comparator} for Kubernetes API version strings in the conventional format, e.g. {@code apps/v1beta1} etc.
 * <p>
 * The comparator has the following characteristics:
 * <ul>
 * <li>API groups (apps, extensions, settings.k8s.io etc) are compared lexicographically. A version with a group present is considered to be after a version without.</li>
 * <li>Then, the major version (v1, v2) etc is compared numerically.</li>
 * <li>Finally, the minor version or qualifier (alpha1, beta1, beta2 etc) is compared lexicographically.
 * An API version string with this part missing is considered to be greater than an API version string with this part present, as if it is missing this signifies a stable version.</li>
 * </ul>
 * <p>
 * If either string does not match the expected format for an API version, then the comparator falls back to simple lexicographic comparison.
 */
public class ApiVersionComparator implements Comparator<String> {

    /** Singleton instance. */
    public static final ApiVersionComparator INSTANCE = new ApiVersionComparator();

    /** Regex for parsing the conventional API version strings. */
    private static final Pattern VERSION_FORMAT_REGEX = Pattern.compile("(?:(?<group>.+)/)?v(?<major>\\d+)(?<minor>[A-Za-z]+\\d+)?");

    /** Private constructor for singleton instance. */
    private ApiVersionComparator() {
    }

    @Override
    public int compare(final String o1, final String o2) {
        final Matcher matcherOne = VERSION_FORMAT_REGEX.matcher(o1);
        final Matcher matcherTwo = VERSION_FORMAT_REGEX.matcher(o2);

        // Fall back to simple string comparision if either of the versions cannot be parsed
        if (!matcherOne.matches() || !matcherTwo.matches()) {
            return o1.compareTo(o2);
        }

        Comparator<Matcher> comparator = Comparator.comparing(m -> m.group("group"), Comparator.nullsFirst(Comparator.naturalOrder()));
        comparator = comparator.thenComparingInt(m -> Integer.parseInt(m.group("major")));
        comparator = comparator.thenComparing(m -> m.group("minor"), Comparator.nullsLast(Comparator.naturalOrder()));

        return comparator.compare(matcherOne, matcherTwo);
    }
}
