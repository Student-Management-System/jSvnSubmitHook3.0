package de.uni_hildesheim.sse.javaSvnHooks.tests.java_files;

import java.io.PrintStream;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;

/**
 * Checks whether java files are submitted if noJavaFileAsError is set to true
 * in global configuration.
 * 
 * @author Adam Krafczyk
 */
public class JavaFilesTest extends Test {

    @Override
    public String getName() {
        return "Checking whether java files are submitted";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getGlobalConfiguration().getBooleanProperty("noJavaFileAsError", false);
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return false;
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        int exitCode = 0;
        
        if (pathConfiguration.getJavaFiles().size() == 0) {
            exitCode = 1;
            
            Configuration config = pathConfiguration.getGlobalConfiguration();
            PrintStream out = config.getTestOutputStream();
            
            if (config.produceXmlOutput()) {
                out.println("<message tool=\"java-files-check\""
                        + " type=\"error\" file=\"\" line=\"\" message=\"No java file submitted.\"></message>");
            } else {
                out.println("No Java file submitted.");
            }
            
        }
        
        return exitCode;
    }

}
