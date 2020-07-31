package de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;

/**
 * Using the {@link Configuration} to read a test configuration.
 */
public class TestConfiguration extends Configuration {

    private Stage stage;
    private ByteArrayOutputStream out;
    private de.uni_hildesheim.sse.javaSvnHooks.tests.Test[] tests;
    
    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from testdata/javacTest.
     * 
     * @param base the base folder for "hook.properties"
     */
    public TestConfiguration(File base) {
        this(base, Stage.POST_COMMIT); // legacy
    }
    
    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from testdata/javacTest. Stage defaults to {@link Stage#POST_COMMIT}.
     * 
     * @param base the base folder for "hook.properties"
     * @param stage the commit stage
     */
    public TestConfiguration(File base, Stage stage) {
        this(new File(base, "hook.properties"), stage, 
            (de.uni_hildesheim.sse.javaSvnHooks.tests.Test[]) null);
    }

    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file. Stage defaults to {@link Stage#POST_COMMIT}.
     * 
     * @param base the base folder for properties
     * @param fileInBase the file name in {@link #BASE}
     * @param tests instances to execute, default is taken if none given
     */
    public TestConfiguration(File base, String fileInBase, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        this(base, fileInBase, Stage.POST_COMMIT, tests);
    }

    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file.
     * 
     * @param base the base folder for properties
     * @param fileInBase the file name in {@link #BASE}
     * @param stage the commit stage
     * @param tests instances to execute, default is taken if none given
     */
    public TestConfiguration(File base, String fileInBase, Stage stage, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        this(new File(base, fileInBase), stage, tests);
    }

    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file.
     * 
     * @param properties the properties file
     * @param stage the commit stage
     * @param tests instances to execute, default is taken if none given
     */
    public TestConfiguration(File properties, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        this(properties, Stage.POST_COMMIT, tests);
    }
    
    /**
     * Creates a {@link Configuration} that will read the configuration 
     * file from a given file. Stage defaults to {@link Stage#POST_COMMIT}.
     * 
     * @param properties the properties file
     * @param stage the commit stage
     * @param tests instances to execute, default is taken if none given
     */
    public TestConfiguration(File properties, Stage stage, 
        de.uni_hildesheim.sse.javaSvnHooks.tests.Test... tests) {
        this.stage = stage;
        if (null == tests || tests.length == 0) {
            this.tests = new de.uni_hildesheim.sse.javaSvnHooks.tests.Test[] {
                new JavacTest()};
        } else {
            this.tests = tests;
        }
        readConfig(properties);
        clear();
    }
    
    @Override
    public String getUniqueIdentifier() {
        return "test_" + String.valueOf(Math.random());
    }
    
    /**
     * Returns the collected test results.
     * 
     * @return the collected test results
     */
    public String getTestResults() {
        return new String(out.toByteArray());
    }
    
    /**
     * Overrides the temporary directory, e.g., for testing.
     * 
     * @param tempDir the new temporary directory
     */
    public void setTempDirectory(File tempDir) {
        super.setTempDir(tempDir);
    }
    
    /**
     * Setter for the installation path of this hook.
     * 
     * @param hookDir The path to the installation of this hook.
     */
    public void setHookDirectory(File hookDir) {
        super.setHookDir(hookDir);
    }
    
    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
    }
    
    /**
     * Clears the temporary information in this test configuration for 
     * instance reuse.
     */
    public void clear() {
        out = new ByteArrayOutputStream();
        setTestOutputStream(new PrintStream(out));
    }

    /**
     * Returns the tests to apply.
     * 
     * @return the tests
     */
    public de.uni_hildesheim.sse.javaSvnHooks.tests.Test[] getTests() {
        return tests;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

}
