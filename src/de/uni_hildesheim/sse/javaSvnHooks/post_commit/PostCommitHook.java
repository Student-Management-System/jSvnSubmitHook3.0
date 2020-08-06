package de.uni_hildesheim.sse.javaSvnHooks.post_commit;

import java.io.ByteArrayOutputStream;
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
import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;
import de.uni_hildesheim.sse.javaSvnHooks.util.ResultOutputStream;

/**
 * Main class for post-commit-hooks. This class contains the main-method that
 * will be called from the post-commit script.
 * 
 * @author Adam Krafczyk
 */
public class PostCommitHook extends CommitHook {
    
    private PostCommitConfiguration config;
    
    /**
     * Executes this post-commit hook.
     * 
     * @param args The command line parameters. This needs to be exactly 3 elements:
     * <ul>
     *  <li>[0] The path to the SVN repository</li>
     *  <li>[1] The revision of the commit</li>
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
        
        config = null;
        ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
        
        
        // Parse command line arguments
        if (args.length != 3) {
            Logger.INSTANCE.log("Invalid number of command line arguments");
            exitCode = ExitCodes.EXIT_ERROR;
        } else {
            // Set up logging
            try {
                Logger.INSTANCE.setOutputStream(new PrintStream(
                        new FileOutputStream(new File(args[2], "jSvnPostCommitHook.log"), true)
                ));
                Logger.INSTANCE.log("\n--------------------");
                Logger.INSTANCE.log("Current time: " + new Date() + "\n");
            } catch (IOException e) {
                Logger.INSTANCE.logException(e, false);
            }
            Logger.INSTANCE.log("Hook called with: " + args[0] + " " + args[1] + " " + args[2]);
            
            // Set up configuration
            config = new PostCommitConfiguration(args[0], args[1], args[2], testOutput);
        }
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            exitCode = beforeTests();
        }
        
        Logger.INSTANCE.log("Commit author: " + config.getCommitAuthor());
        Logger.INSTANCE.log("Commit comment: " + config.getCommitComment());
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            if (config.isUnrestrictedUser(config.getCommitAuthor())) {
                Logger.INSTANCE.log("Author is an unrestricted user, skipping tests.");
            } else {
                exitCode = runTests(config);
            }
        }
        
        
        if (config != null) {
            int status = afterTests(exitCode, testOutput.toString());
            if (status != ExitCodes.EXIT_SUCCESS) {
                exitCode = status;
            }
        }
        
        Logger.INSTANCE.log("Exiting with " + exitCode);
        return exitCode;
    }
    
    /**
     * Checks out all sub-folders in {@link PathConfiguration}s that contain files
     * that are changed by this transaction. Stores the changed {@link PathConfiguration}s
     * in the {@link PostCommitConfiguration}.
     * 
     * @throws SVNException If checking out the files throws a {@link SVNException}.
     */
    private void checkoutChangedFolders() throws SVNException {
        SVNLookClient client = new SVNLookClient((ISVNAuthenticationManager) null, null);
        PostCommitSvnCatHandler handler = new PostCommitSvnCatHandler(config, client);
        
        client.doGetChanged(config.getRepository(), config.getRevision(), handler, false);
        
        // Get affected PathConfigurations
        Set<PathConfiguration> changedPCs = new HashSet<PathConfiguration>();
        Iterator<PathConfiguration> pathConfigs = config.pathConfigurations();
        while (pathConfigs.hasNext()) {
            PathConfiguration pathConfig = pathConfigs.next();
            for (String path : handler.getChangedFiles()) {
                if (path.startsWith(pathConfig.getPath())) {
                    changedPCs.add(pathConfig);
                    break;
                }
            }
        }
        
        config.setChangedPathConfigurations(changedPCs);
        
        // Do a complete checkout of the sub-folders inside the changed PathConfigurations
        for (PathConfiguration pathConfig : changedPCs) {
            File workingDir = new File(config.getCheckoutDir(), pathConfig.getPath());
            Path checkoutDirPath = config.getCheckoutDir().toPath();
            for (File f : workingDir.listFiles()) {
                if (f.isDirectory()) {
                    String repositoryPath = checkoutDirPath.relativize(f.toPath()).toString();
                    client.doGetTree(config.getRepository(),
                            repositoryPath, config.getRevision(), false, true, handler);
                }
            }
        }
    }
    
    /**
     * Sends a JSON message to the student management system containing the result of the submission-
     * 
     * @param repositoryPath The path inside the repository that was changed, without a leading /.
     *          For example: Testblatt01Aufgabe01/JP001
     * @param testOutput The result of the tests that ran. Typically this is formatted as XML
     *          (if {@link Configuration#produceXmlOutput()} is true).
     * 
     * @return {@link ExitCodes#EXIT_ERROR} on error, {@link ExitCodes#EXIT_SUCCESS} otherwise.
     */
    private int notifyManagementSystem(String repositoryPath, String testOutput) {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        /*
        String url = config.getStringProperty("managementSystemUrl", null);
        if (url != null) {
            String author = config.getCommitAuthor();
            Logger.INSTANCE.log("[TODO] Send commit information to " + url);
            Logger.INSTANCE.log("           author = " + author);
            Logger.INSTANCE.log("           repositoryPath = " + repositoryPath);
            Logger.INSTANCE.log("           testOutput = " + testOutput);
            Logger.INSTANCE.log("           unrestrictedUser = " + config.isUnrestrictedUser(author));
        }
        */
        
        return exitCode;
    }
    
    /**
     * Creates checkout directory and checks out all necessary files.
     * 
     * @return {@link ExitCodes#EXIT_ERROR} on error,
     * {@link ExitCodes#EXIT_SUCCESS} otherwise.
     */
    private int beforeTests() {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        exitCode = createDirecotries(config);
        
        if (exitCode == ExitCodes.EXIT_SUCCESS) {
            try {
                SVNLookClient client = new SVNLookClient((ISVNAuthenticationManager) null, null);
                
                config.setCommitAuthor(
                        client.doGetAuthor(config.getRepository(), config.getRevision()));
                config.setCommitComment(
                        client.doGetLog(config.getRepository(), config.getRevision()));
                
                checkoutChangedFolders();
            } catch (SVNException e) {
                Logger.INSTANCE.logException(e, false);
                exitCode = ExitCodes.EXIT_ERROR;
            }
        }
        
        return exitCode;
    }
    
    /**
     * Cleans up and handles results after all tests are run.
     * 
     * @param exitCode The exit code of the tests.
     * @param testOutput The output of the tests that ran.
     * 
     * @return <ul>
     *      <li>{@link ExitCodes#EXIT_ERROR} on error</li>
     *      <li>{@link ExitCodes#EXIT_FAIL} if testOutput is not empty (this is
     *              so that also warnings are sent to the client.</li>
     *      <li>{@link ExitCodes#EXIT_SUCCESS} otherwise</li>
     * </ul>
     */
    private int afterTests(int exitCode, String testOutput) {
        int returnValue = ExitCodes.EXIT_SUCCESS;
        
        if (exitCode == ExitCodes.EXIT_ERROR) {
            ResultOutputStream.addErrorMessage(config, "hook", 
                "An internal error occured.");
        }
        
        // notify stuff about the commit
        for (PathConfiguration pathConfiguration : config.getChangedPathConfigurations()) {
            Path checkoutDirPath = config.getCheckoutDir().toPath();
            for (File f : new File(config.getCheckoutDir(), pathConfiguration.getPath()).listFiles()) {
                String repositoryPath = checkoutDirPath.relativize(f.toPath()).toString();

                // notify the student management system
                int status;
                status = notifyManagementSystem(repositoryPath, testOutput);
                if (status != ExitCodes.EXIT_SUCCESS) {
                    returnValue = status;
                }
            }
        }
        
        deleteCheckoutDir(config);
        
        boolean outputEmpty;
        if (config.produceXmlOutput()) {
            String newLine = System.getProperty("line.separator");
            outputEmpty = testOutput.startsWith("<submitResults>" + newLine + "</submitResults>");
        } else {
            outputEmpty = testOutput.isEmpty();
        }
        
        // Set return value to FAIL if warnings should be sent to client
        // This is needed since SVN only sends System.err to client if exitCode != 0
        if ((!outputEmpty && exitCode == ExitCodes.EXIT_SUCCESS) 
                // Only for restricted users
                && !config.isUnrestrictedUser(config.getCommitAuthor())) {
            Logger.INSTANCE.log("Exit code is 0, but error message are not empty; setting exit code to 1");
            returnValue = ExitCodes.EXIT_FAIL;
        }

        System.err.print(testOutput);
        
        return returnValue;
    }
    
    @Override
    protected boolean isTestEnabled(PathConfiguration pathConfiguration, Test test) {
        return test.runInPostCommit(pathConfiguration);
    }

    /**
     * The main-method that will be called from the pre-commit hook script.
     * Exit codes:
     * <ul>
     *  <li>{@link ExitCodes#EXIT_SUCCESS}: All tests return 0 (i.e. all tests passed)</li>
     *  <li>{@link ExitCodes#EXIT_FAIL}: At least 1 tests returns 1 (i.e. test not passed)</li>
     *  <li>{@link ExitCodes#EXIT_ERROR}: An error occurred</li>
     * </ul>
     * 
     * @param args The command line parameters. This needs to be exactly 3 elements:
     * <ul>
     *  <li>[0] The path to the SVN repository</li>
     *  <li>[1] The revision of the commit</li>
     *  <li>[2] The path to this hook</li>
     * </ul>
     */
    public static void main(String[] args) {
        int exitCode = new PostCommitHook().execute(args);
        System.exit(exitCode);
    }
    
}
