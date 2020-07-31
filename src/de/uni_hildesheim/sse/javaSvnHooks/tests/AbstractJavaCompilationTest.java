package de.uni_hildesheim.sse.javaSvnHooks.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTestFilter;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.PrintDiagnostics;

/**
 * Realizes the basic functionality for tests including the compilation of
 * Java source code files.
 * @author El-Sharkawy
 *
 */
public abstract class AbstractJavaCompilationTest extends AbstractJavaTest {

    protected PathConfiguration pathConfiguration;
    protected Configuration configuration;
    protected File homeDir;
    protected List<String> params;
    
    @Override
    public int execute(PathConfiguration pathConfiguration) {
        // Settings
        this.pathConfiguration = pathConfiguration;
        configuration = pathConfiguration.getGlobalConfiguration();
        /*
         * TODO SE: Added getAbsoluteFile() -> check whether this will work on
         * server.
         */
        homeDir = pathConfiguration.getWorkingDir().getAbsoluteFile();

        // Cleanup
        deleteCompiledClasses(homeDir);
        
        // Compilation settings
        String binPath = getJavaBinaryPath(pathConfiguration);
        params = readParams(pathConfiguration, homeDir, binPath, 
            getCompileParamsToolName());
        
        return executeCompilationTest();
    }
    
    /**
     * Returns the configuration name where to read the compiler parameters 
     * from.
     * 
     * @return {@link #getToolName()}
     */
    protected String getCompileParamsToolName() {
        return getToolName();
    }
    
    /**
     * Executes this test according to the specified configuration.
     * Template pattern.
     * 
     * @return the operating system shell return code (0 denotes
     *         success)
     * 
     */
    protected abstract int executeCompilationTest();
    
    /**
     * Returns the name of the tool inside the configuration properties.
     * @return The name of the tool inside the configuration properties.
     */
    protected abstract String getToolName();
    
    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty(getToolName() + "_pre", false);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty(getToolName() + "_post", false);
    }

    /**
     * Compiles the given set of Java source files inside this java application.
     * <br/>
     * Alternatively, {@link #compiliationAtComandLine(StringBuffer)} can
     * be used, if {@link ToolProvider#getSystemJavaCompiler()} is returning
     * <tt>null</tt>.
     * @param javaFiles All Java source code files, which shall be compiled
     *     together
     * @return The operating system shell return code (0 denotes success)
     * @throws IOException if an I/O error occurred
     * @see #compiliationAtComandLine(StringBuffer)
     */
    protected int compileInJava(Collection<File> javaFiles) throws IOException {
        
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = 
            tool.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> units;
        units = manager.getJavaFileObjectsFromFiles(javaFiles);
        DiagnosticCollector<JavaFileObject> diagnostics = 
            new DiagnosticCollector<JavaFileObject>();
        CompilationTask task = tool.getTask(null, manager, 
            diagnostics, params, null, units);
        
        int result = task.call() ? 0 : 1;
        
        PrintDiagnostics.printDiagnostics(configuration.getTestOutputStream(), diagnostics,
            configuration.produceXmlOutput());
        
        manager.close();
        return result;
    }

    /**
     * Compiles the given set of Java source files externally at the command
     * line. <br/>
     * Alternatively, {@link #compileInJava(List)} can be used, if
     * {@link ToolProvider#getSystemJavaCompiler()} is not returning
     * <tt>null</tt>.
     * @param config The {@link PathConfiguration} object.
     * @param files All Java source code files, which shall be compiled together
     * @return The operating system shell return code (0 denotes success)
     * @throws InterruptedException if the current thread is interrupted 
     *         by another thread while it is waiting, then the wait is ended 
     *         and an InterruptedException is thrown.
     * @throws IOException if an I/O error occurred
     * @see #compileInJava(List)
     */
    protected int compiliationAtComandLine(PathConfiguration config, List<String> files)
        throws InterruptedException, IOException {
        
        Runtime runtime = Runtime.getRuntime();
        String[] env = new String[0];
        JavacTestFilter filter = null;
        if (config.getGlobalConfiguration().produceXmlOutput()) {
            filter = new JavacTestFilter(config);
        }
        
        List<String> cmdList = new ArrayList<String>();
        cmdList.add(config.prefixJava("javac"));
        cmdList.addAll(params);
        cmdList.addAll(files);
        Logger.INSTANCE.log(cmdList2String(cmdList) + " in " + homeDir);
        return runProcess(config.getGlobalConfiguration(), 
            runtime.exec(cmdList2Array(cmdList), env, homeDir), false, filter);
    }
    
    /**
     * Checks whether Java source code files should be compile at command line
     * or inside this application.
     * @return <tt>true</tt> if the settings specified that java source code
     * files should be compiled outside of this application or if the Java
     * compiler could not be found (e.g. JAVA_HOME is not pointing to the JDK),
     * <tt>false</tt> otherwise.
     */
    protected boolean shouldCompileAtCommandLine() {
        boolean runNative = configuration.getBooleanProperty("nativeTestThreads", true);
        boolean hasNoCompiler = null == ToolProvider.getSystemJavaCompiler();
        
        return runNative || (hasNoCompiler && !runNative);
    }

    /**
     * Finds all Java source code files in the given dir (<tt>srcPath</tt>).
     * @param srcPath The location of the Java source code files.
     * @return A Whitespace separated list of java source code files.
     */
    protected StringBuffer findJavaFiles(String srcPath) {
        StringBuffer files = new StringBuffer();
        findJavaFilesImpl(srcPath, files, null);
        return files;
    }

    /**
     * Finds all Java source code files in the given dir (<tt>srcPath</tt>).
     * @param srcPath The location of the Java source code files.
     * @return list of java source code files.
     */
    protected List<String> findJavaFilesList(String srcPath) {
        List<String> result = new ArrayList<String>();
        findJavaFilesImpl(srcPath, null, result);
        return result;
    }

    /**
     * Finds all Java source code files in the given dir (<tt>srcPath</tt>).
     * @param srcPath The location of the Java source code files.
     * @param files the string buffer to fill if given (separated by whitespaces)
     * @param filesList a list of strings to fill
     */
    private void findJavaFilesImpl(String srcPath, StringBuffer files, List<String> filesList) {
        for (Iterator<File> iter = pathConfiguration.getJavaFiles().iterator(); 
            iter.hasNext();) {
            String javaFile = iter.next().toString();
    
            if (null != files && files.length() > 0) {
                files.append(" ");
            }
            
            if (null != srcPath && srcPath.length() > 0 
                && javaFile.startsWith(srcPath + File.separator)) {
                javaFile = javaFile.substring(srcPath.length() + 1);
                while (javaFile.startsWith(File.separator)) {
                    javaFile = javaFile.substring(1);
                }
            }
            // do we have spaces in the path... may be the base path?
            // hmm. it seems that javac/Linux does not recognize the quotes :|
            //if (javaFile.contains(" ")) {
            //    javaFile = "\"" + javaFile + "\"";
            //}
            
            if (null != files) {
                files.append(javaFile);
            } else if (null != filesList) {
                filesList.add(javaFile);
            }
        }
    }
    
}
