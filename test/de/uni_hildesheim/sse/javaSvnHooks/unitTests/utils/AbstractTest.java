package de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;

/**
 * An abstract test with some utility/convenience functions.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractTest {
    
    private static final boolean DEBUG = false;
    
    static {
        if (!DEBUG) {
            Logger.INSTANCE.setOutputStream(
                new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM));
        }
    }
    
    /**
     * Returns the base folder containing the tests.
     * 
     * @return the base folder
     */
    protected abstract File getBase();
    
    /**
     * Returns the path configuration with the given <code>name</code>.
     * 
     * @param name the name
     * @param config the configuration to use
     * @return the path configuration or <b>null</b>
     */
    protected static PathConfiguration findPathConfiguration(String name, TestConfiguration config) {
        PathConfiguration result = null;
        Iterator<PathConfiguration> iter = config.pathConfigurations();
        while (null == result && iter.hasNext()) {
            PathConfiguration pConfig = iter.next();
            if (name.equals(pConfig.getName())) {
                result = pConfig;
            }
        }
        return result;
    }

    /**
     * Executes a test. Folder must be registered as path configuration in
     * configuration. Clears the output collected in configuration.
     * 
     * @param name name of the configuration/folder in javacTest
     * @param expectedOk whether the execution shall be successful or not
     * @return the messages produced by the test, either empty or some XML
     */
    protected String executeTest(String name, boolean expectedOk) {
        return executeTest(name, expectedOk, new TestConfiguration(getBase()));
    }

    /**
     * Executes a test. Folder must be registered as path configuration in
     * the actual configuration.
     * 
     * @param name name of the configuration/folder in javacTest
     * @param expectedOk whether the execution shall be successful or not, may be <b>null</b> for neither 
     *     success/fail detected
     * @param config the configuration to use
     * @return the messages produced by the test, either empty or some XML
     */
    protected String executeTest(String name, Boolean expectedOk, TestConfiguration config) {
        PathConfiguration pathConf = findPathConfiguration(name, config);
        Assert.assertNotNull(pathConf);
        pathConf.setWorkingDir(new File(getBase(), name).getAbsoluteFile());
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test[] tests = config.getTests();
        int okCount = 0;
        int failCount = 0;
        for (int t = 0; t < tests.length; t++) {
            if (tests[t].runInPreCommit(pathConf) || tests[t].runInPostCommit(pathConf)) {
                int status = tests[t].execute(pathConf);
                System.out.println(tests[t].getName() + " " + status);
                if (0 == status) {
                    okCount++;
                } else {
                    failCount++;
                }
            }
        }
        String res = config.getTestResults();
        if (DEBUG) {
            System.out.println("OK: " + okCount + " FAIL: " + failCount);
            System.out.println(res);
        }
        if (null == expectedOk) {
            Assert.assertEquals(0, okCount);
            Assert.assertEquals(0, failCount);
        } else if (expectedOk) {
            Assert.assertNotEquals(0, okCount);
            Assert.assertEquals(0, failCount);
        } else {
            Assert.assertNotEquals(0, failCount);
        }
        config.clear();
        return res;
    }

    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file.
     * 
     * @return the test configuration
     */
    protected TestConfiguration createConfig() {
        return createConfig("hook.properties", (de.uni_hildesheim.sse.javaSvnHooks.tests.Test[]) null);
    }
    
    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file. Defaults to 
     * {@link Configuration.Stage#POST_COMMIT}.
     * 
     * @param fileInBase the file name in {@link #getBase()}
     * @param tests instances to execute, default is taken if none given
     * @return the test configuration
     */
    protected TestConfiguration createConfig(String fileInBase, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        return new TestConfiguration(new File(getBase(), fileInBase), tests);
    }

    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file.
     * 
     * @param fileInBase the file name in {@link #getBase()}
     * @param stage the commit stage
     * @param tests instances to execute, default is taken if none given
     * @return the test configuration
     */
    protected TestConfiguration createConfig(String fileInBase, 
        Configuration.Stage stage, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        return new TestConfiguration(new File(getBase(), fileInBase), 
            stage, tests);
    }

    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file. Defaults to 
     * {@link Configuration.Stage#POST_COMMIT}.
     * 
     * @param file the configuration file 
     * @param tests instances to execute, default is taken if none given
     * @return the test configuration
     */
    protected TestConfiguration createConfig(File file, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        return new TestConfiguration(file, tests);
    }

    /**
     * Asserts that {@code res} is empty.
     * 
     * @param res the result to be checked
     */
    protected static void assertEmpty(String res) {
        Assert.assertEquals("Result shall be empty, but: " + res, "", res);
    }
    
    /**
     * Returns whether this is the CI machine.
     * 
     * @return <code>true</code> for Jenkins, <code>false</code> else
     */
    protected static boolean isJenkins() {
        boolean isJenkins;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            isJenkins = inetAddress.getHostName().contains("jenkins");
        } catch (UnknownHostException e) {
            isJenkins = false;
        }
        return isJenkins;
    }

}
