package de.uni_hildesheim.sse.javaSvnHooks;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;
import de.uni_hildesheim.sse.javaSvnHooks.util.ResultOutputStream;

/**
 * Abstract superclass for all commit hooks.
 * 
 * @author Adam Krafczyk
 */
public abstract class CommitHook {

    /**
     * Checks or creates tmpDir and checkoutDir according to the configuration.
     * 
     * @param configuration The configuration that specifies checkout and tmpDir.
     * 
     * @return EXIT_SUCCESS or EXIT_ERROR.
     */
    protected int createDirecotries(Configuration configuration) {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        // Create tempDir if it's not a directory
        if (exitCode == ExitCodes.EXIT_SUCCESS && !configuration.getTempDir().isDirectory()) {
            boolean created = configuration.getTempDir().mkdirs();
            if (!created) {
                Logger.INSTANCE.log("Can't create tempdir");
                exitCode = ExitCodes.EXIT_ERROR;
            }
        }
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            Logger.INSTANCE.log("tempDir is " + configuration.getTempDir());
            
            // Create checkoutDir inside tempDir
            if (!configuration.getCheckoutDir().exists()) {
                boolean created = configuration.getCheckoutDir().mkdirs();
                if (!created) {
                    Logger.INSTANCE.log("Can't create checkoutDir");
                    exitCode = ExitCodes.EXIT_ERROR;
                } else {
                    Logger.INSTANCE.log("checkoutDir is " + configuration.getCheckoutDir());
                }
            } else {
                Logger.INSTANCE.log("checkoutDir (" + configuration.getCheckoutDir()
                        + ") already exists in tempDir");
                exitCode = ExitCodes.EXIT_ERROR;
            }
        }
        
        return exitCode;
    }
    
    /**
     * Attempts to delete the checkoutDir specified by the {@link Configuration}.
     * 
     * @param configuration The configuration.
     */
    protected void deleteCheckoutDir(Configuration configuration) {
        try {
            FileUtils.deleteDirectory(configuration.getCheckoutDir());
        } catch (IOException e) {
            Logger.INSTANCE.logException(e, false);
        }
    }
    
    /**
     * Checks whether the given {@link Test} is enabled for the given {@link PathConfiguration}.
     * 
     * @param pathConfiguration The {@link PathConfiguration} for the test.
     * @param test The {@link Test} that might be enabled.
     * @return <code>true</code> if the {@link Test} is enabled.
     */
    protected abstract boolean isTestEnabled(PathConfiguration pathConfiguration, Test test);
    
    /**
     * Calls the {@link Test#execute(PathConfiguration)} method for all tests.
     * 
     * @param pathConfiguration The {@link PathConfiguration} to pass to the tests.
     * 
     * @return {@link ExitCodes#EXIT_SUCCESS} when all tests succeed;
     *  {@link ExitCodes#EXIT_FAIL} when one or more tests fail.
     */
    private int runTests(PathConfiguration pathConfiguration) {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        Set<Test> tests = new HashSet<Test>(Test.getAllTests());
        Set<Test> missingDependencyTests = new HashSet<Test>();
        Set<Class<? extends Test>> executedTests = new HashSet<Class<? extends Test>>();
        
        int lastSize;
        
        do {
            lastSize = tests.size();
            
            Iterator<Test> iter = tests.iterator();
            Configuration.Stage stage = pathConfiguration.getGlobalConfiguration().getStage();
            while (iter.hasNext()) {
                Test test = iter.next();
                
                boolean dependencySatisfied = true;
                Class<? extends Test> dep = test.dependsOn(stage);
                if (dep != null) {
                    dependencySatisfied = executedTests.contains(dep);
                }
                
                boolean enabled = isTestEnabled(pathConfiguration, test);
                //Logger.INSTANCE.log("Test " + test.getName() + " dependencies " 
                //    + dependencySatisfied + " enabled " + enabled);
                if (dependencySatisfied && enabled) {
                    //Christopher(?): exitCode = ExitCodes.EXIT_FAIL;
                    missingDependencyTests.remove(test);
                    iter.remove();
                    int result = test.execute(pathConfiguration);
                    if (result != 0) {
                        exitCode = ExitCodes.EXIT_FAIL;
                    } else {
                        executedTests.add(test.getClass());
                    }
                    Logger.INSTANCE.log("Result from \"" + test.getName() + "\": " + result);
                } else if (enabled && !dependencySatisfied) {
                    missingDependencyTests.add(test);
                }
            }
            
        } while (tests.size() != lastSize);
        
        if (missingDependencyTests.size() > 0) {
            StringBuilder message = new StringBuilder("Couldn't run following tests due to not "
                + "fulfilled test dependencies: ");
            for (Iterator<Test> iter = missingDependencyTests.iterator(); iter.hasNext();) {
                message.append(iter.next().getName());
                if (iter.hasNext()) {
                    message.append(", ");
                }
            }
            Logger.INSTANCE.log(message.toString());
        }
        
        return exitCode;
    }
    
    /**
     * Runs all tests for this transaction.
     * 
     * @param configuration The {@link Configuration} object.
     * 
     * @return {@link ExitCodes#EXIT_SUCCESS} when all tests succeed;
     *  {@link ExitCodes#EXIT_FAIL} when one or more tests fail.
     */
    protected int runTests(Configuration configuration) {
        ResultOutputStream.addLeadIn(configuration);
        
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        for (PathConfiguration pathConfiguration : configuration.getChangedPathConfigurations()) {
            for (File f : new File(configuration.getCheckoutDir(), pathConfiguration.getPath()).listFiles()) {
                pathConfiguration.setWorkingDir(f);
                if (runTests(pathConfiguration) == ExitCodes.EXIT_FAIL) {
                    exitCode = ExitCodes.EXIT_FAIL;
                }
            }
        }
        
        ResultOutputStream.addLeadOut(configuration);
        
        return exitCode;
    }
    
}
