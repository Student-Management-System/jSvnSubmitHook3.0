package de.uni_hildesheim.sse.javaSvnHooks.tests.java_package;

import java.io.File;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;

/**
 * Checks whether given java packages are submitted if noJavaFileAsError is set to true
 * in global configuration.
 * 
 * @author Holger Eichelberger
 */
public class JavaPackageTest extends Test {

    private Pattern requiredRegEx = null;
    private Pattern forbiddenRegEx = null;
    
    @Override
    public String getName() {
        return "Checking whether java packages are submitted";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return false;
    }

    /**
     * Turns a string into a regEx pattern. Invalid patterns are logged.
     * 
     * @param string the string
     * @return the regEx, <b>null</b> if not given or invalid
     */
    private static Pattern toPattern(String string) {
        Pattern result = null;
        if (null != string) {
            string = string.trim();
            if (0 == string.length()) {
                result = null;
            } else {
                try {
                    result = Pattern.compile(string);
                } catch (PatternSyntaxException e) {
                    Logger.INSTANCE.log("Package patternRegEx config failure "
                         + "for " + string + " (ignorde): " + e.getMessage());
                }
            }
        }
        return result;
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        requiredRegEx = toPattern(
                pathConfiguration.getStringProperty("requiredPackageRegEx", null));
        forbiddenRegEx = toPattern(
            pathConfiguration.getStringProperty("forbiddenPackageRegEx", null));
        return null != requiredRegEx || null != forbiddenRegEx;
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        Boolean requiredFound = null;
        Boolean forbiddenFound = null;
        int exitCode = ExitCodes.EXIT_SUCCESS;
        for (File f : pathConfiguration.getJavaFiles()) {
            File folder = f;
            if (!folder.isDirectory()) {
                folder = f.getParentFile();
            }
            String name = pathConfiguration.relativizeFileName(folder.getAbsolutePath());
            name = name.replace('/', '.').replace('\\', '.');
            while (name.endsWith(".")) {
                name = name.substring(0, name.length() - 1);
            }
            if (null != requiredRegEx) {
                boolean matches = requiredRegEx.matcher(name).matches();
                if (null == requiredFound) {
                    requiredFound = matches;
                } else {
                    requiredFound |= matches; // shall at least once occur
                }
            }
            if (null != forbiddenRegEx) {
                boolean matches = forbiddenRegEx.matcher(name).matches();
                if (null == forbiddenFound) {
                    forbiddenFound = matches;
                } else {
                    forbiddenFound |= matches; // shall never occur
                }                
            }
        }

        Configuration config = pathConfiguration.getGlobalConfiguration();
        PrintStream out = config.getTestOutputStream();
        if (null != requiredFound && !requiredFound) {
            out.println("<message tool=\"java-package-check\""
                + " type=\"error\" file=\"\" line=\"\" message=\"Required"
                + " package (" + requiredRegEx.pattern() + ") not found.\">"
                + " </message>");
            exitCode = ExitCodes.EXIT_FAIL;
        }
        if (null != forbiddenFound && forbiddenFound) {
            out.println("<message tool=\"java-package-check\""
                + " type=\"error\" file=\"\" line=\"\" message=\"Forbidden"
                + " package found: " + forbiddenRegEx.pattern() + "\">"
                + "</message>");
            exitCode = ExitCodes.EXIT_FAIL;
        }
        return exitCode;
    }

    static {
        Test.registerTest(new JavaPackageTest());
    }
}
