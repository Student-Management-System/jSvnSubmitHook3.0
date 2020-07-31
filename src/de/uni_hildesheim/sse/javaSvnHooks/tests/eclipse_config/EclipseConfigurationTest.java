package de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Implements a configuring test for the Eclipse specific configuration 
 * files.
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration = true</code> enables 
 *      this test</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.libSubPath = </code> 
 *      specifies the sub directory/directories in the libs subdirectory
 *      of the hook install directory</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.allowAbsolute 
 *      = true|false</code> specifies the policy if absolute classpath
 *      entries are allowed  (default false)</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.checkExistance 
 *      = true|false</code> specifies the policy if every given classpath
 *      entry must exist (default true)</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.addToClasspath 
 *      = true|false</code> specifies the policy if proper libraries are
 *      automatically added to the dynamic classpath considered while
 *      compiling the sources etc. (default true)</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.considerSrcPath 
 *      = true|false</code> specifies the policy if the source path
 *      entry should be considered (default true)</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.considerBinPath 
 *      = true|false</code> specifies the policy if the binary 
 *      (destination) path entry should be considered (default true)</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.matchCase 
 *      = true|false</code> specifies the policy if matching libraries
 *      as defined below is case sensitive (default false)</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.matchLibs = </code> 
 *      may specify a comma separated list of non-qualified library 
 *      names that denote the allowed libraries which must then also
 *      be present in the libs subdirectory of the hook install directory. 
 *      If such a file is found and matched, the file in the transaction 
 *      must match the CRC checksum of the server file.</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.requiredNatures = </code> 
 *      may specify a comma separated list of fully qualified 
 *      non-qualified Eclipse nature classes that must be present
 *      in the Eclipse project file (default 
 *      <code>org.eclipse.jdt.core.javanature</code>).</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.requiredBuilders = </code> 
 *      may specify a comma separated list of fully qualified 
 *      non-qualified Eclipse builder classes that must be present
 *      in the Eclipse project file (default 
 *      <code>org.eclipse.jdt.core.javabuilder</code>).</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.eclipsePluginsDir = </code>
 *      may specify a directory relative to hook install directory
 *      or an absolute directory
 *      in which (relevant) eclipse plugins such as the PDE jars
 *      are located. If specified, automatically a plugin mapping is 
 *      constructed an can matched against an associated (in the 
 *      .classpath file) manifest file of the analyzed project.</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.
 *      container.<i>Container</i> = </code> may specify the jars of a 
 *      certain eclipse library container. If no jars are given, the 
 *      internal default values will be disabled. If the configuration
 *      keys are not given, currently the following default values
 *      will be considered:
 *      <ul>
 *       <li><code>JRE_CONTAINER</code> is resolved to all jars 
 *           included in Java (libs and subdirectories)</li>
 *       <li><code>JUNIT_CONTAINER/4</code> is resolved to 
 *           <code>org.junit4</code>, which then is resolved by 
 *           the plugins mechanism mentioned above</li>
 *      </ul></li>
 *  <li><code>nonDefaultJREmappingMode</code>=[WARNING|ERROR|NONE]
 *      (default NONE) specifies how the test should handle non-default 
 *      JRE mappings (NONE: ignore, WARNING: emit a warning, ERROR: stop 
 *      the test with an error message). 
 *      <code>requiredJREmappingRegEx.<i>Nr</i></code> is
 *      a global or configuration local regular expression defining the 
 *      proper mapping.</li>
 *  <li><code>config<i>Nr</i>.eclipseConfiguration.
 *      nonDefaultJREmappingMode</code>=[WARNING|ERROR|NONE] (default NONE)
 *      may supersede the global <code>nonDefaultJREmappingMode</code>).</li>
 * </ul>
 * Reading the configuration may change the test environment for classpath
 * binary path and source path.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
public class EclipseConfigurationTest extends AbstractJavaTest {

    /**
     * Defines the Eclipse "Require-Bundle" manifest 
     * attribute name.
     * 
     * @since 1.20
     */
    private static final java.util.jar.Attributes.Name REQUIRE_BUNDLE 
        = new java.util.jar.Attributes.Name("Require-Bundle");
    
    /**
     * Stores the test individual configuration key.
     *
     * @since 1.20
     */
    private static final String CONF_KEY = "eclipseConfiguration";
    
    /**
     * Stores the property name for a bundle version in a manifest.
     * 
     * @since 1.20
     */
    private static final String MANIFEST_PROPERTY_BUNDLE_VERSION = 
        "bundle-version";

    /**
     * Stores the delimiter for a bundle version in a manifest.
     * 
     * @since 1.20
     */
    private static final String MANIFEST_DELIMITER_BUNDLE_VERSION = "=";

    /**
     * Stores the property name for a visibility in a manifest.
     * 
     * @since 1.20
     */
    private static final String MANIFEST_PROPERTY_VISIBILITY = "visibility";

    /**
     * Stores the delimiter for a visibility in a manifest.
     * 
     * @since 1.20
     */
    private static final String MANIFEST_DELIMITER_VISIBILITY = ":=";
    
    /**
     * Defines the key for the launching JRE container.
     * 
     * @since 1.20
     */
    private static final String CON_LAUNCHING_JRE 
        = "org.eclipse.jdt.launching.JRE_CONTAINER";

    @Override
    public String getName() {
        return "Checking and considering Eclipse configuration files";
    }
    
    @Override
    public int execute(PathConfiguration pathConfiguration) {
        File workingDir = pathConfiguration.getWorkingDir();
        int result = readConfigurationFile(
            new File(workingDir, ".classpath"), 
            pathConfiguration, 
            new EclipseClasspathHandler(pathConfiguration));
        if (0 == result) {
            result = readConfigurationFile(
                new File(workingDir, ".project"), 
                pathConfiguration, 
                new EclipseProjectHandler(pathConfiguration));
        }
        if (0 == result) {
            if (!pathConfiguration.getBooleanProperty(
                CONF_KEY + ".allowMultipleProjects", false)) {
                Map<String, Object> projectMap = new HashMap<String, Object>();
                
                Iterator<File> files = pathConfiguration.getFiles().iterator();
                while (files.hasNext()) {
                    File file = files.next();
                    if (file.getName().equals(".project")) {
                        projectMap.put(file.toString(), null);
                    }
                }
                if (projectMap.size() > 1) {
                    emitError(pathConfiguration.getGlobalConfiguration(), 
                        "Subprojects are not allowed.");
                    result = 1;
                }
            }
        }
//        pathConfiguration.reviewPaths(); TODO
        return result;
    }

    /**
     * Reads an Eclipse XML configuration file.
     * 
     * @param file the file to be read
     * @param pathConfiguration the path configuration to be considered
     * @param handler the handler to be used for reading the configuration
     * @return the result from the handler or from searching for the file
     * 
     * @since 1.20
     */
    private int readConfigurationFile(File file, 
        PathConfiguration pathConfiguration, EclipseHandler handler) {
        int result = 0;
        if (file.exists()) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();
                parser.parse(new FileInputStream(file), handler);
            } catch (SAXException | ParserConfigurationException | IOException e) {
                handler.emitError("Error while processing Eclipse " 
                    + "configuration: " + file.getName());
                Logger.INSTANCE.logException(e, false);
            }
            result = handler.getResult();
        } else {
            handler.emitError("Eclipse configuration not present: " 
                + file.getName()); 
            result = 1;
        }
        return result;
    }
    
    /**
     * Emits an (XML) error message.
     *
     * @param configuration the configuration instance
     * @param message the message to be print
     *
     * @since 1.20
     */
    private void emitError(Configuration configuration, String message) {
        if (configuration.produceXmlOutput()) {
            configuration.getTestOutputStream().println(
                "  <message tool=\"" + XmlUtilities.xmlify("eclipse configuration") 
                + "\" type=\"error\" file=\"\" line=\"\" "
                + "message=\"" + XmlUtilities.xmlify(message) + "\"/>");
        } else {
            configuration.getTestOutputStream().println(message);
        }
    }
    
    /**
     * Defines an abstract handler class for Eclipse 
     * configuration files.
     * 
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    private abstract class EclipseHandler extends DefaultHandler {

        /**
         * Stores the path configuration instance to be considered.
         * 
         * @since 1.20
         */
        protected PathConfiguration pathConfiguration;

        /**
         * Stores the overall configuration instance to be considered.
         * 
         * @since 1.20
         */
        protected Configuration configuration;
        
        /**
         * Stores the current result in processing the Eclipse configuration.
         * 
         * @since 1.20
         */
        protected int result = 0;

        /**
         * Creates a new handler.
         * 
         * @param pathConfiguration the configuration instance to be 
         *        considered
         * 
         * @since 1.20
         */ 
        public EclipseHandler(PathConfiguration pathConfiguration) {
            this.pathConfiguration = pathConfiguration;
            this.configuration = pathConfiguration.getGlobalConfiguration();
        }

        /**
         * Emits an (XML) error message.
         *
         * @param message the message to be print
         *
         * @since 1.20
         */
        public void emitError(String message) {
            EclipseConfigurationTest.this.emitError(configuration, message);
            result = 1;
        }

        /**
         * Returns the result value of analyzing the eclipse configuration.
         * 
         * @return the operating system shell return code (0 denotes
         *         success)
         * 
         * @since 1.20
         */
        public int getResult() {
            return result;
        }
        
    }

    /**
     * Implements a XML handler for reading and checking the Eclipse
     * classpath file.
     *
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.21
     */
    private class EclipseClasspathHandler extends EclipseHandler {

        /**
         * Stores the filesum check utility instance.
         * 
         * @since 1.20
         */
        private FileChecksumUtil checksumUtil = new FileChecksumUtil();

        /**
         * Indicates that also the manifest should be taken into account.
         * 
         * @since 1.20
         */
        private boolean loadManifest = false;
        
        /**
         * Stores if matching libs must also match cases.
         * 
         * @since 1.20
         */
        private boolean matchCase = false;
        
        /**
         * Stores the contained non-default JRE mappings.
         * 
         * @since 1.20
         */
        private String containsNonDefaultJREmapping = "";
        
        /**
         * Stores unmatched classpath entries. Map should be empty 
         * at the end. Stores absolute file name of path entry related
         * to the configuration path entry. (temporary data).
         * 
         * @since 1.21
         */
        private Map<String, String> unmatchedClassPathEntries = 
            new HashMap<String, String>();

        /**
         * Stores the relations to the locally stored libs.
         * If not <b>null</b> each lib in the eclipse configuration
         * must have a matching library on the server. 
         * 
         * @since 1.20
         */
        private Map<String, File> matchingLibs = null;

        /**
         * Stores the plugin prefix-plugin files mapping.
         * 
         * @since 1.20
         */
        private Map<String, PluginFileInfo> pluginMapping = null;

        /**
         * Stores the container-to-plugin mapping.
         * 
         * @since 1.20
         */
        private final Map<String, String> defaultContainerPlugin 
            = new HashMap<String, String>();
        
        /**
         * Creates a new handler.
         * 
         * @param pathConfiguration the configuration instance to be 
         *        considered
         * 
         * @since 1.20
         */ 
        public EclipseClasspathHandler(PathConfiguration pathConfiguration) {
            super(pathConfiguration);
            String match = pathConfiguration.getStringProperty(
                CONF_KEY + ".matchLibs", null);
            matchCase = pathConfiguration.getBooleanProperty(
                CONF_KEY + ".matchCase", false);
            if (null != match) {
                String subDir = pathConfiguration.getStringProperty(
                    CONF_KEY + ".libSubPath", "");
                if (subDir.length() > 0) {
                    subDir = "libs" + File.separator + subDir;
                } else {
                    subDir = "libs";
                }
                
                File libsDir = new File(configuration.getHookDir(), subDir);
                matchingLibs = new HashMap<String, File>();
                StringTokenizer libs = new StringTokenizer(match, ",");
                while (libs.hasMoreTokens()) {
                    String lib = libs.nextToken();
                    File libFile = new File(libsDir, lib);
                    if (!libFile.exists()) {
                        libFile = null;
                    }
                    matchingLibs.put(handleMatchCase(lib), libFile);
                }
            }

            String eclipsePluginsDir = pathConfiguration.getStringProperty(
                CONF_KEY + ".eclipsePluginsDir", "");
            if (eclipsePluginsDir.length() > 0) {
                File eclipsePlugins = new File(eclipsePluginsDir);
                if (!eclipsePlugins.isAbsolute()) {
                    eclipsePlugins = new File(configuration.getHookDir(), 
                        eclipsePluginsDir);
                }
                if (eclipsePlugins.exists() && eclipsePlugins.isDirectory()) {
                    pluginMapping = constructPluginMapping(eclipsePlugins);
                }
            }

            // superfluous
            defaultContainerPlugin.put(
                CON_LAUNCHING_JRE, 
                "");
            
            defaultContainerPlugin.put(
                "org.eclipse.jdt.junit.JUNIT_CONTAINER/4", 
                "org.junit4");
        }
        
        /**
         * Tries to read out the configuration related path for 
         * <code>container</code> or tries to construct/find the 
         * related jars.
         * 
         * @param container the Eclipse container so search for
         * @return the names of the Jars or an empty string
         * 
         * @since 1.20
         */
        private String retrieveContainerMapping(String container) {
            String result = "";
            String postfix = "";
            
            int pos = container.lastIndexOf(".");
            if (pos >= 0) {
                postfix = container.substring(pos);
            }
            if (postfix.length() > 0) {
                result = pathConfiguration.getStringProperty(
                    CONF_KEY + ".container" + postfix, null);
                if (null == result) {
                    if (container.startsWith(CON_LAUNCHING_JRE)) {
                        checkJREcontainer(container);
                        result = getJreContainerMapping();
                    } else {
                        String pluginName = 
                            defaultContainerPlugin.get(container);
                        if (null != pluginMapping && null != pluginName) {
                            PluginFileInfo info = pluginMapping.get(pluginName);
                            result = null != info ? info.getClassPathPart() : null;
                        }
                    }
                }
            }
            if (null == result) {
                result = "";
            }
            return result;
        }
        
        /**
         * Checks the JRE container if it is proper.
         * 
         * @param container the container name
         * 
         * @since 1.20
         */
        private void checkJREcontainer(String container) {
            if (container.length() > CON_LAUNCHING_JRE.length()) {
                String requiredContainer = null;
                int number = 1;
                do {
                    requiredContainer = pathConfiguration.getStringProperty(
                        CONF_KEY + ".requiredJREmappingRegEx." + number, null);
                    if (null == requiredContainer) {
                        requiredContainer = configuration.getStringProperty(
                            "requiredJREmappingRegEx." + number, null);
                    }
                    try {
                        if (null != requiredContainer 
                            && !container.matches(requiredContainer)) {
                            if (containsNonDefaultJREmapping.length() > 0) {
                                containsNonDefaultJREmapping += ", ";
                            }
                            containsNonDefaultJREmapping 
                                += container.substring(
                                    CON_LAUNCHING_JRE.length() + 1);
                        }
                    } catch (PatternSyntaxException exc) {
                        Logger.INSTANCE.log("[Warning] Container mapping pattern: "
                            + exc.getMessage());
                    }
                    number++;
                } while (null != requiredContainer);
            }
        }
      
        /**
         * Returns the default JRE container mapping according to
         * the library path of the JRE running this program.
         * 
         * @return the default JRE container mapping
         * 
         * @since 1.20
         */
        private String getJreContainerMapping() {
            StringBuilder buf = new StringBuilder();
            String libPathString = System.getProperty("sun.boot.library.path");
            if (null != libPathString) {
                File path = new File(libPathString);
                libPathString = path.getAbsolutePath();
                if (libPathString.endsWith(File.separator + "bin")) {
                    path = new File(path.getParent(), "lib");
                }
                appendJarToBuf(path, true, buf);
            }
            return buf.toString();
        }
        
        /**
         * Appends all jars in the given <code>path</code> to <code>buf</code>.
         * 
         * @param path the path to be searched for jars
         * @param firstLevel if rt.jar should be added or all jars 
         *        should be considered
         * @param buf the buffer to be modified as a side effect
         * 
         * @since 1.00
         */
        private void appendJarToBuf(File path, boolean firstLevel, 
            StringBuilder buf) {
            File[] files = path.listFiles();
            if (null != files) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        appendJarToBuf(f, false, buf);
                    } else {
                        if (f.getName().endsWith(".jar") && (!firstLevel 
                            || (firstLevel && !f.getName().equals("rt.jar")))) {
                            if (buf.length() > 0) {
                                buf.append(File.pathSeparator);
                            }
                            buf.append("\"" + f.getAbsolutePath() + "\"");
                        }
                    }
                }
            }
            
        }
        
        /**
         * May convert <code>text</code> to prepare the matching
         * according to the matching policy.
         * 
         * @param text the text to be prepared
         * 
         * @return <code>text</code> if case sensitive matching is
         *         enabled, the lowercase version of <code>text</code> 
         *         otherways
         * 
         * @since 1.20
         */
        private String handleMatchCase(String text) {
            String result = text;
            if (!matchCase) {
                text = text.toLowerCase();
            }
            return result;
        }

        /**
         * Is called when an element is opened.
         * 
         * @param uri
         *            the namespace uri
         * @param localName
         *            the local name of the element
         * @param qName
         *            the qualified name of the element
         * @param atts
         *            the attributes of this element
         * 
         * @throws SAXException
         *             if any exception occurs
         * 
         * @since 1.20
         */
        public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            if (0 == result && qName.equals("classpathentry")) {
                String kind = atts.getValue("kind");
                String path = atts.getValue("path");
                String combineAccessrules = atts.getValue("combineaccessrules");
                if (path.length() > 0) {
                    File f = new File(path);
                    File absoluteF;
                    if (f.isAbsolute()) {
                        absoluteF = f;
                    } else {
                        absoluteF = new File(new File(configuration.getCheckoutDir(), pathConfiguration.getPath()), 
                            path);
                    }
                    if ("con".equals(kind)) {
                        handleCon(path);
                    } else if ("lib".equals(kind)) {
                        handleLib(path, f, absoluteF);
                    } else if ("src".equals(kind) 
                        && null == combineAccessrules) {
                        handleSrc(path, f);
                    } else if ("output".equals(kind)) {
                        handleOutput(path, f, absoluteF);
                    }
                }
            } 
        }

        /**
         * Called from startElement when kind is "output".
         * 
         * @param path The path.
         * @param file A file object representing the path.
         * @param absoluteF A file object representing the absolute path.
         */
        private void handleOutput(String path, File file, File absoluteF) {
            if (pathConfiguration.getBooleanProperty(
                CONF_KEY + ".considerBinPath", true)) {
                if (file.isAbsolute()) {
                    emitError("Absolute output path not allowed."); 
                } else {
                    unmatchedClassPathEntries.remove(
                        absoluteF.getAbsolutePath());
                    configuration.setTestEnvValue(
                        TestEnvKey.BIN, path);
                }
            } else if (path.length() > 0) {
                emitError("Output path (usually called bin) is " 
                    + "not allowed."); 
            }
        }

        /**
         * Called from startElement when kind is "src".
         * 
         * @param path The path.
         * @param file A file object representing the path.
         */
        private void handleSrc(String path, File file) {
            if (pathConfiguration.getBooleanProperty(
                CONF_KEY + ".considerSrcPath", true)) {
                if (file.isAbsolute()) {
                    emitError("Absolute source path not allowed."); 
                } else {
                    configuration.setTestEnvValue(
                        TestEnvKey.SRC, path);
                }
            } else if (path.length() > 0) {
                emitError("Output path (usually called src or bin) is " 
                    + "not allowed."); 
            }
        }

        /**
         * Called from startElement when kind is "lib".
         * 
         * @param path The path.
         * @param file A file object representing the path.
         * @param absoluteF A file object representing the absolute path.
         */
        private void handleLib(String path, File file, File absoluteF) {
            if (file.isAbsolute()) {
                if (!pathConfiguration.getBooleanProperty(
                    CONF_KEY + ".allowAbsolute", false)) {
                    emitError("Absolute classpath entry " 
                        + path
                        + " not allowed."); 
                }
            } else {
                file = new File(pathConfiguration.getWorkingDir(), path);
            }
            if (0 == result && !file.exists()) {
                if (pathConfiguration.getBooleanProperty(
                    CONF_KEY + ".checkExistance", true)) {
                    unmatchedClassPathEntries.put(
                        absoluteF.getAbsolutePath(), path);
                }
            }
            if (0 == result && null != matchingLibs) {
                checkJarVersion(path, file);
            }
            if (0 == result && pathConfiguration.getBooleanProperty(
                    CONF_KEY + ".addToClasspath", true)) {
                addToClassPath(configuration, 
                    file.getAbsolutePath());
            }
        }

        /**
         * Called from startElement when kind is "con".
         * 
         * @param path The path.
         */
        private void handleCon(String path) {
            if ("org.eclipse.pde.core.requiredPlugins"
                .equals(path)) {
                loadManifest = true;
            } else {
                String param = retrieveContainerMapping(path);
                if (null != param && param.length() > 0) {
                    addToClassPath(configuration, param);
                }
            }
        }

        /**
         * Checks the given jars for version equality.
         * 
         * @param path the path of the file to be tested as in the 
         *        Eclipse configuration
         * @param testFile the location of the file to be tested
         * 
         * @since 1.20
         */
        private void checkJarVersion(String path, File testFile) {
            File libFile = matchingLibs.get(
                handleMatchCase(testFile.getName()));
            if (null != libFile) {
                if (!checksumUtil.equalByCheckSum(configuration, 
                    testFile, libFile)) {
                    emitError("Library " + path + " does not"
                        + " fit to the server library version "
                        + "(checksum)."); 
                }
            } else {
                emitError("Library " + path + " does not "
                    + "have a matching library on the "
                    + "server."); 
            }
        }
        
        /**
         * Returns the result value of analyzing the eclipse configuration.
         * 
         * @return the operating system shell return code (0 denotes
         *         success)
         * 
         * @since 1.20
         */
        public int getResult() {
            if (!unmatchedClassPathEntries.isEmpty()) {
                for (String path : unmatchedClassPathEntries.values()) {
                    emitError("Classpath entry " + path 
                        + " does not exist."); 
                }
            }
            if (0 == result && loadManifest) {
                File homeDir = pathConfiguration.getWorkingDir();
                File manifestFile = constructManifestPath(homeDir);
                if (manifestFile.exists() && null != pluginMapping) {
                    try {
                        Manifest manifest = new Manifest(
                            new FileInputStream(manifestFile));
                        analyzeManifest(manifest, pluginMapping, 
                            configuration, true);
                    } catch (IOException ioe) {
                    }
                }
            }
            int result = super.getResult();
            if (containsNonDefaultJREmapping.length() > 0) {
                ConfigLevel nonDefaultJREmode = getConfigLevelProperty(
                    pathConfiguration, CONF_KEY + ".nonDefaultJREmappingMode", null);
                if (null == nonDefaultJREmode) {
                    nonDefaultJREmode = getConfigLevelProperty(configuration, "nonDefaultJREmappingMode", 
                        ConfigLevel.NONE);
                }
                String msg = "Contains non-default JRE container mappings: " 
                    + containsNonDefaultJREmapping;
                if (ConfigLevel.WARNING == nonDefaultJREmode) {
                    Logger.INSTANCE.log("[Warning] " + msg);
                } else if (ConfigLevel.ERROR == nonDefaultJREmode) {
                    emitError(msg); 
                    result = 1;
                }
            }
            return result;
        }
        
        /**
         * Returns the value of a named level property from the 
         * hook configuration. This query is not dependent on 
         * the currently processed submission task.
         * 
         * @param config The configuration to read the property from.
         * @param key the key of the property
         * @param deflt the default value
         * @return the value of the property and <code>deflt</code>
         *         if no key-value-mapping was found.
         */
        private ConfigLevel getConfigLevelProperty(Configuration config, String key, ConfigLevel deflt) {
            ConfigLevel result = deflt;
            String value = config.getStringProperty(key, null);
            if (null != value) {
                result = ConfigLevel.valueOf(value);
            }
            return result;
        }
        
        /**
         * Returns the value of a named level property from the 
         * hook configuration. This query is not dependent on 
         * the currently processed submission task.
         * 
         * @param pc The {@link PathConfiguration} to read the property from
         * @param key the key of the property
         * @param deflt the default value
         * @return the value of the property and <code>deflt</code>
         *         if no key-value-mapping was found.
         */
        public ConfigLevel getConfigLevelProperty(PathConfiguration pc, String key, ConfigLevel deflt) {
            ConfigLevel result = deflt;
            String value = pc.getStringProperty(key, null);
            if (null != value) {
                result = ConfigLevel.valueOf(value);
            }
            return result;
        }

    }
    
    /**
     * Constructs the default manifest path contained in 
     * <code>dir</code>.
     * 
     * @param dir the directory that is used as base path for
     *        the manifest
     * @return the manifest path
     * 
     * @since 1.20
     */
    private static final File constructManifestPath(File dir) {
        return new File(dir, "META-INF" + File.separator + "MANIFEST.MF");
    }
    
    /**
     * Returns the string tail after the given delimiter.
     * 
     * @param text the text containing the tail
     * @param delimiter the delimiter to be searched for
     * @return the text after the delimiter or an empty string
     * 
     * @since 1.20
     */
    private static final String tailAfter(String text, String delimiter) {
        String result = "";
        int pos = text.indexOf(delimiter);
        if (pos > 0) {
            result = text.substring(pos + delimiter.length()).trim();
        }
        return result;
    }
    
    /**
     * Adds the specified path to the global dynamic test environment.
     * 
     * @param configuration the currently relevant global configuration
     * @param path the path to be added
     * 
     * @since 1.20
     */
    private static void addToClassPath(Configuration configuration, 
        String path) {
        String classpath = configuration.getTestEnvValue(
            AbstractJavaTest.TestEnvKey.CLASSPATH);
        if (null == classpath) {
            classpath = "";
        }
        if (path.indexOf(" ") > 0 && !path.startsWith("\"") 
            && !path.endsWith("\"")) {
            path = "\"" + path + "\"";
        }
        classpath = appendToClasspath(classpath, path);
        configuration.setTestEnvValue(
            AbstractJavaTest.TestEnvKey.CLASSPATH, classpath);
    }
    
    /**
     * Constructs the plugin mapping from the specified directory.
     * 
     * @param dir the directory to be analyzed for eclipse plugins
     * @return <code>null</code> if no mapping was constructed, 
     *         the mapping otherwise
     * 
     * @since 1.20
     */
    private static Map<String, PluginFileInfo> constructPluginMapping(
        File dir) {
        File[] files = dir.listFiles();
        Map<String, PluginFileInfo> pluginMapping = null;
        if (null != files) {
            pluginMapping = new HashMap<String, PluginFileInfo>();
            for (File f : files) {
                String name = f.getName();
                int pos = name.indexOf('_');
                if (pos > 0) {
                    String prefix = name.substring(0, pos);
                    PluginFileInfo yetMapped = pluginMapping.get(prefix);
                    if (null == yetMapped 
                        || yetMapped.lastModified() < f.lastModified()) {
                        pluginMapping.put(prefix, new PluginFileInfo(f));
                    }
                }
            }
            for (Iterator<PluginFileInfo> iter = 
                    pluginMapping.values().iterator(); iter.hasNext();) {
                iter.next().initialize();
            }
        }
        return pluginMapping;
    }

    /**
     * Analyzes the manifest in <code>inStream</code>. This method 
     * will analyze required, not resolved plugins recursively.
     * 
     * @param manifest the manifest to be analyzed
     * @param pluginMapping the plugin mapping
     * @param configuration the current program configuration (may 
     *        be <b>null</b> if related plugin jars should be printed
     *        to the standard output stream only)
     * @param initial if <code>true</code> all referenced plugins will
     *        be resolved, if <code>false</code> only reexported plugins
     *        will be resolved
     * @throws IOException in the case of any I/O related error
     * 
     * @since 1.20
     */
    private static void analyzeManifest(Manifest manifest, 
        Map<String, PluginFileInfo> pluginMapping, 
        Configuration configuration, boolean initial) throws IOException {
        java.util.jar.Attributes attributes = 
            manifest.getMainAttributes();
        if (null != attributes 
            && attributes.containsKey(REQUIRE_BUNDLE)) {
            Object value = attributes.get(REQUIRE_BUNDLE);
            // tokenize the string into individual libs with attributes
            ManifestTokenizer libs = 
                new ManifestTokenizer(value.toString(), ",");
            while (libs.hasMoreTokens()) {
                String libString = libs.nextToken();

                // tokenize the library string into attributes
                ManifestTokenizer libData = 
                    new ManifestTokenizer(libString, ";");
                int pos = 0;
                String libName = "";
                //String requiredVersion = "";
                @SuppressWarnings("unused")
                String visibility = "";
                while (libData.hasMoreTokens()) {
                    String text = libData.nextToken();
                    if (0 == pos) {
                        libName = text;
                    } else if (text.startsWith(
                        MANIFEST_PROPERTY_BUNDLE_VERSION)) {
                        /*requiredVersion =*/ tailAfter(text, 
                                                  MANIFEST_DELIMITER_BUNDLE_VERSION);
                    } else if (text.startsWith(
                        MANIFEST_PROPERTY_VISIBILITY)) {
                        visibility = tailAfter(text, 
                            MANIFEST_DELIMITER_VISIBILITY);
                    }
                    pos++;
                }
                //if (initial || visibility.equals("reexport"))
                PluginFileInfo info = pluginMapping.get(libName);
                if (null != info && !info.isResolved()) {
                    info.addToClassPath(configuration);
                    info.setResolved();
                    Manifest inputStream = info.getManifest();
                    if (null != inputStream) {
                        analyzeManifest(inputStream, pluginMapping, 
                            configuration, false);
                    }
                }
            }
        }
    }
    
    /**
     * A specialized tokenizer that recognizes quoted parts.
     * 
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    private static class ManifestTokenizer {
        
        /**
         * Stores the text to be tokenized.
         * 
         * @since 1.2
         */
        private String text;
        
        /**
         * Stores the delimiter string.
         * 
         * @since 1.20
         */
        private String delimiter;
        
        /**
         * Stores the current position within {@link #text}.
         * 
         * @since 1.20
         */
        private int pos = 0;
        
        /**
         * Creates a new manifest tokenizer.
         * 
         * @param text the text to be tokenized
         * @param delimiter the delimiter separating tokens
         * 
         * @since 1.20
         */
        public ManifestTokenizer(String text, String delimiter) {
            this.text = text;
            this.delimiter = delimiter;
        }
        
        /**
         * Returns if there are more tokens at the current position.
         * 
         * @return <code>true</code> if there are more tokens, 
         *         <code>false</code> else
         * 
         * @since 1.20
         */
        public boolean hasMoreTokens() {
            return pos < text.length();
        }
        
        /**
         * Returns the next token, only if {@link #hasMoreTokens()}
         * has responded <code>true</code> before.
         * 
         * @return the next token
         * 
         * @since 1.20
         */
        public String nextToken() {
            int start = pos;
            boolean quoted = false;
            boolean found = false;
            while (pos < text.length() && !found) {
                if (text.charAt(pos) == '"') {
                    quoted = !quoted;
                }
                found = !quoted && matchDelimiter();
                if (!found) {
                    pos++;
                }
            }
            String result = text.substring(start, pos);
            if (found) {
                pos += delimiter.length();
            }
            return result; 
        }
        
        /**
         * Matches the current position the delimiter?
         * 
         * @return <code>true</code> if the current position
         *         matches the delimiter, <code>false</code> else
         * 
         * @since 1.20
         */
        private boolean matchDelimiter() {
            boolean match = true;
            boolean oneIteration = false;
            for (int i = 0; pos + i < text.length() 
                && i < delimiter.length(); i++) {
                oneIteration = true;
                match = text.charAt(pos + i) == delimiter.charAt(i);
            }
            return oneIteration && match;
        }
    }

    /*
    public static void main(String[] args) throws IOException {
        Map<String, PluginFileInfo> mapping = 
            constructPluginMapping(new File(
            "C:\\localUserFiles\\eichelberger\\eclipse331\\eclipse\\plugins"));
        File manifest = new File("C:\\localUserFiles\\eichelberger\\"
            + "javaWorkSpace\\ReToolEclipsePlugin\\META-INF\\MANIFEST.MF");
        analyzeManifest(new Manifest(new FileInputStream(manifest)), 
            mapping, null, true);
    }
    */
    
    /**
     * Information about an eclipse plugin. Due to performance
     * reason, first this instance is created and when all actual
     * plugins are loaded, all instances of this class must be
     * initialized by calling {@link #initialize()}.
     * 
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    private static class PluginFileInfo {
        
        /**
         * Stores all jars related to this plugin.
         * 
         * @since 1.20
         */
        private List<File> jars = new ArrayList<File>();
        
        /**
         * Stores the original file from which this instance
         * was created.
         * 
         * @since 1.20
         */
        private File file;
        
        /**
         * Stores if this plugin and its dependent plugins has 
         * been resolved.
         * 
         * @since 1.20
         */
        private transient boolean resolved = false;

        /**
         * Creates the plugin information instance. Note, that
         * {@link #initialize()} must be called afterwards.
         * 
         * @param file the jar file or directory denoting the plugin 
         *        itself. In the case of a directory, 
         *        {@link #initialize()} will crawl for contained
         *        jars
         * 
         * @since 1.20
         */
        public PluginFileInfo(File file) {
            this.file = file;
        }
        
        /**
         * Initializes this instance by adding all relevant jars to the
         * internal jar list.
         * 
         * @since 1.20
         */
        public void initialize() {
            if (file.isDirectory()) {
                jars.addAll(FileUtils.listFiles(file, new String[] {"jar"}, true));
            } else {
                jars.add(file);
                if (file.getName().startsWith("org.eclipse.swt_")) {
                    String swtName = file.getName();
                    int pos = swtName.lastIndexOf('.');
                    if (pos > 0) {
                        swtName = swtName.substring(0, pos);
                    }
                    pos = swtName.lastIndexOf('.');
                    if (pos > 0) {
                        swtName = swtName.substring(pos);
                    }
                    while (swtName.length() > 1 
                        && !Character.isDigit(swtName.charAt(0))) {
                        swtName = swtName.substring(1);
                    }
                    while (swtName.length() > 1 
                        && !Character.isDigit(swtName.charAt(
                            swtName.length() - 1))) {
                        swtName = swtName.substring(0, swtName.length() - 1);
                    }
                    File parent = file.getParentFile();
                    File[] files = parent.listFiles();
                    for (File f : files) {
                        if (f.getName().startsWith("org.eclipse.swt.") 
                            && f.getName().contains(swtName)) {
                            jars.add(f);
                        }
                    }
                }
            }
        }

        /**
         * Returns the time when {@link #file} was modified last.
         * 
         * @return A long value representing the time the file was last 
         *         modified, measured in milliseconds since the epoch 
         *         (00:00:00 GMT, January 1, 1970), or 0L if the file 
         *         does not exist or if an I/O error occurs
         * 
         * @since 1.20
         */
        public long lastModified() {
            return file.lastModified();
        }
        
        /**
         * Adds all related jar files to the dynamic classpath by calling 
         * {@link EclipseConfigurationTest#addToClassPath(
         * Configuration, String)}.
         * 
         * @param configuration the global configuration containing the dynamic
         *        test environment data (if <b>null</b> the relevant files
         *        will only be printed to the standard output stream
         * 
         * @since 1.20
         */
        public void addToClassPath(Configuration configuration) {
            for (File f : jars) {
                if (null != configuration) {                        
                    EclipseConfigurationTest.addToClassPath(
                        configuration, f.getAbsolutePath());
                }
            }
        }
        
        /**
         * Returns the class path part resulting from this object.
         * 
         * @return the class path part 
         * 
         * @since 1.20
         */
        public String getClassPathPart() {
            StringBuilder buf = new StringBuilder();
            for (File f : jars) {
                if (buf.length() > 0) {
                    buf.append(File.pathSeparator);
                }
                buf.append("\"" + f.getAbsolutePath() + "\"");
            }
            return buf.toString();
        }
        
        /**
         * Returns if this plugin is resolved.
         * 
         * @return <code>true</code> if this plugin is
         *         considered as resolved, <code>false</code>
         *         else
         * 
         * @since 1.20
         */
        public boolean isResolved() {
            return resolved;
        }
        
        /**
         * Marks this plugin as resolved.
         * 
         * @since 1.20
         */
        public void setResolved() {
            resolved = true;
        }
        
        /**
         * Returns a stream containing the manifest of this plugin.
         * 
         * @return a steam containing the manifest or <b>null</b> if 
         *         none was found
         * @throws IOException if an I/O related error occurred
         * 
         * @since 1.20
         */
        public Manifest getManifest() throws IOException {
            Manifest manifest = null;
            if (file.isDirectory()) {
                File manifestFile = constructManifestPath(file);
                if (manifestFile.exists() && manifestFile.isFile()) {
                    manifest = new Manifest(new FileInputStream(manifestFile));
                }
            } else {
                JarInputStream jis =
                    new JarInputStream(new FileInputStream(file));
                manifest = jis.getManifest();
                jis.close();
            }
            return manifest;
        }
        
    }
    
    /**
     * Implements a XML handler for reading and checking the Eclipse
     * project file.
     *
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    private class EclipseProjectHandler extends EclipseHandler {

        /**
         * Stores the contents of the last unfinished element (sufficient
         * for this application, does not need to be a stack).
         * 
         * @since 1.20
         */
        private StringBuilder elementContents = new StringBuilder();
        
        /**
         * Stores the fully qualified name of the current element.
         * 
         * @since 1.20
         */
        private StringBuffer fqn = new StringBuffer(); 

        /**
         * Stores the string version of {@link #fqn} to gain a bit 
         * performance.
         * 
         * @since 1.20
         */
        private String fqnString = "";
        
        /**
         * Stores all natures required for a project to be committed properly.
         * 
         *  @since 1.20
         */
        private Map<String, Object> requiredNatures = 
            new HashMap<String, Object>();

        /**
         * Stores all builders required for a project to be committed properly.
         * 
         *  @since 1.20
         */
        private Map<String, Object> requiredBuilders = 
            new HashMap<String, Object>();
        
        /**
         * Creates a new project file handler.
         * 
         * @param pathConfiguration the configuration instance to be 
         *        considered
         * 
         * @since 1.20
         */
        public EclipseProjectHandler(PathConfiguration pathConfiguration) {
            super(pathConfiguration);
            readToMap(requiredNatures, CONF_KEY + ".requiredNatures", 
                "org.eclipse.jdt.core.javanature");
            readToMap(requiredBuilders, CONF_KEY + ".requiredBuilders", 
                "org.eclipse.jdt.core.javabuilder");
        }
        
        /**
         * Reads the configuration specified by the configuration 
         * <code>key</code> to the given <code>map</code>. 
         * <code>key</code> denotes a comma separated list of values to
         * be stored as keys in <code>map</code>.
         * 
         * @param map the map to be modified as a side effect 
         * @param key the configuration key from which the keys are loaded
         * @param deflt the default value if the key was not specified in
         *        the configuration
         * 
         * @since 1.20
         */
        private void readToMap(Map<String, Object> map, String key, 
            String deflt) {
            StringTokenizer tokens = new StringTokenizer(
                pathConfiguration.getStringProperty(
                    key, deflt), ",");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                map.put(token, null);
            }

        }
        
        /**
         * Is called when an element is opened.
         * 
         * @param uri
         *            the namespace uri
         * @param localName
         *            the local name of the element
         * @param qName
         *            the qualified name of the element
         * @param atts
         *            the attributes of this element
         * 
         * @throws SAXException
         *             if any exception occurs
         * 
         * @since 1.20
         */
        public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            if (fqn.length() > 0) {
                fqn.append(".");
            }
            fqn.append(qName);
            fqnString = fqn.toString();
        }
            
        /**
         * Receive notification of the end of an element.
         *
         * <p>By default, do nothing.  Application writers may override this
         * method in a subclass to take specific actions at the end of
         * each element (such as finalising a tree node or writing
         * output to a file).</p>
         *
         * @param uri The Namespace URI, or the empty string if the
         *        element has no Namespace URI or if Namespace
         *        processing is not being performed.
         * @param localName The local name (without prefix), or the
         *        empty string if Namespace processing is not being
         *        performed.
         * @param qName The qualified name (with prefix), or the
         *        empty string if qualified names are not available.
         * @exception org.xml.sax.SAXException Any SAX exception, possibly
         *            wrapping another exception.
         * @see org.xml.sax.ContentHandler#endElement
         * 
         * @since 1.20
         */
        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (fqnString.equals(
                "projectDescription.buildSpec.buildCommand.name")) {
                requiredBuilders.remove(elementContents.toString().trim());
            }
            if (fqnString.equals("projectDescription.natures.nature")) {
                requiredNatures.remove(elementContents.toString().trim());
            }
            elementContents.delete(0, elementContents.length());
            int pos = fqn.lastIndexOf(".");
            if (pos > 0) {
                fqn.delete(pos, fqn.length());
            } else {
                fqn.delete(0, fqn.length());
            }
            fqnString = fqn.toString();
        }
        
        /**
         * Reads arbitrary text data.
         * 
         * @param ch
         *            the character buffer filled with text data
         * @param start
         *            the valid start position in <code>ch</code>
         * @param length
         *            the length of valid data in <code>ch</code>
         * 
         * @throws SAXException
         *             if any exception occurs
         * 
         * @since 1.20
         */
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            String s = new String(ch, start, length);
            int len;
            do {
                len = s.length();
                while (s.startsWith("\n")) {
                    s = s.substring(1);
                }
                while (s.startsWith("\r")) {
                    s = s.substring(1);
                }
            } while (len != s.length());
            do {
                len = s.length();
                while (s.endsWith("\n")) {
                    s = s.substring(0, s.length() - 1);
                }
                while (s.startsWith("\r")) {
                    s = s.substring(0, s.length() - 1);
                }
            } while (len != s.length());
            s = s.trim();
            if (s.length() > 0) {
                elementContents.append(s);
            }
        }
        
        /**
         * Returns the result value of analyzing the eclipse configuration.
         * Produces the messages as a side effect.
         * 
         * @return the operating system shell return code (0 denotes
         *         success)
         * 
         * @since 1.20
         */
        public int getResult() {
            if (!requiredBuilders.isEmpty()) {
                emitError("This project does not fulfill all required "
                    + "Eclipse builders: " + requiredBuilders.keySet()); 
            }
            if (!requiredNatures.isEmpty()) {
                emitError("This project does not fulfill all required "
                    + "Eclipse project natures: " 
                    + requiredNatures.keySet());                
            }
            return super.getResult();
        }
        
    }

    
    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }
    
    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty(CONF_KEY + "_pre", false);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty(CONF_KEY + "_post", false);
    }

    /**
     * Registers this test automatically when loading this class.
     * 
     * @since 1.20
     */
    static {
        registerTest(new EclipseConfigurationTest());
    }

}