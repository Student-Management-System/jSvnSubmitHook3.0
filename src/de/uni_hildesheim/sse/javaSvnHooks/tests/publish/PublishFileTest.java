package de.uni_hildesheim.sse.javaSvnHooks.tests.publish;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;

/**
 * Implements a generic "test" to publish individual files while or
 * after a commit transaction.
 *  
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.publish = true</code> enables this publishing 
 *      plugin.</li>
 *  <li><code>config<i>Nr</i>.publish.commentRegEx = </code> defines 
 *      regular expression the commit comment must match to enable
 *      this plugin.</li>
 *  <li><code>config<i>Nr</i>.publish.needsFilesFromRepository = </code> 
 *      defines if files must be checked out in incremental repository 
 *      commit mode.</li>    
 *  <li><code>config<i>Nr</i>.publish.fileRegEx<i>CNr</i> = </code> 
 *      specifies a regular expression a file within the specified
 *      file set must match to execute the command with number
 *      <i>CNr</i>.</li>
 *  <li><code>config<i>Nr</i>.publish.fileCmd<i>CNr</i> = </code> the 
 *      command to be executed when the regular expression with number
 *      <i>CNr</i> matches a file in this path configuration.</li>
 * </ul>
 * Additionally, <code>fileRegEx</code> and <code>fileCmd</code> may
 * be personalized by SVN user names, i.e. by specifying e.g.  
 * <code>config1.publish.fileRegEx2.administrator</code>. If no user
 * name matches, the non-personalized values are used as default. 
 * Furthermore, personalized configurations must also be given as
 * uninterrupted sequence of integers (<i>Nr</i> as well as <i>CNr</i>). 
 * 
 * @author Holger Eichelberger
 */
class PublishFileTest extends AbstractJavaTest {

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }
    
    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return false;
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("publish", false);
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        Configuration configuration = 
            pathConfiguration.getGlobalConfiguration();
        String commentRegEx = 
            pathConfiguration.getStringProperty("publish.commentRegEx", "");
        if (matches(configuration.getCommitComment(), commentRegEx)) {
            int i = 0;
            String author = configuration.getCommitAuthor();
            if (author.length() > 0) {
                author = "." + author;
            }
            boolean found = false;
            do {
                found = matchConfiguration(pathConfiguration, author, i);
                if (i == 0 && !found && author.length() > 0) {
                    author = "";
                    found = 
                        matchConfiguration(pathConfiguration, author, i);
                }
                i++;
            } while (found  && i < Integer.MAX_VALUE);
        }
        return 0;
    }
    
    /**
     * Tries to find a matching file/command configuration with 
     * given numerical id, optional author and given path 
     * configuration. If a configuration is found, the matching
     * is checked by calling 
     * {@link #matchFiles(PathConfiguration, String, String)}. 
     * 
     * @param pathConfiguration the path configuration to be considered
     * @param author an optional specification of the author, use an
     *        empty string if not relevant; otherwise this string should
     *        start with a "."
     * @param id the numerical identification (counter) of the configuration
     *        statement in the configuration file 
     * @return <code>true</code> if a personalized or non-personalized
     *         configuration was found, <code>false</code> else
     */
    private boolean matchConfiguration(PathConfiguration pathConfiguration,
        String author, int id) {
        boolean configFound = false;
        String fileMatchRegEx = 
            pathConfiguration.getStringProperty(
                "publish.fileRegEx" + id + author, "");
        String filePublishCmd = 
            pathConfiguration.getStringProperty(
                "publish.fileCmd" + id + author, "");
        if (fileMatchRegEx.length() > 0 
            && filePublishCmd.length() > 0) {
            configFound = true;
            matchFiles(pathConfiguration, 
                fileMatchRegEx, filePublishCmd);
        }
        return configFound;
    }
    
    /**
     * Returns if the given <code>string</code> matches the
     * specified regular expression.
     * 
     * @param string the string to be matched
     * @param regEx the regular expression in question if it matches
     *        <code>string</code>
     * @return <code>true</code> if the <code>string</code> matches
     *         <code>regEx</code>, <code>false</code> if there is
     *         no match or the regular expression is of wrong syntax
     */
    private static boolean matches(String string, String regEx) {
        boolean matches = false;
        if (null != string && null != regEx) {
            try {
                matches = string.matches(regEx);
            } catch (PatternSyntaxException pse) {
            }
        }
        return matches;
    }
    
    /**
     * Tries to match the given regular expression with any file
     * in the path configuration. If the regular expression matches
     * the specified command is executed.
     * 
     * @param pathConfiguration the path configuration delivering
     *        the configuration and the committed files
     * @param regEx the regular expression to be matched against the
     *        files in <code>pathConfiguration</code>
     * @param cmd the command to be executed for each file matching
     *        <code>regEx</code>
     */
    private void matchFiles(PathConfiguration pathConfiguration, 
        String regEx, String cmd) {
        for (Iterator<File> iter = pathConfiguration.getFiles().iterator(); 
            iter.hasNext();) {
            String file = iter.next().getPath();
            if (matches(file, regEx)) {
                File homeDir = pathConfiguration.getWorkingDir();
                Runtime runtime = Runtime.getRuntime();
                String[] env = new String[0];
                try {
                    runProcess(pathConfiguration.getGlobalConfiguration(),
                        runtime.exec(cmd + " \"" + file + "\"", env, homeDir), false, null);
                } catch (InterruptedException | IOException exception) {
                    Logger.INSTANCE.logException(exception, false);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Publish individual files";
    }

    /**
     * Registers this test automatically when loading this class.
     */
    static {
        registerTest(new PublishFileTest());
    }
    
}
