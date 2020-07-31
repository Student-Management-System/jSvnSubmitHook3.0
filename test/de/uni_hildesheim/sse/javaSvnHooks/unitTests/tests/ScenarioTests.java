package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.checkstyle.CheckstyleTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_name.FileNameTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;

/**
 * Tests combinations of tests.
 * 
 * @author Holger Eichelberger
 */
public class ScenarioTests extends AbstractTest {

    private static final File BASE = new File("testdata/scenarioTest");

    @Override
    protected File getBase() {
        return BASE;
    }
    
    // NOTE:
    // checks or checkstyle pre-config may have to be adjusted when checkstyle
    // is somewhen able to parse identifiers with umlauts according to Java Spec

    /**
     * Tests for umlauts in identifiers in Java files.
     */
    @Test
    public void testUmlauts1() {
        // pre-commit
        TestConfiguration config = createConfig("hook.properties", 
            Configuration.Stage.PRE_COMMIT,
            new FileNameTest(), new CheckstyleTest());
        String res = executeTest("/umlauts1", false, config);
        // Checkstyle still fails with exception when umlauts are in identifiers
        // different pre-commit config does not make a difference here
        Assert.assertTrue(
            res.indexOf("message=\"Unexpected character") > 0);

        // post-commit
        config = createConfig("hook.properties", 
            Configuration.Stage.POST_COMMIT,
            new CheckstyleTest());
            res = executeTest("/umlauts1", false, config);
        // Checkstyle still fails with exception when umlauts are in identifiers
        // different pre-commit config does not make a difference here
        Assert.assertTrue(
            res.indexOf("message=\"Unexpected character") > 0);
    }

    /**
     * Tests for umlauts in class/file name.
     */
    @Test
    public void testUmlauts2() {
        // pre-commit
        TestConfiguration config = createConfig("hook.properties", 
            Configuration.Stage.PRE_COMMIT,
            new FileNameTest(), new CheckstyleTest());
        String res = executeTest("/umlauts2", false, config);
        Assert.assertTrue(
            res.indexOf("message=\"File names must consist only of") > 0);
        // Checkstyle still fails with exception when umlauts are in identifiers
        // different pre-commit config does not make a difference here
        if (!isJenkins()) { // does not occur on Linux
            Assert.assertTrue(
                res.indexOf("message=\"Unexpected character") > 0);
        }

        // post-commit
        config = createConfig("hook.properties", 
            Configuration.Stage.POST_COMMIT,
            new CheckstyleTest());
        res = executeTest("/umlauts2", false, config);
        // Checkstyle still fails with exception when umlauts are in identifiers
        if (!isJenkins()) { // does not occur on Linux
            Assert.assertTrue(
                res.indexOf("message=\"Unexpected character") > 0);
        }
    }

    /**
     * Tests for umlauts in comment/string in Java file name.
     */
    @Test
    public void testUmlauts3() {
        // pre-commit
        TestConfiguration config = createConfig("hook.properties", 
            Configuration.Stage.PRE_COMMIT,
            new FileNameTest(), new CheckstyleTest());
        String res = executeTest("/umlauts3", true, config);
        // don't restrict strings or comments for now
        Assert.assertEquals("", res);

        // post-commit
        config = createConfig("hook.properties", 
            Configuration.Stage.POST_COMMIT,
            new CheckstyleTest());
        res = executeTest("/umlauts3", true, config);
        // don't restrict strings or comments for now
        Assert.assertEquals("", res);
    }

    /**
     * Tests for no umlauts (counter-test for {@link #testUmlauts1()} etc.).
     */
    @Test
    public void testUmlautsOk() {
        // pre-commit
        TestConfiguration config = createConfig("hook.properties", 
            Configuration.Stage.PRE_COMMIT,
            new FileNameTest(), new CheckstyleTest());
        String res = executeTest("/umlautsOk", true, config);
        Assert.assertEquals("", res);

        // post-commit
        config = createConfig("hook.properties", 
            Configuration.Stage.POST_COMMIT,
            new CheckstyleTest());
        res = executeTest("/umlautsOk", true, config);
        Assert.assertEquals("", res);
    }

}
