package com.github.tinselspoon.intellij.kubernetes;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.List;

/**
 * Unit tests for the {@link KubernetesYamlCompletionContributor}.
 */
public class CompletionTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return TestUtil.getTestDataPath("completion/");
    }

    public void testCompletingApiVersion() {
        // GIVEN a file containing just the apiVersion field
        myFixture.configureByFiles("CompletingApiVersionInEmptyFile.yml");

        // WHEN activating completion with the caret at the apiVersion field
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN we should see a list of API versions
        assertNotNull(strings);
        assertContainsElements(strings, "v1", "batch/v1");
    }

    public void testCompletingBooleanValue() {
        // GIVEN a file containing a Pod
        myFixture.configureByFiles("CompletingBooleanValue.yml");

        // WHEN activating completion with the caret in a boolean field
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN there should be no suggestions
        assertNotNull(strings);
        assertSameElements(strings, "true", "false");
    }

    public void testCompletingInsideComment() {
        // GIVEN a file containing a Pod
        myFixture.configureByFiles("CompletingInsideComment.yml");

        // WHEN activating completion with the caret in a comment
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN there should be no suggestions
        assertNullOrEmpty(strings);
    }

    public void testCompletingKind() {
        // GIVEN a file containing just the kind field
        myFixture.configureByFiles("CompletingKindInEmptyFile.yml");

        // WHEN activating completion with the caret at the kind field
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN we should see a list of resource kinds - try a few from different API versions
        assertNotNull(strings);
        assertContainsElements(strings, "Pod", "Deployment", "Job");
    }

    public void testCompletingKindWhenApiVersionPresentFiltersTheList() {
        // GIVEN a file containing a prefilled apiVersion field
        myFixture.configureByFiles("CompletingKindWithExistingApiVersion.yml");

        // WHEN activating completion with the caret at the kind field
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN we should see resources that are in that apiVersion
        assertNotNull(strings);
        assertContainsElements(strings, "Pod", "ReplicationController");

        // AND we should not see resources in different apiVersions
        assertDoesntContain(strings, "Deployment", "Job");
    }

    public void testCompletingNestedElements() {
        // GIVEN a file containing a PersistentVolume
        myFixture.configureByFiles("CompletingNestedElements.yml");

        // WHEN activating completion with the caret under the 'status' property
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN we should see some fields appropriate to the status element
        assertNotNull(strings);
        assertContainsElements(strings, "message", "phase", "reason");
    }

    public void testCompletingRegularValue() {
        // GIVEN a file containing a Pod
        myFixture.configureByFiles("CompletingRegularValue.yml");

        // WHEN activating completion with the caret in a string field
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN there should be no suggestions
        assertNullOrEmpty(strings);
    }

    public void testCompletingTopLevelElements() {
        // GIVEN a file containing a Pod
        myFixture.configureByFiles("CompletingTopLevelElements.yml");

        // WHEN activating completion with the caret at a new property
        myFixture.completeBasic();
        final List<String> strings = myFixture.getLookupElementStrings();

        // THEN we should see a the fields appropriate to the Pod
        assertNotNull(strings);
        assertSameElements(strings, "apiVersion", "kind", "metadata", "spec", "status");
    }
}
