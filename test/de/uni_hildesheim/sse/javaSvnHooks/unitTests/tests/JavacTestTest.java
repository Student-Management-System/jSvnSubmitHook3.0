package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.tests.checkstyle.CheckstyleTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config.EclipseConfigurationTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javadoc.JavadocTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;

/**
 * Tests the {@link JavacTest}.
 * 
 * @author Holger Eichelberger
 */
public class JavacTestTest extends AbstractTest {

    private static final File BASE = new File("testdata/javacTest");

    @Override
    protected File getBase() {
        return BASE;
    }
    
    /**
     * Tests the output for incomplete files.
     */
    @Test
    public void testIncompleteJava() {
        String res = executeTest("/incompleteJava", false);
        assertTextsContain(res, "message=\"error: reached end of file while parsing\"");
    }

    /**
     * Tests the output for complete files.
     */
    @Test
    public void testCompleteJava() {
        String res = executeTest("/completeJava", true);
        assertTextsContain(res);
    }

    /**
     * Tests the output for a src/bin project.
     */
    @Test
    public void testSrcBin() {
        String properties = "hookSrcBin.properties";
        String version = System.getProperty("java.version", "");
        // -html... is required in tests
        if (version.startsWith("10.")) {
            properties = "hookSrcBin10.properties";
        }
        TestConfiguration config = createConfig(properties, 
            new EclipseConfigurationTest(), new JavacTest(), new JavadocTest());
        String res = executeTest("/srcBinJava", true, config);
        Assert.assertEquals("Result shall be empty, but: " + res, "", res);
        
        // cleanup javadoc
        File base = new File(BASE, "srcBinJava");
        File[] files = base.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.isDirectory() && f.getName().startsWith("doc_test_")) {
                    FileUtils.deleteQuietly(f);
                }
            }
        }
    }

    /**
     * Tests checkstyle execution.
     */
    @Test
    public void testCheckstyle() {
        TestConfiguration config = createConfig("hookCheckstyle.properties", new JavacTest(), new CheckstyleTest());
        String res;
        res = executeTest("/incompleteJavaCheckstyleBeginners", false, config);
        assertTextsContain(res, "has incorrect indentation level", "must match pattern");
        res = executeTest("/incompleteJavaCheckstyleOO", false, config);
        assertTextsContain(res, "has incorrect indentation level", "must match pattern", "definition in wrong order");
        res = executeTest("/incompleteJavaCheckstyleDoc", false, config);
        assertTextsContain(res, "has incorrect indentation level", "must match pattern", "definition in wrong order", 
            "Javadoc has empty description section", "Missing a Javadoc comment");

        res = executeTest("/completeJavaCheckstyleBeginners", true, config);
        assertTextsContain(res);
        res = executeTest("/completeJavaCheckstyleOO", true, config);
        assertTextsContain(res);
        res = executeTest("/completeJavaCheckstyleDoc", true, config);
        assertTextsContain(res);
    }
    
    /**
     * Either asserts that <code>text</code> is empty or contains all given expected substrings.
     * 
     * @param text the text to check for
     * @param expected the subtrings that must be part of <code>text</code>
     */
    private static void assertTextsContain(String text, String... expected) {
        if (0 == expected.length) {
            Assert.assertEquals("Result shall be empty, but: " + text, "", text);
        } else {
            for (String e : expected) {
                Assert.assertTrue("Result shall contain: " + e, text.indexOf(e) >= 0);
            }
        }
    }

}
