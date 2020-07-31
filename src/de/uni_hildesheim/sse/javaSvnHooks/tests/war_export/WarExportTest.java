package de.uni_hildesheim.sse.javaSvnHooks.tests.war_export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.Test;
import de.uni_hildesheim.sse.javaSvnHooks.tests.file_size.FileSizeTest;
import de.uni_hildesheim.sse.javaSvnHooks.util.ExitCodes;

/**
 * This test packages all submitted files into a web application archive (.war)
 * and deploys them in a Tomcat instance by copying the archive to a specified folder.
 * 
 * This test considers the following parameters from the configuration
 * file. <code>config<i>Nr</i></code> denotes the configuration with
 * a generic number <i>Nr</i>.
 * <ul>
 *  <li><code>config<i>Nr</i>.war_export = true</code> enables this test as post-commit</li>
 *  <li><code>config<i>Nr</i>.war_export.destination = /some/path</code> sets the destination
 *          folder to copy (deploy) the archives to</li>
 * </ul>
 * 
 * @author Adam Krafczyk
 */
public class WarExportTest extends Test {
    
    static {
        registerTest(new WarExportTest());
    }

    @Override
    public String getName() {
        return "Web Application Archive export";
    }

    @Override
    public Class<? extends Test> dependsOn(Configuration.Stage stage) {
        return FileSizeTest.class;
    }

    @Override
    public boolean runInPreCommit(PathConfiguration pathConfiguration) {
        return false;
    }

    @Override
    public boolean runInPostCommit(PathConfiguration pathConfiguration) {
        return pathConfiguration.getBooleanProperty("war_export", false);
    }
    
    /**
     * Copies a single file, or all files (recursively) in a directory into the
     * given archive output stream.
     * 
     * @param baseDir The base directory, used to relativize the path names of
     *      the files to be stored in the archive.
     * @param fileOrDir The single file, or the directory containing other
     *      directories and files to be copied into the archive.
     * @param out The stream used to write the files into the archive.
     * @throws IOException If reading the files or writing the archive fails.
     */
    private void archiveFile(File baseDir, File fileOrDir, ZipOutputStream out) throws IOException {
        if (fileOrDir.isDirectory()) {
            for (File file : fileOrDir.listFiles()) {
                archiveFile(baseDir, file, out);
            }
            
        } else if (fileOrDir.isFile()) {
            FileInputStream in = new FileInputStream(fileOrDir);
            
            String relativeName = fileOrDir.getAbsolutePath().substring(
                    baseDir.getAbsolutePath().length() + 1);
            out.putNextEntry(new ZipEntry(relativeName));
            
            int read;
            while ((read = in.read()) != -1) {
                out.write(read);
            }
            
            in.close();
            
        } else {
            throw new FileNotFoundException(fileOrDir.toString());
        }
    }
    
    /**
     * Creates a new archive that contains all files in the given directory.
     * 
     * @param dir The files that will become the content of the archive.
     * @param config The configuration, used to determine the tmp directory and file name.
     * @return The newly created archive.
     * @throws IOException If reading the files or writing the archive fails.
     */
    private File createWarArchive(File dir, Configuration config) throws IOException {
        String archiveName = dir.getAbsolutePath().substring(
                config.getCheckoutDir().getAbsolutePath().length() + 1);
        archiveName = archiveName.replace('/', '.') + ".war";
        File archive = new File(config.getTempDir(), archiveName);
        
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive));
        
        archiveFile(dir, dir, out);
        
        out.close();
        
        return archive;
    }

    @Override
    public int execute(PathConfiguration pathConfiguration) {
        int returnValue = ExitCodes.EXIT_SUCCESS;
        
        File tomcatDir = null;
        
        String tomcatDirName = pathConfiguration.getStringProperty("war_export.destination", null);
        if (tomcatDirName == null) {
            Logger.INSTANCE.log("Error: war export directory not set");
            returnValue = ExitCodes.EXIT_FAIL;
        } else {
            tomcatDir = new File(tomcatDirName);
            if (!tomcatDir.isDirectory()) {
                Logger.INSTANCE.log("Error: war export directory " + tomcatDir.getPath() + " is not a directory");
                returnValue = ExitCodes.EXIT_FAIL;
            }
        }
        
        if (returnValue == ExitCodes.EXIT_SUCCESS) {
            try {
                File archive = createWarArchive(pathConfiguration.getWorkingDir(),
                        pathConfiguration.getGlobalConfiguration());
                
                File destination = new File(tomcatDir, archive.getName());
                if (destination.exists()) {
                    destination.delete();
                }
                
                FileUtils.moveFile(archive, destination);
                Logger.INSTANCE.log("Deployed web application archive: "
                        + destination.getAbsolutePath());
                
            } catch (IOException e) {
                Logger.INSTANCE.logException(e, false);
                returnValue = ExitCodes.EXIT_FAIL;
            }
        }
        
        return returnValue;
    }

}
