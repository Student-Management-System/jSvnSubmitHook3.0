package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Tests the {@link FileSizeTest}.
 * 
 * @author Holger Eichelberger
 */
public class FileSizeTestTest extends AbstractTest {

    private static final File BASE = new File("testdata/filesizeTest");

    @Override
    protected File getBase() {
        return BASE;
    }

    /**
     * Tests both test folders (ok, fail) with 10/100 configuration.
     */
    @Test
    public void testFilesOk() {
        TestConfiguration config = createConfig("hookFilesOk.properties", new FileSizeTest());
        String res = executeTest("/filesOk", true, config);
        assertEmpty(res);

        res = executeTest("/filesFail", false, config);
        assertFailingAll(res);
    }

    /**
     * Tests both test folders (ok, fail) with no configuration.
     */
    @Test
    public void testFilesOk2() {
        TestConfiguration config = createConfig("hookFilesOk2.properties", new FileSizeTest());
        String res = executeTest("/filesOk", true, config);
        assertEmpty(res);

        res = executeTest("/filesFail", true, config);
        assertEmpty(res);
    }

    /**
     * Tests both test folders (ok, fail) with 8/50 configuration..
     */
    @Test
    public void testFilesFail() {
        TestConfiguration config = createConfig("hookFilesFail.properties", new FileSizeTest());
        String res = executeTest("/filesOk", false, config);
        assertFailingAll(res);

        res = executeTest("/filesFail", false, config);
        assertFailingAll(res);
    }
    
    /**
     * Asserts that files and project size fail.
     * 
     * @param res the result to check
     */
    private static void assertFailingAll(String res) {
        assertFailingFiles(res);
        assertFailingProject(res);
    }

    /**
     * Asserts that file size fail.
     * 
     * @param res the result to check
     */
    private static void assertFailingFiles(String res) {
        Assert.assertTrue("Result shall contain message from config, but: " + res, res.indexOf(XmlUtilities.xmlify(
            "The following files are rejected because they exceed the maximum file size")) > 0);
    }

    /**
     * Asserts that project size fail.
     * 
     * @param res the result to check
     */
    private static void assertFailingProject(String res) {
        Assert.assertTrue("Result shall contain message from config, but: " + res, res.indexOf(XmlUtilities.xmlify(
            "The project exceeds the maximum size.")) > 0);
    }

}
