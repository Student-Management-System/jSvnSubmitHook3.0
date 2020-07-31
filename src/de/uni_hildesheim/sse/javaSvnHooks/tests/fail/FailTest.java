package de.uni_hildesheim.sse.javaSvnHooks.tests.fail;

import java.io.PrintStream;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;

/**
 * Implements a test that will always fail / reject a commit.
 * This test is only intended for testing purposes.
 * 
 * @author Adam Krafczyk
 */
public class FailTest extends Test {

    @Override
    public String getName() {
        return "This always fails";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return null;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("fail_pre", false);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("fail_post", false);
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        Configuration config = pathConfiguration.getGlobalConfiguration();
        PrintStream out = config.getTestOutputStream();
        if (config.produceXmlOutput()) {
            out.print("<message tool=\"fail\" type=\"error\" file=\"\" line=\"\" ");
            out.println(" message=\"Rejecting commit for debugging purposes\"></message>");
        } else {
            out.println("Rejecting commit for debugging purposes");
        }
        return 1;
    }
    
    static {
        registerTest(new FailTest());
    }

}
