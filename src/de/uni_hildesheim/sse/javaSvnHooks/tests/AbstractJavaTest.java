package de.uni_hildesheim.sse.javaSvnHooks.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.util.StreamGobbler;

/**
 * Realizes the basic implementation for a Java related commit test (plugin).
 * This class considers the following configuration parameters:
 * <ul>
 *  <li><code>javaSourcePath</code> as global property defines the fixed
 *      java source path to be considered by the subclassed java tests.</li>
 *  <li><code>config<i>Nr</i>.javaSourcePath</code> as path local 
 *      property defines the fixed java source path to be considered by 
 *      the subclassed java tests.</li>
 *  <li><code>javaSourcePathPrefix</code> as global property defines the
 *      prefix identifying a possible source path. If the prefix ends with
 *      <code>*</code>, the prefix is compared ignoring cases. This property
 *      is used to infer the source path.
 *  <li><code>config<i>Nr</i>.javaSourcePathPrefix</code> as path local 
 *      property defines the prefix identifying a possible source path. 
 *      If the prefix ends with <code>*</code>, the prefix is compared 
 *      ignoring cases. This property is used to infer the source path.
 * </ul>
 * Here a path configuration supersedes a global configuration.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public abstract class AbstractJavaTest extends Test {

    private static final String INSTALL_DIR;
    
    static {
        String instDir = ".";
        try {
            File f = new File(instDir);
            File pf = f.getParentFile();
            if (null != pf) {
                f = pf;
            }
            instDir = f.getAbsolutePath();
        } catch (SecurityException e) {
        }
        INSTALL_DIR = instDir;
    }
    
    /**
     * Defines the basic keys for the dynamic test environment
     * in the global configuration.
     *
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    public enum TestEnvKey implements de.uni_hildesheim.sse.javaSvnHooks.TestEnvKey {
    
        /**
         * Denotes the dynamic part of the classpath.
         *
         * @since 1.20
         */
        CLASSPATH,

        /**
         * Denotes the dynamic java source path.
         *
         * @since 1.20
         */
        SRC,

        /**
         * Denotes the dynamic java binary path.
         *
         * @since 1.20
         */
        BIN;
    }

    /**
     * Returns the Java classpath as single path entries.
     * 
     * @param pathConfiguration the configuration to be considered
     * @param keyPrefix the key prefix denoting the name of the checker
     * @param addBinPathToClasspath if <code>true</code> then the bin 
     *        path from the dynamic environment is added to the class
     *        path if present
     * @return the Java classpath
     * 
     * @since 1.00
     */
    protected List<String> getClasspath(PathConfiguration pathConfiguration,
        String keyPrefix, boolean addBinPathToClasspath) {
        List<String> result = JavaClasspath.getClasspath(pathConfiguration, 
            keyPrefix);
        if (addBinPathToClasspath) {
            String binPath = getJavaSourcePath(pathConfiguration);
            if (null != binPath && binPath.length() > 0) {
                JavaClasspath.appendToClasspath(result, binPath);
            }
        }
        return result;
    }

    /**
     * Combines the default Java (compiler/interpreter) command line 
     * parameters collected from <code>configuration</code>. This 
     * method considers the configuration properties 
     * <code><i>keyPrefix</i>.libDir</code>,  
     * <code><i>keyPrefix</i>.params</code> and <code>classpath</code>
     * from the path configuration.
     * 
     * @param pathConfiguration the configuration to be considered
     * @param keyPrefix the key prefix denoting the name of the checker
     * @param addBinPathToClasspath if <code>true</code> then the bin 
     *        path from the dynamic environment is added to the class
     *        path if present
     * @return the compiled Java command line parameters
     * 
     * @since 1.00
     */
    protected List<String> getJavaParam(PathConfiguration pathConfiguration,
        String keyPrefix, boolean addBinPathToClasspath) {
        
        /*String params = pathConfiguration.getClasspathProperty();
        params = appendLibDirsToClasspath(params, pathConfiguration,
            keyPrefix);
        String testEnvClasspath = pathConfiguration.getGlobalConfiguration().
            getTestEnvValue(TestEnvKey.CLASSPATH);
        if (null != testEnvClasspath) {
            params = appendToClasspath(params, testEnvClasspath);
        }
        String params = JavaClasspath.classpathToString(
            JavaClasspath.getClasspath(pathConfiguration, keyPrefix));
        if (addBinPathToClasspath) {
            String binPath = getJavaSourcePath(pathConfiguration);
            if (null != binPath && binPath.length() > 0) {
                params = appendToClasspath(params, binPath);
            }
        }*/
        String params = JavaClasspath.classpathToString(getClasspath(
            pathConfiguration, keyPrefix, addBinPathToClasspath));
        List<String> result = new ArrayList<String>();
        if (params.length() > 0) {
            result.add("-classpath");
            result.add(params);
        }
        String prop = pathConfiguration.getStringProperty(keyPrefix + ".params", "");
        if (prop.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(prop);
            String buffer = "";
            int quoteLevel = 0;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.startsWith("\"")) {
                    quoteLevel++;
                } else if (token.endsWith("\"")) {
                    quoteLevel--;
                }
                buffer = appendToBuffer(buffer, token);
                if (0 == quoteLevel) {
                    result.add(buffer);
                    buffer = "";
                }
            }
        }
        return result;
    }

    /**
     * Appends the given {@code text} to {@code buffer}.
     * 
     * @param buffer the buffer to append to
     * @param text the text to append
     * @return the changed buffer
     */
    private String appendToBuffer(String buffer, String text) {
        String result = buffer;
        if (result.length() > 0) {
            result += " ";
        }
        result += text;
        return result;
    }
    
    /**
     * Converts a list of parameter to a command line parameter string.
     * 
     * @param params the parameters to be converted
     * @return the converted string
     * 
     * @since 1.00
     */
    protected static List<String> string2Params(String params) {
        List<String> result = new ArrayList<String>();
        boolean skipWhitespace = false;
        int size = params.length();
        int lastPos = 0;
        for (int i = 0; i < size; i++) {
            char c = params.charAt(i);
            if (c == ' ') {
                if (!skipWhitespace) {
                    String par = params.substring(lastPos, i).trim();
                    if (par.length() > 0) {
                        result.add(par);
                    }
                    lastPos = i + 1;
                }
            } else if (c == '"' || c == '\'') {
                skipWhitespace = !skipWhitespace;
            }
        }
        if (lastPos != size) {
            String par = params.substring(lastPos);
            if (par.length() > 0) {
                result.add(par);
            }
        }
        return result;
    }
    
    /**
     * Turns a command/parameter list into a string.
     * 
     * @param cmd the list
     * @return the representing string
     */
    protected String cmdList2String(List<String> cmd) {
        StringBuilder result = new StringBuilder();
        for (String s : cmd) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(s);
        }
        return result.toString();
    }

    /**
     * Returns a command/parameter list as String array.
     * 
     * @param cmd the list
     * @return the array
     */
    protected String[] cmdList2Array(List<String> cmd) {
        String[] cmdArray = new String[cmd.size()];
        cmd.toArray(cmdArray);
        return cmdArray;
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
    protected static String appendToClasspath(String classpath, String entry) {
        if (entry.length() > 0) {
            if (0 == classpath.length()) {
                classpath = entry;
            } else {
                classpath += File.pathSeparator + entry;
            }
        }
        return classpath;
    }

    /**
     * Appends libraries to the specified <code>classpath</code>.
     * 
     * @param classpath the classpath to which the libraries should be
     *        appended
     * @param pathConfiguration the configuration to be considered
     * @param keyPrefix the key prefix denoting the name of the checker
     * @return the combined classpath
     * 
     * @since 1.00
     */
    protected static String appendLibDirsToClasspath(String classpath, 
        PathConfiguration pathConfiguration, String keyPrefix) {
        String libDir = 
            pathConfiguration.getStringProperty(keyPrefix + ".libDir", "");
        if (libDir.length() > 0) {
            File homeDir = new File(pathConfiguration.getWorkingDir(), libDir);
            StringBuffer buf = new StringBuffer(classpath);
            enumJars(homeDir, buf);
            classpath = appendToClasspath(classpath, buf.toString());
            if (classpath.length() > 0 && !classpath.startsWith(".")) {
                classpath = "." + File.separator + classpath;
            }
        }
        return classpath;
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
    protected static void enumJars(File dir, StringBuffer result) {
        File[] dirs = dir.listFiles();
        if (null != dirs) {
            for (File f : dirs) {
                if (f.isDirectory()) {
                    enumJars(f, result);
                } else {
                    if (f.getName().endsWith(".jar")) {
                        if (result.length() > 0) {
                            result.append(File.pathSeparator);
                        }
                        result.append(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Returns the binary path for java tests. 
     * 
     * @param pathConfiguration the configuration to be considered
     * @return the binary path for java tests or 
     *         <b>null</b>
     * 
     * @since 1.20
     */
    protected String getJavaBinaryPath(PathConfiguration pathConfiguration) {
        String result = null;
        String testEnv = pathConfiguration.getGlobalConfiguration().
            getTestEnvValue(TestEnvKey.BIN);
        if (null != testEnv && testEnv.length() > 0) {
            result = testEnv;
        }
        return result;
    }
    
    /**
     * Returns the (inferred) source path for java tests. This method
     * queries for the global or local property 
     * <code>javaSourcePath</code>. If this property is not set, 
     * <code>javaSourcePathPrefix</code> is be used to retrieve a 
     * prefix that may match the leading source directory
     * path. If prefix ends with <code>*</code> the prefix is compared
     * without considering upper and lower cases. If at least the half 
     * number of java files to be considered
     * match the regular expression, the leading directory is assumed
     * to be the source path.
     * 
     * @param pathConfiguration the configuration to be considered
     * @return the inferred source path for java tests or 
     *         <b>null</b>
     * 
     * @since 1.00
     */
    protected String getJavaSourcePath(PathConfiguration pathConfiguration) {
        String result = null;
        String testEnv = pathConfiguration.getGlobalConfiguration().
            getTestEnvValue(TestEnvKey.SRC);
        if (null != testEnv && testEnv.length() > 0) {
            result = testEnv;
        } else {
            Configuration globalConfiguration = 
                pathConfiguration.getGlobalConfiguration();
            String sourcePath = pathConfiguration.
                getStringProperty("javaSourcePath", null);
            if (null == sourcePath) {
                sourcePath = globalConfiguration.
                    getStringProperty("javaSourcePath", null);
            }
            if (null != sourcePath) {
                result = sourcePath;
            } else {
                String sourcePathPrefix = pathConfiguration.
                    getStringProperty("javaSourcePathPrefix", null);
                if (null == sourcePathPrefix) {
                    sourcePathPrefix = globalConfiguration.
                        getStringProperty("javaSourcePathPrefix", null);
                }
                if (null != sourcePathPrefix) {
                    result = inferSourcePath(pathConfiguration, 
                        sourcePathPrefix);
                }
            }
        }
        return result;
    }
    
    /**
     * Infers the source path.
     * 
     * @param pathConfiguration the configuration to be considered
     * @param sourcePathPrefix the expected source path prefix
     * @return the inferred source path (or <b>null</b>)
     * 
     * @since 1.00
     */
    private String inferSourcePath(PathConfiguration pathConfiguration, 
        String sourcePathPrefix) {
        String result = null;

        boolean ignoreCase = false;
        if (sourcePathPrefix.endsWith("*")) {
            sourcePathPrefix = 
                sourcePathPrefix.substring(0, sourcePathPrefix.length() - 1);
            sourcePathPrefix = sourcePathPrefix.toLowerCase();
            ignoreCase = true;
        }
        
        int countJavaFiles = 0;
        int countMatchingFiles = 0;
        for (Iterator<File> jI = pathConfiguration.getJavaFiles().iterator(); jI.hasNext();) {
            String sourceFile = jI.next().toString();
            String compFile = sourceFile;
            if (ignoreCase) {
                compFile = compFile.toLowerCase();
            }
            if (compFile.startsWith(sourcePathPrefix)) {
                countMatchingFiles++;
                int start = 0;
                int pos = sourceFile.indexOf(File.separator);
                if (0 == pos) {
                    pos = sourceFile.indexOf(File.separator, 1);
                    start = 1;
                }
                if (pos > 0) {
                    result = sourceFile.substring(start, pos);
                }
            }
            countJavaFiles++;
        }
        // heuristics
        if (countMatchingFiles != countJavaFiles) {
            result = null;
        }
        return result;
    }

    /**
     * Deletes all commited and already compiled *.class files.
     * @param homeDir The directory of the commited project.
     */
    protected void deleteCompiledClasses(File homeDir) {
        Collection<File> classFiles = FileUtils.listFiles(homeDir, new String[] {"class"}, true);
        for (File f : classFiles) {
            f.delete();
        }
    }

    /**
     * Loads additional parameters for the javac compiler.
     * @param pathConfiguration the configuration to be considered
     * @param homeDir The directory of the committed project.
     * @param keyPrefix the key prefix denoting the name of the checker
     * @param binPath the bin path from the dynamic environment; if not
     *        <code>null</code> then it is added to the class path
     * @return the compiled Java command line parameters
     */
    protected List<String> readParams(PathConfiguration pathConfiguration,
        File homeDir, String binPath, String keyPrefix) {
            
        List<String> result = getJavaParam(pathConfiguration, keyPrefix, false);
        if (null != binPath && binPath.length() > 0) {
            File binPathFile = new File(homeDir, binPath);
            if (!binPathFile.exists()) {
                binPathFile.mkdirs();
            } else {
                FileUtils.deleteQuietly(binPathFile);
                binPathFile.mkdirs();
            }
            result.add("-d");
            result.add(binPathFile.getAbsolutePath());
        }
        return result;
    }
    
    /**
     * Runs the specified progress. Therefore 
     * {@link StreamGobbler StreamGobblers} are created and attached
     * to the streams of the process.
     * 
     * @param config The {@link Configuration} object
     * @param process the process to be executed
     * @param printOut transfer the default output stream of the process 
     *        to the system error stream
     * @param filter an optional test message filter
     * @return the operating system shell return code (0 denotes
     *         success)
     * @throws InterruptedException if the current thread is interrupted 
     *         by another thread while it is waiting, then the wait is ended 
     *         and an InterruptedException is thrown.
     */
    protected int runProcess(Configuration config, Process process, boolean printOut,
        ITestMessageFilter filter) throws InterruptedException {
        StreamGobbler errorGobbler =
            new StreamGobbler(process.getErrorStream(), 
                config.getTestOutputStream(), filter);
        StreamGobbler outputGobbler =
            new StreamGobbler(process.getInputStream(), 
                printOut ? config.getTestOutputStream() : null, filter);
        int result = process.waitFor();
        int sleepCount = 0;
        // wait for gobblers but at maximum 2 minutes
        int maxProcessWaitSteps = Math.max(0, 
            config.getIntProperty("maxProcessWait", 120)) * 2;
        while (errorGobbler.isRunning() || outputGobbler.isRunning() 
            && sleepCount < maxProcessWaitSteps) {
            Thread.sleep(500);
            sleepCount++;
        }
        StringBuffer out = new StringBuffer();
        out.append(errorGobbler);
        out.append(outputGobbler);
        return result;
    }

    /**
     * Replaces placeholder strings.
     * 
     * @param str the string to replace within
     * @return the replaced string
     */
    protected String substituteDirs(String str) {
        return str.replace("$installDir", INSTALL_DIR);
    }

}
