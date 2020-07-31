package de.uni_hildesheim.sse.javaSvnHooks;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

/**
 * Defines the specific configuration settings of a path/subpath in a repository.
 * These values are read from the hook configuration file according to a certain
 * key postfix value.
 * 
 * @author Adam Krafczyk
 */
public class PathConfiguration {

    public static final String PRESET_PREFIX = "preset.";
    public static final String CLASSPATH_KEY = "classpath";
    
    private String path;
    
    private String prefix;
    
    private Configuration configuration;
    
    private File workingDir;
    
    /**
     * Creates a path configuration for the given path.
     * 
     * @param path The subpath in the SVN repository.
     * @param prefix The prefix in the configuration file (e.g. config0.).
     * @param configuration The global configuration
     */
    PathConfiguration(String path, String prefix, Configuration configuration) {
        this.path = path;
        this.prefix = prefix;
        this.configuration = configuration;
    }
    
    /**
     * Returns {@code cmd} prefixed with {@link #getJavaBin()}, if necessary inserting a {@link File#separator}.
     * 
     * @param cmd the command to be prefixed
     * @return the prefixed command
     * 
     * @see Configuration#prefixJava(String)
     */
    public String prefixJava(String cmd) {
        return configuration.prefixJava(cmd);
    }

    /**
     * Getter for the subpath in the SVN repository represented by this configuration.
     * 
     * @return The subpath in the repository.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the name of the configuration as path without trailing slashes.
     * 
     * @return the name of the configuration
     */
    public String getName() {
        String result = path;
        while (result.endsWith("/")) { // SVN name, just /
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    /**
     * Getter for the global configuration.
     * 
     * @return The global configuration.
     */
    public Configuration getGlobalConfiguration() {
        return configuration;
    }
    
    /**
     * Returns the value of a named boolean property from the 
     * hook configuration. The key will be prepended by the 
     * the internally stored key postfix value.
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     * 
     * @since 1.00
     */
    public boolean getBooleanProperty(String key, boolean deflt) {
        return configuration.getBooleanProperty(prefix + key, 
            configuration.getBooleanProperty(PRESET_PREFIX + key, deflt));
    }

    /**
     * Returns the value of a named integer property from the 
     * hook configuration. The key will be prepended by the 
     * the internally stored key postfix value.
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     * 
     * @since 1.00
     */
    public int getIntProperty(String key, int deflt) {
        return configuration.getIntProperty(prefix + key, 
            configuration.getIntProperty(PRESET_PREFIX + key, deflt));
    }

    /**
     * Returns the value of a named string property from the 
     * hook configuration. The key will be prepended by the 
     * the internally stored key postfix value.
     * 
     * @param key the key of the property
     * @param deflt the default value
     * @return the value of the property and <code>deflt</code>
     *         if no key-value-mapping was found.
     * 
     * @since 1.00
     */
    public String getStringProperty(String key, String deflt) {
        return configuration.getStringProperty(prefix + key, 
            configuration.getStringProperty(PRESET_PREFIX + key, deflt));
    }
    
    /**
     * Returns the classpath assigned to the currently processed 
     * task (determined according to the submission to the repository).
     * 
     * @return the classpath of the currently processed task
     */
    public String getClasspathProperty() {
        return getStringProperty(CLASSPATH_KEY, "");
    }
    
    /**
     * Getter for the path where the files relevant for the current test execution are stored.
     * 
     * @return The path to the directory of the test files.
     */
    public File getWorkingDir() {
        return workingDir;
    }
    
    /**
     * Setter for the path where the files relevant for the current test execution are stored.
     * 
     * @param workingDir The path to the directory of the test files.
     */
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
    
    /**
     * Getter for all java files in the current workingDir.
     * 
     * @return An {@link Iterator} over all java files found in the current working dir.
     */
    public Collection<File> getJavaFiles() {
        return FileUtils.listFiles(workingDir, new String[] {"java"}, true);
    }
    
    /**
     * Getter for all files in the current workingDir.
     * 
     * @return An {@link Iterator} over all files found in the current working dir.
     */
    public Collection<File> getFiles() {
        return FileUtils.listFiles(workingDir, null, true);
    }
    
    /**
     * Changes a given filename to be relative to the temporary checkout directory.
     * This should be used for all filenames that are sent to the client.
     * 
     * @param filename The filename to be relativized.
     * @return The relativized filename
     */
    public String relativizeFileName(String filename) {
        String relativized = filename;
        if (null != workingDir) {
            URI fileUri = new File(filename).toURI();
            relativized = workingDir.toURI().relativize(fileUri).getPath();
        }
        return relativized;
    }
    
}
