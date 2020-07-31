package de.uni_hildesheim.sse.javaSvnHooks.tests.checkstyle;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;

/**
 * Implements a (default) commit test for checkstyle.
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.checkstyle = true</code> enables this test</li>
 *  <li><code>config<i>Nr</i>.checkstyle.config = </code> defines the file
 *      to be loaded as configuration. Paths are always interpreted with
 *      the installation directory as base and take <code>$installDir</code> 
 *      substitution into account.</li>
 *  <li><code>config<i>Nr</i>.checkstyle.pre-config = </code> defines the file
 *      to be loaded as configuration in pre-commit hook execution. If none is 
 *      given, use <code>checkstyle.config</code> as fallback. Paths are always 
 *      interpreted with the installation directory as base and take 
 *      <code>$installDir</code> substitution into account.</li>
 *  <li><code>config<i>Nr</i>.checkstyle.jvmParams = </code> may be used
 *      to specify VM parameter.</li>
 *  <li><code>config<i>Nr</i>.checkstyle.params = </code> may be used
 *      to specify additional checkstyle parameter.</li>
 * </ul>
 * Furthermore, the jar file (and optionally additional jar files
 * can globally be specified using <code>checkstyleJar</code>. Please consider
 * to append the individual jar files using the valid operation system 
 * dependent path separator. This property is subject to <code>$installDir</code> substitution.
 * 
 * Also the Checkstyle main class can be specified
 * using <code>checkstyleMain</code>. Here, the default value is 
 * <code>com.puppycrawl.tools.checkstyle.Main</code>.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CheckstyleTest extends AbstractJavaTest {

    @Override
    public String getName() {
        return "Source code style check (Java)";
    }
    
    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("checkstyle_pre", false);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("checkstyle_post", false);
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        int result = 0;
        Configuration configuration 
            = pathConfiguration.getGlobalConfiguration();

        File homeDir = pathConfiguration.getWorkingDir();
        Runtime runtime = Runtime.getRuntime();
        String[] env = new String[0];
        String main = configuration.getStringProperty("checkstyleMain", 
            "com.puppycrawl.tools.checkstyle.Main");
        String command =
            pathConfiguration.prefixJava("java ") + getJVMParams(pathConfiguration, configuration)
            + " " + main + " "
            + getParams(pathConfiguration, configuration) + " ";
        CheckstyleTestFilter filter = null;
        if (configuration.produceXmlOutput()) {
            filter = new CheckstyleTestFilter(pathConfiguration, "checkstyle", true);
        }

        String srcPath = getJavaSourcePath(pathConfiguration);
        if (null != srcPath && srcPath.length() > 0) {
            homeDir = new File(homeDir, srcPath);
        }
        
        StringBuffer files = new StringBuffer();
        for (Iterator<File> iter = pathConfiguration.getJavaFiles().iterator(); 
            iter.hasNext();) {
            String javaFile = iter.next().getPath();

            if (files.length() > 0) {
                files.append(" ");
            }
        
            if (null != srcPath && srcPath.length() > 0 
                && javaFile.startsWith(srcPath + File.separator)) {
                javaFile = javaFile.substring(srcPath.length() + 1);
                while (javaFile.startsWith(File.separator)) {
                    javaFile = javaFile.substring(1);
                }
            }
            files.append(javaFile);
        }

        Logger.INSTANCE.log(command + files + " in " + homeDir);
        try {
            result = runProcess(configuration,
                runtime.exec(command + files, env, homeDir), true,
                filter);
        } catch (InterruptedException | IOException e) {
            Logger.INSTANCE.logException(e, false);
            result = 2;
        }

        return result;
    }

    /**
     * Gets the checkstyle parameters from the given configuration.
     * 
     * @param pathConfiguration The {@link PathConfiguration}.
     * @param configuration The {@link Configuration}.
     * @return The parameter string to passed to the java call.
     */
    private String getParams(PathConfiguration pathConfiguration,
            Configuration configuration) {
        String result = "";
        String config = "";
        // in pre-stage, try to get specific configuration
        if (Configuration.Stage.PRE_COMMIT == configuration.getStage()) {
            config = pathConfiguration.getStringProperty(
                "checkstyle.pre-config", "");
        }
        // if no config so far, try to get default (legacy) config
        if (config.length() == 0) {
            config = pathConfiguration.getStringProperty(
                    "checkstyle.config", "");
        }
        // if there is a config, substitute
        if (config.length() > 0) {
            config = substituteDirs(config);
        }
        // as before, if there is a config, append it to the params
        if (config.length() > 0) {
            if (configuration.getHookDir() != null) { // for testing
                config = configuration.getHookDir() + File.separator + config;
            }
            result = "-c " + config;
        }
        result += pathConfiguration.getStringProperty("checkstyle.params", "");
        return result;
    }
    
    /**
     * Gets the JVM paramters from the given configuration.
     * 
     * @param pathConfiguration The {@link PathConfiguration}.
     * @param configuration The {@link Configuration}.
     * @return The JVM parameter string to passed to the java call.
     */
    private String getJVMParams(PathConfiguration pathConfiguration,
            Configuration configuration) {
        String jar = substituteDirs(
            configuration.getStringProperty("checkstyleJar", ""));
        String classpath = pathConfiguration.getClasspathProperty();
        String jvmParams =
            pathConfiguration.getStringProperty("checkstyle.jvmParams", "");

        classpath = appendToClasspath(classpath, jar);
        classpath = appendLibDirsToClasspath(classpath, pathConfiguration, 
            "checkstyle");

        
        classpath = appendToClasspath(classpath, 
            pathConfiguration.getClasspathProperty());
        classpath = appendLibDirsToClasspath(classpath, pathConfiguration,
            "javac");
        String testEnvClasspath = pathConfiguration.getGlobalConfiguration().
            getTestEnvValue(TestEnvKey.CLASSPATH);
        if (null != testEnvClasspath) {
            classpath = appendToClasspath(classpath, testEnvClasspath);
        }
        
        if (classpath.length() > 0) {
            classpath = "-classpath " + classpath;
        }
        jvmParams += " " + classpath;
        
        return jvmParams;
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return stage == Configuration.Stage.POST_COMMIT 
            ? JavacTest.class : null;
    }

    /**
     * Registers this test automatically when loading this class.
     * 
     * @since 1.00
     */
    static {
        registerTest(new CheckstyleTest());
    }

}