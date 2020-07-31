package de.uni_hildesheim.sse.javaSvnHooks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;

/**
 * Base class for all configurations. This {@link Configuration} is needed to
 * run tests.
 * 
 * @author Adam Krafczyk
 */
public abstract class Configuration {

    /**
     * Denots the commit stage.
     * 
     * @author Holger Eichelberger
     */
    public enum Stage {
        PRE_COMMIT,
        POST_COMMIT;
    }
    
    /**
     * Prefix of all properties for specifying settings of an individual submission
     * (repository path inside the svn).
     */
    private static final String PATH_SETTING_PROPERTY_PREFIX = "config";
    
    /**
     * The properties as read from the configuration file.
     */
    private Properties props;
    
    /**
     * Directory where temporary files will be stored.
     */
    private File tempDir;
    
    /**
     * Directory where a copy of the repository files will be stored while running tests.
     */
    private File checkoutDir;
    
    /**
     * Whether test results should be in XML format.
     */
    private boolean produceXMLoutput;
    
    /**
     * Stores the {@link List} of {@link PathConfiguration}s defined in the config file.
     */
    private List<PathConfiguration> pathConfigurations = new ArrayList<PathConfiguration>();
    
    /**
     * Stores the path to the installation directory of the hook.
     */
    private File hookDir;
    
    private String javaBin = "";
    
    /**
     * The {@link PrintStream} where test results will write their output to.
     */
    private PrintStream testOutputStream = new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM);
    
    private Map<TestEnvKey, String> testEnv = new HashMap<TestEnvKey, String>();
    
    /**
     * Stores the author of the commit.
     */
    private String commitAuthor;
    
    /**
     * Stores the commit log entry.
     */
    private String commitComment;
    
    /**
     * Stores the patterns of files to be ignored.
     */
    private List<Pattern> ignoreFilesPatterns;
    
    /**
     * Stores the list of unrestricted user names.
     */
    private List<String> unrestrictedUsers;
    
    /**
     * Stores the set of changed path configurations.
     */
    private Set<PathConfiguration> changedPathConfigurations = new HashSet<PathConfiguration>();
    
    /**
     * Reads the given configuration file.
     * 
     * @param configurationFile The property file to be read as configuration.
     */
    protected void readConfig(File configurationFile) {
        props = new Properties();
        try {
            FileInputStream fis =
                new FileInputStream(configurationFile);
            props.load(fis);
            fis.close();
        } catch (IOException ioe) {
            Logger.INSTANCE.logException(ioe, false);
        }
        
        tempDir = new File(getStringProperty("tempDir", "/tmp"));
        javaBin = getStringProperty("javaBin", "");
        checkoutDir = new File(tempDir, getUniqueIdentifier());
        
        produceXMLoutput = getBooleanProperty("xmlOutput", true);
        
        ignoreFilesPatterns = new ArrayList<Pattern>();
        int i = 0;
        String pattern = null;
        do {
            pattern = getStringProperty("ignoreRegEx." + i, null);
            if (pattern != null) {
                try {
                    Pattern p = Pattern.compile(pattern);
                    ignoreFilesPatterns.add(p);
                } catch (PatternSyntaxException e) {
                    Logger.INSTANCE.log("Invalid pattern ignoreRegeEx." + i + ": "
                            + pattern + " (" + e.getMessage() + ")");
                }
            }
            i++;
        } while (pattern != null);
        ignoreFilesPatterns = Collections.unmodifiableList(ignoreFilesPatterns);
        
        unrestrictedUsers = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(getStringProperty("unrestrictedUsers", ""), ",");
        while (tokens.hasMoreTokens()) {
            unrestrictedUsers.add(tokens.nextToken().trim());
        }
        
        readPathConfigurations();
        
        findPlugins();
    }
    
    /**
     * Reads the path configurations from the properties.
     */
    private void readPathConfigurations() {
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            key = key.toLowerCase();
            if (key.startsWith(PATH_SETTING_PROPERTY_PREFIX) && key.endsWith(".prefix")) {
                String path = (String) entry.getValue();
                String prefix = key.substring(0, key.lastIndexOf('.') + 1);
                pathConfigurations.add(new PathConfiguration(path, prefix, this));
            }
        }
    }
    
    /**
     * Converts a class name to a file name.
     * 
     * @param name the class name to be converted
     * @return the file name (without extension)
     */
    private String class2file(String name) {
        return name.replace('.', File.separatorChar);
    }

    /**
     * Converts a file name to a class name.
     * 
     * @param prefix the prefix to be removed from 
     *        <code>file</code> before doing the conversion
     * @param file the file to be converted to a class name
     * @return the class name of <code>file</code>
     */
    private String file2class(File prefix, File file) {
        String name = file.getAbsolutePath();
        if (null != prefix) {
            if (name.startsWith(prefix.getAbsolutePath())) {
                name = name.substring(prefix.getAbsolutePath().length());
            }
        }
        if (name.endsWith(".class")) {
            name = name.substring(0, name.length() - 6);
        }
        while (name.startsWith(File.separator)) {
            name = name.substring(1);
        }
        return name.replace(File.separatorChar, '.');
    }

    /**
     * Loads addition test plugins from the tests package of this hook.
     */
    private void findPlugins() {
        ClassLoader loader = Configuration.class.getClassLoader();
        String pluginPackage = Configuration.class.getPackage().getName();
        String pluginDirRest = class2file(pluginPackage);
        File plugins = new File(getHookDir(), pluginDirRest);
        if (plugins.exists()) {
            for (File f : FileUtils.listFiles(plugins, new String[] {"class"}, true)) {
                try {
                    Class.forName(file2class(getHookDir(), f), true,
                        loader);
                } catch (ClassNotFoundException exc) {
                }
            }
        } else { // load from jar in class path by determining the relevant names only
            String classpath = System.getProperty("java.class.path").replace('\\', '/');
            StringTokenizer entries = new StringTokenizer(classpath, File.pathSeparator);
            boolean pluginPathFound = false;
            while (entries.hasMoreTokens() && !pluginPathFound) {
                String entry = entries.nextToken();
                //Christopher(?): // For Windows type OS's the leading "/" must be removed
                //Christopher(?): if (System.getProperty("os.name").toLowerCase().contains("win")) {
                while (entry.startsWith("/")) {
                    entry = entry.substring(1);
                }
                //Christopher(?): }
                if (entry.endsWith(".jar")) {
                    try {
                        JarInputStream jis = new JarInputStream(new FileInputStream(entry));
                        JarEntry jEntry;
                        do {
                            jEntry = jis.getNextJarEntry();
                            if (null != jEntry && !jEntry.isDirectory()
                                && jEntry.getName().endsWith(".class")) {
                                String f = jEntry.getName();
                                f =
                                    f.substring(0, f.length() - 6).replace(
                                        '\\', '/').replace('/', '.');
                                if (f.startsWith(pluginPackage)) {
                                    //Christopher(?): Logger.INSTANCE.log("TestClass: " + f);
                                    pluginPathFound = true;
                                    try {
                                        Class.forName(f, true, loader);
                                    } catch (ClassNotFoundException exc) {
                                    }
                                }
                            }
                        } while (null != jEntry);
                        jis.close();
                    } catch (IOException exc) {
                    }
                }
            }
        }
    }
    
    /**
     * Returns the value of a named booolean property from the 
     * hook configuration. This query is not dependent on 
     * the currently processed submission task.
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     */
    public boolean getBooleanProperty(String key, boolean deflt) {
        return Boolean.valueOf(props.getProperty(key, String.valueOf(deflt))
                .toLowerCase());
    }
    
    /**
     * Returns the value of a named integer property from the 
     * hook configuration. 
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     */
    public int getIntProperty(String key, int deflt) {
        int result = deflt;
        try {
            result = Integer.parseInt(props.getProperty(key, String
                .valueOf(deflt)));
        } catch (NumberFormatException nfe) {
        }
        return result;
    }
    
    /**
     * Returns the value of a named integer property from the 
     * hook configuration. 
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     */
    public long getLongProperty(String key, long deflt) {
        long result = deflt;
        try {
            result = Long.parseLong(props.getProperty(key, String
                    .valueOf(deflt)));
        } catch (NumberFormatException nfe) {
        }
        return result;
    }
    
    /**
     * Returns whether the configuration has the given property set.
     * 
     * @param key the key of the property
     * @return <code>true</code> if the property is set/defined, 
     *     <code>false</code> else
     */
    public boolean hasProperty(String key) {
        return null != props.getProperty(key);
    }
    
    /**
     * Returns the value of a named string property from the 
     * hook configuration. This query is not dependent on 
     * the currently processed submission task.
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     */
    public String getStringProperty(String key, String deflt) {
        return props.getProperty(key, deflt);
    }
    
    /**
     * Getter for the directory where temporary files can be stored.
     * 
     * @return The absolute path to the directory.
     */
    public File getTempDir() {
        return tempDir;
    }
    
    /**
     * Overrides the temporary directory, e.g., for testing.
     * 
     * @param tempDir the new temporary directory
     */
    protected void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }
    
    /**
     * Getter for the directory where a copy of the repository files will be
     * stored while running tests.
     * 
     * @return The absolute path to the directory.
     */
    public File getCheckoutDir() {
        return checkoutDir;
    }
    
    /**
     * Getter for the {@link PrintStream} where test results will be written to.
     * 
     * @return The {@link PrintStream} for test results.
     */
    public PrintStream getTestOutputStream() {
        return testOutputStream;
    }
    
    /**
     * Setter for the {@link PrintStream} where test results will be written to.
     * 
     * @param testOutputStream The {@link PrintStream} for test results.
     */
    protected void setTestOutputStream(PrintStream testOutputStream) {
        this.testOutputStream = testOutputStream;
    }
    
    /**
     * Whether test results should be in XML format.
     * 
     * @return whether test results should be in XML format.
     */
    public boolean produceXmlOutput() {
        return produceXMLoutput;
    }
    
    /**
     * Getter for the {@link PathConfiguration}s in this configuration.
     * 
     * @return An {@link Iterator} for the {@link PathConfiguration}s.
     */
    public Iterator<PathConfiguration> pathConfigurations() {
        return pathConfigurations.iterator();
    }
    
    /**
     * Getter for a unique identifier for the transaction / commit / etc.
     * 
     * @return A unique identifier (e.g. for creating a temporary directory).
     */
    public abstract String getUniqueIdentifier();
    
    /**
     * Returns the value matching the specified <code>key</code>
     * from the test environment settings.
     *
     * @param key the key of the setting to be returned
     *
     * @return the value assigned to <code>key</code> or 
     *        <b>null</b> if no value was assigned so far
     */
    public String getTestEnvValue(TestEnvKey key) {
        String result = null;
        if (null != key) {
            result = testEnv.get(key);
        } 
        return result;
    }
    
    /**
     * Changes the value matching the specified <code>key</code>
     * from the test environment settings.
     *
     * @param key the key of the setting to be returned
     * @param value the value assigned to <code>key</code> or 
     *        <b>null</b> if no value should be assigned
     */
    public void setTestEnvValue(TestEnvKey key, String value) {
        assert null != key;
        testEnv.put(key, value);
    }
    
    /**
     * Getter for the installation path of this hook.
     * 
     * @return The path to the installation of this hook.
     */
    public File getHookDir() {
        return hookDir;
    }
    
    /**
     * Returns the directory string to the java bin folder.
     * 
     * @return the directory string
     */
    public String getJavaBin() {
        return javaBin;
    }
    
    /**
     * Returns {@code cmd} prefixed with {@link #getJavaBin()}, if necessary inserting a {@link File#separator}.
     * 
     * @param cmd the command to be prefixed
     * @return the prefixed command
     */
    public String prefixJava(String cmd) {
        String result = cmd;
        if (javaBin.length() > 0) {
            result = javaBin;
            if (!javaBin.endsWith(File.separator)) {
                result += File.separator;
            }
            result += cmd;
        }
        return result;
    }
    
    /**
     * Setter for the installation path of this hook.
     * 
     * @param hookDir The path to the installation of this hook.
     */
    protected void setHookDir(File hookDir) {
        this.hookDir = hookDir;
    }
    
    /**
     * Changes a property, e.g., for testing.
     * 
     * @param key the key
     * @param value the value
     */
    protected void setProperty(String key, String value) {
        props.setProperty(key, value);
    }
    
    /**
     * Getter for the name of the author of the commit.
     * 
     * @return The name of the author of the commit.
     */
    public String getCommitAuthor() {
        return commitAuthor;
    }

    /**
     * Setter for the name of the author of the commit.
     * 
     * @param commitAuthor The name of the author of the commit.
     */
    protected void setCommitAuthor(String commitAuthor) {
        this.commitAuthor = commitAuthor;
    }

    /**
     * Getter for the log entry of the commit.
     * 
     * @return The log entry of the commit.
     */
    public String getCommitComment() {
        return commitComment;
    }

    /**
     * Setter for the log entry of the commit.
     * 
     * @param commitComment The log entry of the commit.
     */
    protected void setCommitComment(String commitComment) {
        this.commitComment = commitComment;
    }
    
    /**
     * Getter for the list of regular expressions for files that should be ignored
     * during tests.
     * @return An unmodifiable list of regular expressions.
     */
    public List<Pattern> getIgnoreFilesPatterns() {
        return ignoreFilesPatterns;
    }
    
    /**
     * Checks whether the given user has unrestricted access (i.e. no tests are should run).
     * @param user The name of the user.
     * @return <code>true</code> if no tests should be run for this user.
     */
    public boolean isUnrestrictedUser(String user) {
        return unrestrictedUsers.contains(user);
    }
    
    /**
     * Setter for the {@link Set} of changed {@link PathConfiguration}s.
     * 
     * @param changedPathConfigurations All {@link PathConfiguration} that contain
     * files changed by the current transaction.
     */
    protected void setChangedPathConfigurations(Set<PathConfiguration> changedPathConfigurations) {
        this.changedPathConfigurations.clear();
        this.changedPathConfigurations.addAll(changedPathConfigurations);
    }
    
    /**
     * Getter for the {@link Set} of changed {@link PathConfiguration}s.
     * 
     * @return An unmodifiable {@link Set} of {@link PathConfiguration}s that
     * contain files changed by the current transaction.
     */
    protected Set<PathConfiguration> getChangedPathConfigurations() {
        return Collections.unmodifiableSet(changedPathConfigurations);
    }
    
    /**
     * Returns the commit stage.
     * 
     * @return the commit stage
     */
    public abstract Stage getStage();
    
}
