package de.uni_hildesheim.sse.javaSvnHooks.tests.javac;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaCompilationTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config.EclipseConfigurationTest;

/**
 * Implements a (default) commit test for the Java compiler.
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.javac = true</code> enables this test</li>
 *  <li><code>config<i>Nr</i>.javac.params = </code> may be used to 
 *      specify additional parameters for the Java compiler</li>
 *  <li><code>config<i>Nr</i>.javac.classpath = </code> may be used to 
 *      specify additional elements for the classpath</li>
 *  <li><code>config<i>Nr</i>.javac.libDir = </code> the directory
 *      to be searched recursively for additional libraries</li>
 * </ul>
 * Furthermore, the parameters described in
 * {@link de.uni_hildesheim.sse.javaSvnCommit.core.AbstractJavaTest} 
 * are considered. This test also implements a non-threaded non-native
 * call of the Java compiler. Therefore, the hook must be run with
 * the Java command from the JDK (not the default one from the JRE) so
 * that Java can find its own default compiler instance. Even if 
 * non-native test threading is enabled, this compiler will not call the
 * internal Java compiler if none is present, i.e. if the java interpreter
 * of a JRE runs the hook.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JavacTest extends AbstractJavaCompilationTest {

    @Override
    public String getName() {
        return "Compiling the submitted sources (Java)";
    }
    
    @Override
    public int executeCompilationTest() {
        int result = 0;
        
        String srcPath = getJavaSourcePath(pathConfiguration);
        if (null != srcPath && srcPath.length() > 0) {
            homeDir = new File(homeDir, srcPath);
        }

        Collection<File> javaFiles = pathConfiguration.getJavaFiles();
        
        for (File file : javaFiles) {
            checkAndRepairEncoding(file);
        }
        
        try {
            if (shouldCompileAtCommandLine()) {
                List<String> files = findJavaFilesList(srcPath);
                result = compiliationAtComandLine(pathConfiguration, files);
            } else {
                result = compileInJava(javaFiles);
            }
        } catch (IOException | InterruptedException e) {
            Logger.INSTANCE.logException(e, false);
            result = 2;
        }
        return result;
    }
    
    /**
     * Searches for umlauts that are wrongly converted to UTF-8.
     * @param file The file to check and possibly fix.
     */
    private void checkAndRepairEncoding(File file) {
        try {
            String content = org.apache.commons.io.FileUtils.readFileToString(
                    file, StandardCharsets.UTF_8);
            
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                switch (c) {
                // Umlauts coded with "ANSI"
                case 65533:
                    break;
                // Umlauts coded with "UTF-8 without BOM"
                case 196:
                    break;
                case 214:
                    break;
                case 220:
                    break;
                case 223:
                    break;
                case 228:
                    break;
                case 246:
                    break;
                case 252:
                    break;
                // Umlauts coded with "UTF-8"
                case 65279:
                    break;
                default:
                    builder.append(c);
                    break;
                }
            }
            
            org.apache.commons.io.FileUtils.write(file, builder.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return EclipseConfigurationTest.class;
    }

    /**
     * Registers this test automatically when loading this class.
     * 
     * @since 1.00
     */
    static {
        registerTest(new JavacTest());
    }

    @Override
    protected String getToolName() {
        return "javac";
    }

}