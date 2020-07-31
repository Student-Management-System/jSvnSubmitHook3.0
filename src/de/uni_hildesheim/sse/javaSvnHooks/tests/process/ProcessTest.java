package de.uni_hildesheim.sse.javaSvnHooks.tests.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * This test runs an external process that does the actual tests on the submitted
 * files. The external process is run in the temporary checkout directory.
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.process_pre = true</code> enables this test as pre-commit</li>
 *  <li><code>config<i>Nr</i>.process_post = true</code> enables this test as post-commit</li>
 *  <li><code>config<i>Nr</i>.process = /some/exe</code> sets the executable file to call</li>
 * </ul>
 * 
 * @author Adam Krafczyk
 */
public class ProcessTest extends Test {

    static {
        registerTest(new ProcessTest());
    }
    
    @Override
    public String getName() {
        return "generic process execution";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("process_pre", false);
    }

    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("process_post", false);
    }
    
    /**
     * Runs a process.
     * 
     * @param command The executable file to call.
     * @param workingDir The working dir of the new process.
     * @param stdout Buffer to append the stdout output of the process to.
     * @param stderr Buffer to append the stderr output of the process to.
     * @return The exit code of the process.
     * @throws IOException If an I/O error occurs.
     */
    public static int runProcess(String command, File workingDir, StringBuilder stdout,
            StringBuilder stderr) throws IOException {
        
        List<String> commandList = new ArrayList<>();
        commandList.add("/bin/bash");
        commandList.add("-c");
        commandList.add(command);
        
        ProcessBuilder builder = new ProcessBuilder(commandList);
        builder.directory(workingDir);
        
        Process process = builder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line;
        while ((line = out.readLine()) != null) {
            stdout.append(line).append('\n');
        }
        while ((line = err.readLine()) != null) {
            stderr.append(line).append('\n');
        }
        
        return process.exitValue();
    }
    
    /**
     * Prints the results of a process into the given test output.
     * @param config The configuration.
     * @param errors The error messages.
     * @param warnings The warning messages.
     * @throws IOException 
     */
    public static void printMessages(Configuration config, String errors,
            String warnings) throws IOException {
        if (!errors.equals("")) {
            StringBuilder out = new StringBuilder();
            if (config.produceXmlOutput()) {
                out.append("<message tool=\"cpp_compiler\" type=\"error\" file=\"\"");
                out.append(" line=\"\" message=\"");
                out.append(XmlUtilities.xmlifyForAttributes(errors));
                out.append("\" />");
            } else {
                out.append("Error(s):\n");
                out.append(errors);
            }
            config.getTestOutputStream().println(out.toString());
        }
        
        if (!warnings.equals("")) {
            StringBuilder out = new StringBuilder();
            if (config.produceXmlOutput()) {
                out.append("<message tool=\"cpp_compiler\" type=\"warning\" file=\"\"");
                out.append(" line=\"\" message=\"");
                out.append(XmlUtilities.xmlifyForAttributes(warnings));
                out.append("\" />");
            } else {
                out.append("Warning(s):\n");
                out.append(warnings);
            }
            config.getTestOutputStream().println(out.toString());
        }
        
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        int returnValue = ExitCodes.EXIT_FAIL;
        
        try {
            String command = pathConfiguration.getStringProperty("process", "");
            
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            int status = runProcess(command, pathConfiguration.getWorkingDir(),
                    stdout, stderr);
            
            printMessages(pathConfiguration.getGlobalConfiguration(),
                    stderr.toString(), stdout.toString());
            
            if (status == ExitCodes.EXIT_SUCCESS) {
                returnValue = ExitCodes.EXIT_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return returnValue;
    }

}
