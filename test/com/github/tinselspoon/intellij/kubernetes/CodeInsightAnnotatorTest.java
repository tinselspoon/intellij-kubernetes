package com.github.tinselspoon.intellij.kubernetes;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Tests for the {@link com.github.tinselspoon.intellij.kubernetes.codeInsight} annotators.
 */
public class CodeInsightAnnotatorTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return TestUtil.getTestDataPath("codeInsight/");
    }

    public void testNoAnnotationsOnValidFile() {
        myFixture.configureByFile("ValidFile.yml");
        myFixture.checkHighlighting();
    }

    public void testDuplicatedProperty() {
        myFixture.configureByFile("DuplicatedProperty.yml");
        myFixture.checkHighlighting();
    }

    public void testDataTypes() {
        myFixture.configureByFile("DataTypes.yml");
        myFixture.checkHighlighting();
    }

    public void testPropertyNotInModel() {
        myFixture.configureByFile("PropertyNotInModel.yml");
        myFixture.checkHighlighting();
    }
}
