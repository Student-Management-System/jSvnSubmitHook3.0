package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.tests.file_name.FileNameTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Tests the {@link FileNameTest}.
 * 
 * @author Holger Eichelberger
 */
public class FileNameTestTest extends AbstractTest {

    private static final File BASE = new File("testdata/filenameTest");

    @Override
    protected File getBase() {
        return BASE;
    }

    /**
     * Tests both test folders (ok, fail) with a proper configuration including regex and message.
     */
    @Test
    public void testFilesOk() {
        TestConfiguration config = createConfig("hookFilesOk.properties", new FileNameTest());
        String res = executeTest("/filesOk", true, config);
        assertEmpty(res);

        res = executeTest("/filesNone", true, config);
        assertEmpty(res);

        res = executeTest("/filesFail", false, config);
        Assert.assertTrue("Result shall contain message from config, but: " + res, res.indexOf(XmlUtilities.xmlify(
            "File names must consist only of lower/uppercase characters, numbers and the following special "
            + "characters: +, -, $, .")) > 0);
    }

    /**
     * Tests both test folders (ok, fail) with a proper configuration including only the regex.
     */
    @Test
    public void testFilesOk2() {
        TestConfiguration config = createConfig("hookFilesOk2.properties", new FileNameTest());
        String res = executeTest("/filesOk", true, config);
        assertEmpty(res);

        res = executeTest("/filesNone", true, config);
        assertEmpty(res);

        res = executeTest("/filesFail", false, config);
        Assert.assertTrue("Result shall contain message from code, but: " + res, 
            res.indexOf(XmlUtilities.xmlify(FileNameTest.DFLT_MSG_PREFIX)) > 0);
    }

    /**
     * Tests both test folders (ok, fail) with a proper configuration without regEx / test specification.
     */
    @Test
    public void testFilesOk3() {
        TestConfiguration config = createConfig("hookFilesOk3.properties", new FileNameTest());
        String res = executeTest("/filesOk", null, config);
        assertEmpty(res);

        res = executeTest("/filesNone", null, config);
        assertEmpty(res);

        res = executeTest("/filesFail", null, config);
        assertEmpty(res);
    }

    /**
     * Tests both test folders (ok, fail) with an erroneous configuration with broken regEx.
     */
    @Test
    public void testFilesFail() {
        TestConfiguration config = createConfig("hookFilesFail.properties", new FileNameTest());
        String res = executeTest("/filesOk", false, config);
        assertRegExFail(res);

        res = executeTest("/filesNone", false, config);
        assertRegExFail(res);

        res = executeTest("/filesFail", false, config);
        assertRegExFail(res);
    }

    /**
     * Asserts that the test output contains {@link FileNameTest#REGEX_MSG_PREFIX}.
     * 
     * @param out the test output
     */
    private void assertRegExFail(String out) {
        Assert.assertTrue("Test results shall contain regEx error msg, but: " + out, 
            out.indexOf(FileNameTest.REGEX_MSG_PREFIX) > 0);
    }

}
