package de.uni_hildesheim.sse.javaSvnHooks.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.util.MessageUtils;

/**
 * Abstract base class for all tests.
 * 
 * @author Adam Krafczyk
 */
public abstract class Test {

    private static final List<Test> ALL_TESTS = new ArrayList<Test>();
    
    /**
     * Registers a test.
     * 
     * @param test The {@link Test} to be registered.
     */
    protected static void registerTest(Test test) {
        ALL_TESTS.add(test);
    }
    
    /**
     * Getter for a {@link List} of all tests.
     * 
     * @return An unmodifiable {@link List} of all registered tests.
     */
    public static List<Test> getAllTests() {
        return Collections.unmodifiableList(ALL_TESTS);
    }
    
    /**
     * Getter for the name of this test.
     * 
     * @return The name or description of this test.
     */
    public abstract String getName();
    
    /**
     * Getter for the dependency of this test. Multiple dependencies are not supported.
     * 
     * @param stage the actual commit stage
     * @return The dependency of this test.
     */
    public abstract Class<? extends Test> dependsOn(Configuration.Stage stage);
    
    /**
     * Whether this test should be run in a pre-commit hook.
     * 
     * @param pathConfiguration The {@link PathConfiguration} in which the test should run.
     * 
     * @return <code>true</code> if this test should be run in a pre-commit hook.
     */
    public abstract boolean runInPreCommit(PathConfiguration pathConfiguration);
    
    /**
     * Whether this test should be run in a post-commit hook.
     * 
     * @param pathConfiguration The {@link PathConfiguration} in which the test should run.
     * 
     * @return <code>true</code> if this test should be run in a post-commit hook.
     */
    public abstract boolean runInPostCommit(PathConfiguration pathConfiguration);
    
    /**
     * Runs this test on the given {@link PathConfiguration}.
     * 
     * @param pathConfiguration The {@link PathConfiguration} for the files to
     * run this test on.
     * 
     * @return The exit code of this test. 0 means test passed, anything else
     * means test not passed.
     */
    public abstract int execute(PathConfiguration pathConfiguration);

    // checkstyle: stop parameter number check
    
    /**
     * Records a message.
     * 
     * @param config the actual configuration
     * @param tool the issuing tool name
     * @param type the message type
     * @param file the file (may be empty)
     * @param line the line in file (may be empty)
     * @param message the message
     */
    public void message(Configuration config, String tool, String type, String file, String line, String message) {
        String msg = MessageUtils.composeMessage(config, tool, type, file, line, message);
        config.getTestOutputStream().println(msg);
    }

    // checkstyle: resume parameter number check

    /**
     * Records an error message.
     * 
     * @param config the actual configuration
     * @param tool the issuing tool name
     * @param file the file (may be empty)
     * @param line the line in file (may be empty)
     * @param message the message
     */
    public void error(Configuration config, String tool, String file, String line, String message) {
        message(config, tool, MessageUtils.TYPE_ERROR, file, line, message);
    }

    /**
     * Records an error message.
     * 
     * @param config the actual configuration
     * @param tool the issuing tool name
     * @param file the file (may be empty)
     * @param message the message
     */
    public void error(Configuration config, String tool, String file, String message) {
        message(config, tool, MessageUtils.TYPE_ERROR, file, "", message);
    }

    /**
     * Records an error message.
     * 
     * @param config the actual configuration
     * @param tool the issuing tool name
     * @param message the message
     */
    public void error(Configuration config, String tool, String message) {
        message(config, tool, MessageUtils.TYPE_ERROR, "", "", message);
    }

}
