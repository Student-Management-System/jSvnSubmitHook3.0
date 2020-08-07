package de.uni_hildesheim.sse.javaSvnHooks.tests.junit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;

import org.junit.runner.JUnitCore;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.AbstractJavaCompilationTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.ClassRegistry;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.Console;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestResult;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestSuiteSecurityManager;
import de.uni_hildesheim.sse.javaSvnHooks.util.ResultOutputStream;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;
import de.uni_hildesheim.sse.test.suite.AbstractJavaTestSuite;

/**
 * Tests the functionality of the submitted project with the aim of
 * JUnit4.
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.junit_pre = true</code> enables this test</li>
 *  <li><code>config<i>Nr</i>.junit_post = true</code> enables this test</li>
 *  <li><code>config<i>Nr</i>.junit.parameters = </code> may be used to
 *      specify additional parameters to the java compiler</li>
 *  <li><code>config<i>Nr</i>.junit.classpath = </code> may be used to 
 *      specify additional elements for the classpath</li>
 *  <li><code>config<i>Nr</i>.junit.libDir = </code> the directory
 *      to be searched recursively for additional libraries</li>
 *  <li><code>config<i>Nr</i>.junit.testSuiteJar = </code> the jar file 
 *      containing the Junit test suite. Prefixed by <code>jUnitSuitePath</code>
 *      if relative. If not given but <code>jUnitSuitePath</code> is specified,
 *      use {@link PathConfiguration#getName()} as name of the jar (adding .jar)
 *      to simplify the configuration effort. 
 * </ul>
 * Furthermore, the parameters described in
 * {@link de.uni_hildesheim.sse.javaSvnCommit.core.AbstractJavaTest} 
 * are considered.
 * 
 * @author Adam Krafczyk
 *
 */
public class JunitTest extends AbstractJavaCompilationTest {

    /**
     * Registers this test automatically when loading this class.
     * 
     * @since 1.00
     */
    static {
        registerTest(new JunitTest());
    }
    
    /**
     * Compiles the submitted java files.
     * @return The result of the compilation (!= 0 -> error).
     * @throws IOException If compiling throws an {@link IOException}.
     * @throws InterruptedException If compiling throws an {@link InterruptedException}.
     */
    private int compileFiles() throws InterruptedException, IOException {
        int result = 0;
        String srcPath = getJavaSourcePath(pathConfiguration);
        if (null != srcPath && srcPath.length() > 0) {
            homeDir = new File(homeDir, srcPath);
        }

        // TODO DO WE NEED THIS AT ALL? see dependsOn()
        Collection<File> javaFiles = pathConfiguration.getJavaFiles();
        
        if (shouldCompileAtCommandLine()) {
            List<String> files = findJavaFilesList(srcPath);
            result = compiliationAtComandLine(pathConfiguration, files);
        } else {
            result = compileInJava(javaFiles);
        }
        
        return result;
    }
    
    /**
     * Loads the TestSuite from the location specified in the configuration.
     * @return The TestSuite class.
     * @throws NoTestSuiteException If loading the TestSuite fails.
     */
    private Class<?> loadTestSuite() throws NoTestSuiteException {
        String testSuiteJarPath = pathConfiguration.getStringProperty(
                "junit.testSuiteJar", null);
        String junitSuitePath = pathConfiguration.getGlobalConfiguration()
                .getStringProperty("junitSuitePath", null);
        if (null == junitSuitePath && null != testSuiteJarPath) {
            // if global prefix is given but no suite, try configuration name
            junitSuitePath = pathConfiguration.getName() + ".jar";
        }
        if (null != junitSuitePath && null != testSuiteJarPath) {
            File suitePath = new File(testSuiteJarPath);
            if (!suitePath.isAbsolute()) {
                testSuiteJarPath = junitSuitePath + File.separator 
                    + testSuiteJarPath;
            }
        }
        Logger.INSTANCE.log("Loading Jar: " + testSuiteJarPath);
        if (null == testSuiteJarPath) {
            throw new NoTestSuiteException("No TestSuite set in config.");
        }
        
        Logger.INSTANCE.log("Checking whether file exists.");
        if (!new File(testSuiteJarPath).exists()) {
            throw new NoTestSuiteException("TestSuite doesn't exist.");
        }
        
        Logger.INSTANCE.log("Creating Classloader.");
        ClassLoader testSuiteClassLoader;
        try {
            // use the loader of this class, not 
            // ClassLoader.getSystemClassLoader() as this will fail when
            // testing this test with junit
            testSuiteClassLoader = new URLClassLoader(new URL[] {
                new File(testSuiteJarPath).toURI().toURL()}, 
                JunitTest.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new NoTestSuiteException(e);
        }
        
        Logger.INSTANCE.log("Loading Testsuite class.");
        final String suiteClsName = "de.uni_hildesheim.sse.test.JavaTestSuite";
        Class<?> testSuiteClass;
        try {
            testSuiteClass = Class.forName(suiteClsName, true,
                testSuiteClassLoader);
        } catch (ClassNotFoundException e) {
            throw new NoTestSuiteException("Class " + suiteClsName 
                + " not found");
        }
        
        return testSuiteClass;
    }
    
    /**
     * Prints the errors and warnings produced by the TestSuite to SvnOUT.
     * @param testResult The {@link TestResult} of the TestSuite.
     * @return The number of failed mandatory tests.
     */
    private int handleErrors(TestResult testResult) {
        int failedMandatoryTests = 0;
        
        for (TestFailure testFailure : testResult.getTestFailures()) {
            StringBuffer errorMessage = new StringBuffer();
            if (configuration.produceXmlOutput()) {
                errorMessage.append("<message tool=\"junit\" ");
                if (testFailure.isMandatory()) {
                    errorMessage.append("type=\"error\" ");
                } else {
                    errorMessage.append("type=\"warning\" ");
                }
                errorMessage.append("message=\"");
                errorMessage.append(
                    XmlUtilities.xmlifyForAttributes(testFailure.getMessage()));
                errorMessage.append("\"");
                if (testFailure.getFileName() != null) {
                    errorMessage.append(" file=\"");
                    // this filename is already relativized
                    errorMessage.append(XmlUtilities.xmlify(testFailure.getFileName()));
                    errorMessage.append("\"");
                    
                    if (testFailure.getLine() != -1) {
                        errorMessage.append(" line=\"");
                        errorMessage.append(XmlUtilities.xmlify(
                                testFailure.getLine() + ""));
                        errorMessage.append("\"");
                    }
                    
                }
                errorMessage.append("></message>\n");
            } else {
                errorMessage.append("logic ");
                if (testFailure.isMandatory()) {
                    errorMessage.append("error");
                } else {
                    errorMessage.append("warning");
                }
                if (testFailure.getFileName() != null) {
                    errorMessage.append("(in ");
                    errorMessage.append(testFailure.getFileName());
                    if (testFailure.getLine() != -1) {
                        errorMessage.append(" line ");
                        errorMessage.append(testFailure.getLine());
                    }
                    errorMessage.append(")");
                }
                errorMessage.append(": ");
                errorMessage.append(testFailure.getMessage());
                errorMessage.append("\n");
            }
            
            if (testFailure.isMandatory()) {
                failedMandatoryTests++;
            }
            
            configuration.getTestOutputStream().print(errorMessage.toString());
        }
        
        return failedMandatoryTests;
    }
    
    // checkstyle: stop exception type check
    
    @Override
    public int executeCompilationTest() {
        int exitCode = 0;
        try {
            // Compile files to test
            int compileResult = compileFiles();
            // Exit if compilation was unsuccessful
            if (compileResult != 0) {
                exitCode = -1;
            }
            
            if (exitCode == 0) {
                exitCode = executeInternal();
            }
            
        } catch (IOException | InvocationTargetException | InterruptedException
                | IllegalAccessException | NoSuchMethodException e) {
            ResultOutputStream.addErrorMessage(configuration, "hook", 
                "Internal failure (call Java team)", e);
            exitCode = 2;
        } catch (ClassNotFoundException e) {
            ResultOutputStream.addErrorMessage(configuration, "hook", 
                "Class cannot be loaded (wrong package?)", e);
            Logger.INSTANCE.logException(e, true);
            exitCode = 2;
        } catch (NoTestSuiteException e) { // no explicit error emitted so far
            // neither corrector (as no message) nor user (via corrector) can
            // do something here
            ResultOutputStream.addErrorMessage(configuration, "hook", 
                "Test suite not configured properly (please inform tutor)", e);
            exitCode = 2;
        } catch (WrongPackageException e) { // this has been handled properly
            exitCode = 2;
        } catch (Throwable e) {
            ResultOutputStream.addErrorMessage(configuration, "hook", 
                "Internal failure (please call tutor)", e);
            exitCode = 2;
        }
        // if redirected and exception occurred, streams are still redirected
        Console.INSTANCE.deaktivieren();
        
        return exitCode;
    }

    // checkstyle: resume exception type check
    
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // Sorry, also for security and stability reasons, I do not really 
    // understand why the JUnit-tests must run within the same JVM.
    // Since JDK 13 we have to add additional modules to the unnamed
    // module to get this running, which have to be added to the whole Hook
    // JVM rather than through a configuration entry just to the tests.
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
    /**
     * Runs this junit test after files have been compiled.
     * 
     * @return The exit code of this test.
     * @throws WrongPackageException If a class in the submission has a wrong package declaration.
     * @throws NoTestSuiteException If the test suite or the classes of the
     *      submission cannot be loaded.
     * @throws ClassNotFoundException If a class could not be loaded (internal problem).
     * @throws IOException If an IO exception occurs (internal problem).
     * @throws IllegalAccessException If a method of the test suite cannot be invoked (internal problem).
     * @throws InvocationTargetException If a method of the test suite cannot be invoked (internal problem).
     * @throws NoSuchMethodException If a method of the test suite cannot be invoked (internal problem).
     */
    private int executeInternal() throws
            WrongPackageException, NoTestSuiteException,
            ClassNotFoundException, IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        int exitCode = 0;
        // Load TestSuite Jar
        final Class<?> testSuiteClass = loadTestSuite();
        // Activate console if specified in configuration to ensure that
        // statically created Scanners get the proper System.in
        if (pathConfiguration.getBooleanProperty(
                "junit.consoleActivate", true)) {
            Console.INSTANCE.aktivieren();
        }
        // Create a ClassRegistry object and tell the test suite about it
        Logger.INSTANCE.log("Creating Registry for " + homeDir.getAbsolutePath());
        ClassRegistry registry = null;
        try {
            registry = new ClassRegistry(homeDir, 
                getClasspath(pathConfiguration, "javac", false));
        } catch (WrongPackageException exc) {
            //FIXME and TODO SE: Make it generic
            String offendingFileName = exc.getClassFile().getAbsolutePath();
            offendingFileName = offendingFileName.replaceAll("\\.class$", ".java");
            
            String errorMessage = "  <message tool=\"javac\" type=\"error\" "
                    + "file=\"" + XmlUtilities.xmlify(pathConfiguration.relativizeFileName(offendingFileName))
                    + "\" line=\"1\" message=\"Paket-Deklaration und Ordnerstruktur"
                    + " stimmen nicht ueberein.\" />\n";
            configuration.getTestOutputStream().print(errorMessage);
            throw exc;
        }
        AbstractJavaTestSuite.setClassRegistry(registry);
        try { // legacy, set anyway
            testSuiteClass.getMethod("setClassRegistry", ClassRegistry.class)
                .invoke(null, registry);
        } catch (NoClassDefFoundError e) {
            Logger.INSTANCE.logException(e, false);
        } catch (NoSuchMethodException e) {
        }
        // Create a TestResult object and tell the test suite about it
        TestResult testResult = new TestResult();
        testSuiteClass.getMethod("setTestResult", TestResult.class)
                .invoke(null, testResult);
        // Create security manager and tell test suite about it
        TestSuiteSecurityManager securityManager =
                new TestSuiteSecurityManager(homeDir.getAbsolutePath());
        try {
            testSuiteClass.getMethod("setSecurityManager",
                    TestSuiteSecurityManager.class)
                    .invoke(null, securityManager);
        } catch (NoSuchMethodException e) {
            // Probably older test suite version
        }
        // Set user.dir to homeDir, to allow relative file paths in executed code
        String previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", homeDir.getAbsolutePath());
        
        // Run the Unit tests
        boolean timedOut = runUnitTests(testSuiteClass, testResult, securityManager);
        System.setProperty("user.dir", previousUserDir);
        Console.INSTANCE.deaktivieren();
        if (!timedOut) {
            int failedMandatoryTests = handleErrors(testResult);
            
            if (failedMandatoryTests > 0) {
                exitCode = 1;
            }
        } else {
            exitCode = 2;
        }
        registry.close();
        return exitCode;
    }
    
    // checkstyle: stop exception type check

    /**
     * Runs the unit tests.
     * 
     * @param testSuiteClass The test suite class.
     * @param testResult The {@link TestResult} object.
     * @param securityManager The {@link SecurityManager} to set.
     * 
     * @return Whether the test run timed out or not.
     */
    @SuppressWarnings("deprecation")
    private boolean runUnitTests(final Class<?> testSuiteClass, TestResult testResult,
            TestSuiteSecurityManager securityManager) {
        final JUnitCore junit = new JUnitCore();
        JUnitRunListener jUnitRunListener = new JUnitRunListener(testResult);
        junit.addListener(jUnitRunListener);
        Logger.INSTANCE.log("Running TestSuite");
        
        
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    junit.run(testSuiteClass);
                } catch (Throwable t) { // catch everything and report!
                    configuration.getTestOutputStream().print(
                        "<message tool=\"junit\" type=\"error\" "
                        + "message=\"Error during test execution (call Java team): "
                        + t.getMessage() + "\" />");
                }
            }
        };
        
        th.start();
        
        long timeout = pathConfiguration.getIntProperty("junit.timeout",
                1000 * 60 * 2); // 2 minutes
        
        try {
            th.join(timeout);
        } catch (InterruptedException e) {
            Logger.INSTANCE.logException(e, false);
        }
        
        boolean timedOut = false;
        
        synchronized (securityManager) {
            securityManager.unset();
            
            if (th.isAlive()) {
                th.stop();
                
                StringBuffer errorMessage = new StringBuffer();
                if (configuration.produceXmlOutput()) {
                    errorMessage
                        .append("<message tool=\"junit\" type=\"error\" ");
                    errorMessage
                        .append("message=\"Execution exceeded timeout\">");
                    errorMessage.append("</message>\n");
                } else {
                    errorMessage
                        .append("logic error: Execution exceeded timeout");
                    errorMessage.append("\n");
                }
                configuration.getTestOutputStream().print(errorMessage.toString());
                
                timedOut = true;
            }
        }
        
        return timedOut;
    }

    // checkstyle: resume exception type check

    @Override
    public String getName() {
        return "Testing code with JUnit";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return JavacTest.class;
    }
    
    @Override
    protected String getToolName() {
        return "junit";
    }

    @Override
    protected String getCompileParamsToolName() {
        return "javac";
    }

}
