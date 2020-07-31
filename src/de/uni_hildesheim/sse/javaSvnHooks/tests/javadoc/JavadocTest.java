package de.uni_hildesheim.sse.javaSvnHooks.tests.javadoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;

/**
 * Implements a (default) commit test for the Javadoc
 * documentation generator. Even if {@link CheckstyleTest}
 * may run before or after this check, checkstyle does 
 * not consider all issues detected while generating
 * documentation and the generator does not consider the
 * issues detected by checkstyle. 
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.javadoc = true</code> enables this test</li>
 *  <li><code>config<i>Nr</i>.javadoc.parameters = </code> may be used to
 *      specify additional parameters to the javadoc generator</li>
 *  <li><code>config<i>Nr</i>.javadoc.classpath = </code> may be used to 
 *      specify additional elements for the classpath</li>
 *  <li><code>config<i>Nr</i>.javadoc.libDir = </code> the directory
 *      to be searched recursively for additional libraries</li>
 * </ul>
 * Furthermore, the parameters described in {@link AbstractJavaTest} 
 * are considered.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JavadocTest extends AbstractJavaTest {

    @Override
    public String getName() {
        return "Building the source code documentation (Java)";
    }
    
    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("javadoc_pre", false);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("javadoc_post", false);
    }
    
    @Override
    public int execute(PathConfiguration pathConfiguration) {
        int result = 0;
        Configuration configuration 
            = pathConfiguration.getGlobalConfiguration();

        File homeDir = pathConfiguration.getWorkingDir();
        File outDir = new File(homeDir, "doc_"
            + configuration.getUniqueIdentifier());
        String srcPath = getJavaSourcePath(pathConfiguration);
        Runtime runtime = Runtime.getRuntime();
        String[] env = new String[0];

        List<String> params = getParams(pathConfiguration, srcPath);
        String arg = getArgs(pathConfiguration, srcPath);
        
        JavadocTestFilter filter =
            new JavadocTestFilter(pathConfiguration, "javadoc", true);
        List<String> cmdList = new ArrayList<String>();
        cmdList.add(pathConfiguration.prefixJava("javadoc"));
        cmdList.add("-d");
        cmdList.add("\"" + outDir.getAbsolutePath() + "\"");
        cmdList.addAll(params);
        cmdList.add(arg);
        Logger.INSTANCE.log(cmdList2String(cmdList) + " in " + homeDir);
        
        try {
            result = runProcess(configuration, runtime.exec(cmdList2Array(cmdList),
                env, homeDir), false, filter);
        } catch (InterruptedException | IOException e) {
            Logger.INSTANCE.logException(e, false);
            result = 2;
        }
        if (0 == result && filter.hasError()) {
            result = 1;
        }
        return result;
    }

    /**
     * Creates the arguments string to be passed to the javadoc call.
     * @param pathConfiguration The {@link PathConfiguration} for this test.
     * @param srcPath The source path.
     * @return The list of files as argument string for the javadoc call.
     */
    private String getArgs(PathConfiguration pathConfiguration, String srcPath) {
        StringBuffer arg = new StringBuffer();
        
        Set<File> javaDirs = new HashSet<File>();
        for (Iterator<File> iter = pathConfiguration.getJavaFiles().iterator(); iter
                .hasNext();) {
            javaDirs.add(iter.next().getParentFile());
        }
        
        for (Iterator<File> iter = javaDirs.iterator(); 
            iter.hasNext();) {
            String f = iter.next().getPath();
            f = pathConfiguration.relativizeFileName(f); // just slashes
            boolean append = true;
            if (null != srcPath && srcPath.length() > 0 
                && f.startsWith(srcPath + "/")) {
                f = f.substring(srcPath.length() + 1);
                while (f.startsWith("/")) {
                    f = f.substring(1);
                }
                append = f.length() > 0;
            }
            if (append) {
                if (arg.length() > 0) {
                    arg.append(" ");
                }
                arg.append(f);
            }
        }

        for (Iterator<File> iter = pathConfiguration.getJavaFiles().iterator(); iter
            .hasNext();) {
            String f = iter.next().getPath();
            f = pathConfiguration.relativizeFileName(f); // just slashes
            if (arg.length() > 0) {
                arg.append(" ");
            }
            arg.append(f);
        }
        return arg.toString();
    }

    /**
     * Creates the parameter string for the javadoc call.
     * @param pathConfiguration The {@link PathConfiguration} of this test.
     * @param srcPath The source path.
     * @return The parameter string for the javadoc call.
     */
    private List<String> getParams(PathConfiguration pathConfiguration, String srcPath) {
        List<String> params = getJavaParam(pathConfiguration, "javadoc", false);
        if (null != srcPath && srcPath.length() > 0) {
            params.add("-sourcepath");
            params.add(srcPath);
        }
        return params;
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return JavacTest.class;
    }

    /**
     * Registers this test automatically when loading this class.
     * 
     * @since 1.00
     */
    static {
        registerTest(new JavadocTest());
    }

}