package de.uni_hildesheim.sse.javaSvnHooks.pre_commit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import de.uni_hildesheim.sse.javaSvnHooks.CommitHook;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;

/**
 * Main class for pre-commit-hooks. This class contains the main-method that
 * will be called from the pre-commit script.
 * 
 * @author Adam Krafczyk
 */
public class PreCommitHook extends CommitHook {
    
    /**
     * Executes this pre-commit hook.
     * 
     * @param args The command line parameters. This needs to be exactly 3 elements:
     * <ul>
     *  <li>[0] The path to the SVN repository</li>
     *  <li>[1] The transaction identifier</li>
     *  <li>[2] The path to this hook</li>
     * </ul>
     * 
     * @return The exit code:
     * <ul>
     *  <li>{@link ExitCodes#EXIT_SUCCESS}: All tests return 0 (i.e. all tests passed)</li>
     *  <li>{@link ExitCodes#EXIT_FAIL}: At least 1 tests returns 1 (i.e. test not passed)</li>
     *  <li>{@link ExitCodes#EXIT_ERROR}: An error occured</li>
     * </ul>
     */
    public int execute(String[] args) {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        PreCommitConfiguration configuration = null;
        
        // Parse command line arguments
        if (args.length != 3) {
            Logger.INSTANCE.log("Invalid number of command line arguments");
            exitCode = ExitCodes.EXIT_ERROR;
        } else {
            // Set up logging
            try {
                Logger.INSTANCE.setOutputStream(new PrintStream(
                        new FileOutputStream(new File(args[2], "jSvnPreCommitHook.log"), true)
                ));
                Logger.INSTANCE.log("\n--------------------");
                Logger.INSTANCE.log("Current time: " + new Date() + "\n");
            } catch (IOException e) {
                Logger.INSTANCE.logException(e, false);
            }
            Logger.INSTANCE.log("Hook called with: " + args[0] + " " + args[1] + " " + args[2]);
            
            // Set up configuration
            configuration = new PreCommitConfiguration(args[0], args[1], args[2]);
        }
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            exitCode = beforeTests(configuration);
        }
        
        Logger.INSTANCE.log("Commit author: " + configuration.getCommitAuthor());
        Logger.INSTANCE.log("Commit comment: " + configuration.getCommitComment());
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            if (configuration.isUnrestrictedUser(configuration.getCommitAuthor())) {
                Logger.INSTANCE.log("Author is an unrestricted user, skipping tests.");
            } else {
                exitCode = runTests(configuration);
            }
        }
        
        if (configuration != null) {
            afterTests(configuration);
        }
        
        Logger.INSTANCE.log("Exiting with " + exitCode);
        return exitCode;
    }
    
    /**
     * Checks out all sub-folders in {@link PathConfiguration}s that contain files
     * that are changed by this transaction. Stores the changed {@link PathConfiguration}s
     * in the {@link PreCommitConfiguration}.
     * 
     * @param configuration The {@link PreCommitConfiguration}.
     * 
     * @throws SVNException If checking out the files throws a {@link SVNException}.
     */
    private void checkoutChangedFolders(PreCommitConfiguration configuration) throws SVNException {
        SVNLookClient client = new SVNLookClient((ISVNAuthenticationManager) null, null);
        PreCommitSvnCatHandler handler = new PreCommitSvnCatHandler(configuration, client);
        
        client.doGetChanged(configuration.getRepository(), configuration.getTransaction(), handler, false);
        
        // Get affected PathConfigurations
        Set<PathConfiguration> changedPCs = new HashSet<PathConfiguration>();
        Iterator<PathConfiguration> pathConfigs = configuration.pathConfigurations();
        while (pathConfigs.hasNext()) {
            PathConfiguration pathConfig = pathConfigs.next();
            for (String path : handler.getChangedFiles()) {
                if (path.startsWith(pathConfig.getPath())) {
                    changedPCs.add(pathConfig);
                    break;
                }
            }
        }
        
        configuration.setChangedPathConfigurations(changedPCs);
        
        // Do a complete checkout of the sub-folders inside the changed PathConfigurations
        for (PathConfiguration config : changedPCs) {
            File workingDir = new File(configuration.getCheckoutDir(), config.getPath());
            Path checkoutDirPath = configuration.getCheckoutDir().toPath();
            for (File f : workingDir.listFiles()) {
                if (f.isDirectory()) {
                    String repositoryPath = checkoutDirPath.relativize(f.toPath()).toString();
                    client.doGetTree(configuration.getRepository(),
                            repositoryPath, configuration.getTransaction(), false, true, handler);
                }
            }
        }
    }
    
    @Override
    protected boolean isTestEnabled(PathConfiguration pathConfiguration, Test test) {
        return test.runInPreCommit(pathConfiguration);
    }
    
    /**
     * Creates checkout directory and checks out all necessary files.
     * 
     * @param configuration The configuration object.
     * 
     * @return {@link ExitCodes#EXIT_ERROR} on error,
     * {@link ExitCodes#EXIT_SUCCESS} otherwise.
     */
    private int beforeTests(PreCommitConfiguration configuration) {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        exitCode = createDirecotries(configuration);
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            try {
                SVNLookClient client = new SVNLookClient((ISVNAuthenticationManager) null, null);
                
                configuration.setCommitAuthor(
                        client.doGetAuthor(configuration.getRepository(), configuration.getTransaction()));
                configuration.setCommitComment(
                        client.doGetLog(configuration.getRepository(), configuration.getTransaction()));
                
                checkoutChangedFolders(configuration);
            } catch (SVNException e) {
                Logger.INSTANCE.logException(e, false);
                exitCode = ExitCodes.EXIT_ERROR;
            }
        }
        
        return exitCode;
    }
    
    /**
     * Cleans up after all tests are run.
     * 
     * @param configuration The configuration object.
     */
    private void afterTests(PreCommitConfiguration configuration) {
        // config.testOutput is already set to System.err
        // System.err is automatically sent to client, if test failed
        //  so we don't need to do it here.
        
        deleteCheckoutDir(configuration);
    }

    /**
     * The main-method that will be called from the pre-commit hook script.
     * Exit codes:
     * <ul>
     *  <li>{@link ExitCodes#EXIT_SUCCESS}: All tests return 0 (i.e. all tests passed)</li>
     *  <li>{@link ExitCodes#EXIT_FAIL}: At least 1 tests returns 1 (i.e. test not passed)</li>
     *  <li>{@link ExitCodes#EXIT_ERROR}: An error occured</li>
     * </ul>
     * 
     * @param args The command line parameters. This needs to be exactly 3 elements:
     * <ul>
     *  <li>[0] The path to the SVN repository</li>
     *  <li>[1] The transaction identifier</li>
     *  <li>[2] The path to this hook</li>
     * </ul>
     */
    public static void main(String[] args) {
        int exitCode = new PreCommitHook().execute(args);
        System.exit(exitCode);
    }

}
