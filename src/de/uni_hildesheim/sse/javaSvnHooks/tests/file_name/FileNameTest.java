package de.uni_hildesheim.sse.javaSvnHooks.tests.file_name;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Checks whether submitted files comply with a given regular expression.
 * 
 * @author Holger Eichelberger
 */
public class FileNameTest extends Test {

    public static final String REGEX_MSG_PREFIX = "fileNameRegEx configuration error: ";
    public static final String DFLT_MSG_PREFIX = "File with illegal name submitted";
    private static final String MSG_TOOL_NAME = "filename-check";
    
    @Override
    public String getName() {
        return "Checking whether files with illegal names are submitted";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }
    
    /**
     * Returns the file name regular expression.
     * 
     * @param pathConfiguration the path configuration
     * @return the file name regular expression, <b>null</b> for none
     */
    private String getFileNameRegEx(PathConfiguration pathConfiguration) {
        String result = pathConfiguration.getGlobalConfiguration().getStringProperty("fileNameRegEx", null);
        if (null != result && result.length() == 0) {
            result = null;
        }
        return result;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return null != getFileNameRegEx(pathConfiguration);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return false;
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        int exitCode = 0;
        String regEx = getFileNameRegEx(pathConfiguration);
        if (null != regEx) {
            Configuration config = pathConfiguration.getGlobalConfiguration();
            String message = config.getStringProperty("fileNameRegExMsg", DFLT_MSG_PREFIX);
            if (config.produceXmlOutput()) {
                message = XmlUtilities.xmlify(message);
            }
            try {
                Pattern pattern = Pattern.compile(regEx);
                Iterator<File> iter = pathConfiguration.getFiles().iterator();
                while (iter.hasNext()) {
                    File file = iter.next();
                    if (!pattern.matcher(file.getName()).matches()) {
                        error(config, MSG_TOOL_NAME, file.getName(), message);
                        exitCode = 1;
                    }
                }
            } catch (PatternSyntaxException e) {
                error(config, MSG_TOOL_NAME, REGEX_MSG_PREFIX + e.getMessage().replace("\n", "") 
                    + " Please inform tutor!");
                exitCode = 1;
            }
        }
        return exitCode;
    }
    
    /**
     * Registers this test automatically when loading this class.
     * 
     * @since 1.00
     */
    static {
        registerTest(new FileNameTest());
    }

}
