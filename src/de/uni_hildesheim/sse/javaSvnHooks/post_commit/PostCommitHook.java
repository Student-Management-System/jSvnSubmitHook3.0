package de.uni_hildesheim.sse.javaSvnHooks.post_commit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import de.uni_hildesheim.sse.javaSvnHooks.CommitHook;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;
import de.uni_hildesheim.sse.javaSvnHooks.util.ResultOutputStream;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

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
     * Commits the test result to the review repository.
     * 
     * @param reviewPath The path to the review repository (e.g. /path/to/repo/configPath/group)
     * @param content The content of the review.txt to be generated.
     * 
     * @return {@link ExitCodes#EXIT_ERROR} on error, {@link ExitCodes#EXIT_SUCCESS} otherwise.
     */
    private int commitReview(String reviewPath, String content) {
        int exitCode = ExitCodes.EXIT_SUCCESS;
        
        Logger.INSTANCE.log("Committing review.txt to " + reviewPath);
        
        File checkoutDir = new File(config.getTempDir(), config.getUniqueIdentifier() + "_review");
        if (!checkoutDir.mkdir()) {
            Logger.INSTANCE.log("Can't create temporary checkout directory for review");
            exitCode = ExitCodes.EXIT_ERROR;
        }
        
        try {
            if (exitCode == ExitCodes.EXIT_SUCCESS) {
                String user = config.getStringProperty("reviewRepo.user", "submitHook");
                String pw = config.getStringProperty("reviewRepo.password", "submitHook");
                SVNURL url = SVNURL.parseURIEncoded(reviewPath);
                SVNClientManager clientManager = SVNClientManager.newInstance(null,
                        BasicAuthenticationManager.newInstance(user, pw.toCharArray()));
                SVNUpdateClient updateClient = clientManager.getUpdateClient();
                updateClient.doCheckout(url, checkoutDir, SVNRevision.HEAD,
                        SVNRevision.HEAD, SVNDepth.INFINITY, true);
                
                File reviewTxt = new File(checkoutDir, "review.txt");
                FileUtils.write(reviewTxt, content, StandardCharsets.UTF_8);
                
                clientManager.getWCClient().doAdd(reviewTxt, true, false, false,
                        SVNDepth.EMPTY, false, false);
                
                SVNCommitClient client = clientManager.getCommitClient();
                client.doCommit(new File[] {checkoutDir}, false,
                        "Automatic Hook Review", null, null, false, false, SVNDepth.INFINITY);
            }
        } catch (SVNException e) {
            Logger.INSTANCE.logException(e, false);
            exitCode = ExitCodes.EXIT_ERROR;
        } catch (IOException e) {
            Logger.INSTANCE.logException(e, false);
            exitCode = ExitCodes.EXIT_ERROR;
        }
        
        try {
            FileUtils.deleteDirectory(checkoutDir);
        } catch (IOException e) {
            Logger.INSTANCE.logException(e, false);
        }
        
        return exitCode;
    }
    
    /**
     * Notifies a web hook about the commit.
     * 
     * @param repositoryPath The path inside the repository that was changed.
     * 
     * @return {@link ExitCodes#EXIT_ERROR} on error, {@link ExitCodes#EXIT_SUCCESS} otherwise.
     */
    private int notifyWebHook(String repositoryPath) {
        Charset charset = Charset.forName("UTF-8");
        String url = config.getStringProperty("webHookAddress", null);
        
        int status = ExitCodes.EXIT_SUCCESS;
        
        if (url != null) {
            Logger.INSTANCE.log("Notifying web hook " + url);
            
            StringBuilder content = new StringBuilder();
            content.append("<commit>");
            content.append("<path>");
            content.append(XmlUtilities.xmlify(repositoryPath));
            content.append("</path>");
            content.append("<commitAuthor>");
            content.append(XmlUtilities.xmlify(config.getCommitAuthor()));
            content.append("</commitAuthor>");
            content.append("<commitComment>");
            content.append(XmlUtilities.xmlify(config.getCommitComment()));
            content.append("</commitComment>");
            content.append("</commit>");
            
            try {
                URLConnection con = new URL(url).openConnection();
                con.setDoOutput(true); // sets to POST
                con.setRequestProperty("Accept", "*/*");
                con.setRequestProperty("Accept-Charset", charset.name());
                con.setRequestProperty("Content-Type", "text/xml; charset=" + charset.name());
                con.setRequestProperty("x-hook-id", "f31710cc25da40c462e6d98296637c8c8755796bcca0f16948f41cf1e930cecf");
                
                OutputStream out = con.getOutputStream();
                out.write(content.toString().getBytes(charset));
                out.close();
                
                InputStream in = con.getInputStream();
                ByteArrayOutputStream answer = new ByteArrayOutputStream(512);
                int read;
                while ((read = in.read()) != -1) {
                    answer.write(read);
                }
                in.close();
                String result = new String(answer.toByteArray(), charset);
                if (result.length() > 0) {
                    Logger.INSTANCE.log("Answer from web hook:\n" + result);
                }
                
            } catch (IOException e) {
                Logger.INSTANCE.logException(e, false);
                status = ExitCodes.EXIT_FAIL;
            }
        }
        
        return status;
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
                
                // Commit to review repository, if user is restricted
                int status = ExitCodes.EXIT_SUCCESS;
                if (config.isUnrestrictedUser(config.getCommitAuthor())) {
                    Logger.INSTANCE.log("Author is an unrestricted user, skipping export to review-repo.");
                } else {
                    String reviewPath = config.getStringProperty("reviewRepo", "") + "/" + repositoryPath;
                    status = commitReview(reviewPath, testOutput);
                    if (status != ExitCodes.EXIT_SUCCESS) {
                        returnValue = status;
                    }
                }
                // Notify the web interface
                status = notifyWebHook(repositoryPath);
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
