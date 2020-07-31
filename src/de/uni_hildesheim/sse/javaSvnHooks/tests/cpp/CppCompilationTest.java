package de.uni_hildesheim.sse.javaSvnHooks.tests.cpp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.process.ProcessTest;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;

/**
 * Tests whether the submitted C++ files are compilable.
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.cpp_pre = true</code> enables this test as pre-commit</li>
 *  <li><code>config<i>Nr</i>.cpp_post = true</code> enables this test as post-commit</li>
 *  <li><code>config<i>Nr</i>.cpp_compiler = g++</code> sets the compiler (and flags) to use</li>
 *  <li><code>config<i>Nr</i>.cpp_filepattern = .*\.cpp</code> sets pattern to identify C++ files</li>
 * </ul>
 * 
 * @author Adam Krafczyk
 */
public class CppCompilationTest extends Test {
    
    static {
        registerTest(new CppCompilationTest());
    }

    @Override
    public String getName() {
        return "Compiling the submitted sources (C++)";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("cpp_pre", false);
    }

    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("cpp_post", false);
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        String compiler = pathConfiguration.getStringProperty("cpp_compiler", "g++");
        String pattern = pathConfiguration.getStringProperty("cpp_filepattern", ".*\\.cpp");
        
        int returnValue = ExitCodes.EXIT_SUCCESS;
        
        try {
            List<String> compiledFiles = new ArrayList<>();
            for (File file : pathConfiguration.getFiles()) {
                String filename = file.getName();
                if (filename.matches(pattern)) {
                    String base = filename.substring(0, filename.lastIndexOf("."));
                    
                    StringBuilder stdout = new StringBuilder();
                    StringBuilder stderr = new StringBuilder();
                    int status = ProcessTest.runProcess(
                            compiler + " -c -o " + base + ".o " + filename,
                            pathConfiguration.getWorkingDir(),
                            stdout, stderr);
                    
                    if (status == ExitCodes.EXIT_SUCCESS) {
                        compiledFiles.add(base + ".o");
                    }
                    
                    ProcessTest.printMessages(pathConfiguration.getGlobalConfiguration(),
                            stderr.toString(), stdout.toString());
                    
                    if (status != ExitCodes.EXIT_SUCCESS) {
                        returnValue = ExitCodes.EXIT_FAIL;
                    }
                }
            }
            
//            if (returnValue == ExitCodes.EXIT_SUCCESS) {
//                
//                String command = compiler + " -o exe";
//                for (String file : compiledFiles) {
//                    command += " " + file;
//                }
//                
//                StringBuilder stdout = new StringBuilder();
//                StringBuilder stderr = new StringBuilder();
//                int status = ProcessTest.runProcess(
//                        command, pathConfiguration.getWorkingDir(),
//                        stdout, stderr);
//                
//                ProcessTest.printMessages(pathConfiguration.getGlobalConfiguration(),
//                        stderr.toString(), stdout.toString());
//                
//                if (status != ExitCodes.EXIT_SUCCESS) {
//                    returnValue = ExitCodes.EXIT_FAIL;
//                }
//                
//            }
            
        } catch (IOException e) {
            e.printStackTrace();
            returnValue = ExitCodes.EXIT_FAIL;
        }
        
        return returnValue;
    }
    
}
