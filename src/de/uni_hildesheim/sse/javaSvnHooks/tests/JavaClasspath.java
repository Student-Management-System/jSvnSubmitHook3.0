package de.uni_hildesheim.sse.javaSvnHooks.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaTest.TestEnvKey;

/**
 * Java classpath utilities.
 * 
 * @author eichelberger
 */
public class JavaClasspath {

    /**
     * Obtains the classpath from the given path configuration.
     * 
     * @param pathConfiguration the path configuration
     * @param keyPrefix the configuration key prefix
     * @return the classpath as individual entries
     */
    public static List<String> getClasspath(PathConfiguration pathConfiguration,
        String keyPrefix) {
        List<String> result = new ArrayList<String>();        
        appendToClasspath(result, pathConfiguration.getClasspathProperty());
        appendLibDirsToClasspath(result, pathConfiguration, keyPrefix);
        String testEnvClasspath = pathConfiguration.getGlobalConfiguration().
            getTestEnvValue(TestEnvKey.CLASSPATH);
        appendToClasspath(result, testEnvClasspath);
        return result;
    }
    
    /**
     * Appends libraries to the specified <code>classpath</code>.
     * 
     * @param classpath the classpath to which the libraries should be
     *        appended
     * @param pathConfiguration the configuration to be considered
     * @param keyPrefix the key prefix denoting the name of the checker
     * 
     * @since 1.00
     */
    protected static void appendLibDirsToClasspath(List<String> classpath, 
        PathConfiguration pathConfiguration, String keyPrefix) {
        String libDir = 
            pathConfiguration.getStringProperty(keyPrefix + ".libDir", "");
        if (libDir.length() > 0) {
            File homeDir = new File(pathConfiguration.getWorkingDir(), libDir);
            enumJars(homeDir, classpath);
            if (classpath.size() > 0 && !classpath.get(0).startsWith(".")) {
                classpath.set(0, "." + File.separator + classpath.get(0));
            }
        }
    }

    /**
     * Enumerates all jar files in the specified directory and adds them
     * as fully qualified paths.
     * 
     * @param dir the file/directory where to start
     * @param result the string buffer to be modified as a side effect
     * 
     * @since 1.00
     */
    protected static void enumJars(File dir, List<String> result) {
        File[] dirs = dir.listFiles();
        if (null != dirs) {
            for (File f : dirs) {
                if (f.isDirectory()) {
                    enumJars(f, result);
                } else {
                    if (f.getName().endsWith(".jar")) {
                        appendToClasspath(result, f.getAbsolutePath());
                    }
                }
            }
        }
    }
    
    /**
     * Appends an element to the given classpath.
     * 
     * @param classpath the classpath to be extended
     * @param entry the entry to be appended
     * @return the combined classpath
     * 
     * @since 1.00
     */
    public static String appendToClasspath(String classpath, String entry) {
        if (entry.length() > 0) {
            //don't unquote, causes runtime.exec/javac to fail
            //if (entry.indexOf(' ') > 0) {
            //    entry = "\"" + entry + "\"";
            //}
            if (0 == classpath.length()) {
                classpath = entry;
            } else {
                classpath += File.pathSeparator + entry;
            }
        }
        return classpath;
    }
    
    /**
     * Appends the given entry to the classpath.
     * 
     * @param classpath The classpath list to append the entry to.
     * @param entry The entry to append.
     */
    public static void appendToClasspath(List<String> classpath, String entry) {
        if (null != entry && entry.length() > 0) {
            // in case of multiple entries, parse out individual ones
            // consider simple quoting
            boolean inQuotes = false;
            int pos = 0;
            int start = 0;
            while (pos < entry.length()) {
                char c = entry.charAt(pos);
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == File.pathSeparatorChar && !inQuotes) {
                    appendEntryToClasspath(classpath, entry, start, pos);
                    start = pos + 1;
                }
                pos++;
            }
            appendEntryToClasspath(classpath, entry, start, pos);
        }
    }
    
    /**
     * Internal helper method for {@link #appendToClasspath(List, String)}.
     * 
     * @param classpath The classpath list.
     * @param entry The entry to append.
     * @param start
     * @param end
     */
    private static void appendEntryToClasspath(List<String> classpath, 
        String entry, int start, int end) {
        if (end > start) {
            String path = entry.substring(start, end).trim();
            if (path.startsWith("\"") && path.endsWith("\"") 
                && path.length() >= 2) {
                path = path.substring(1, path.length() - 1);
            }
            if (path.length() > 0) {
                classpath.add(path);
            }
        }
    }
        
    /**
     * Converts the given classpath list to a string.
     * 
     * @param classpath The classpath list.
     * 
     * @return The same classpath as a string representation.
     */
    public static String classpathToString(List<String> classpath) {
        String result = "";
        for (int i = 0; i < classpath.size(); i++) {
            result = appendToClasspath(result, classpath.get(i));
        }
        return result;
    }
    
}
