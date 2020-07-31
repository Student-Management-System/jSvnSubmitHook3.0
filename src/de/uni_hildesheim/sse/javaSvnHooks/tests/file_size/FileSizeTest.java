package de.uni_hildesheim.sse.javaSvnHooks.tests.file_size;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Checks whether there are too large files in the submitted project.
 * This test will always run in the pre-commit hook if either maxFileSize or
 * maxProjectSize is defined (and > 0) in the global configuration.
 * 
 * @author Adam Krafczyk
 */
public class FileSizeTest extends Test {

    @Override
    public String getName() {
        return "Checking file and project size";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return null;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return true;
    }
    
    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return true;
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        Configuration config = pathConfiguration.getGlobalConfiguration();
        PrintStream out = config.getTestOutputStream();
        long maxProjectSize = config.getLongProperty("maxProjectSize", 0);
        long maxFileSize = config.getLongProperty("maxFileSize", 0);
        
        List<File> tooBigFiles = new ArrayList<File>();
        long projectSize = 0;
        
        if (maxFileSize > 0) {
            for (File file : pathConfiguration.getFiles()) {
                long length = file.length();
                projectSize += length;
                if (length > maxFileSize) {
                    tooBigFiles.add(file);
                }
            }
        }
        
        int exitCode = 0;
        
        if (tooBigFiles.size() > 0) {
            exitCode = 1;
            if (config.produceXmlOutput()) {
                out.print("<message tool=\"file-size-check\""
                        + " type=\"error\" file=\"\" line=\"\" message=\"");
            }
            
            StringBuilder message = new StringBuilder();
            
            if (tooBigFiles.size() == 1) {
                message.append("The following file is rejected because it exceeds the maximum file size: ");
            } else {
                message.append("The following files are rejected because they exceed the maximum file size: ");
            }
            
            for (int i = 0; i < tooBigFiles.size(); i++) {
                message.append(tooBigFiles.get(i).getPath());
                if (i + 1 < tooBigFiles.size()) {
                    message.append(", ");
                }
            }
            
            if (config.produceXmlOutput()) {
                out.print(XmlUtilities.xmlify(message.toString()));
                out.print("\"/>");
            } else {
                out.print(message.toString());
            }
            
            out.println();
        }
        
        if (maxProjectSize > 0 && projectSize > maxProjectSize) {
            exitCode = 1;
            if (config.produceXmlOutput()) {
                out.println("<message tool=\"file-size-check\" type=\"error\""
                        + " message=\"The project exceeds the maximum size.\"></message>");
            } else {
                out.println("The project exceeds the maximum size.");
            }
        }
        
        return exitCode;
    }
    
    static {
        Test.registerTest(new FileSizeTest());
    }

}
