package de.uni_hildesheim.sse.javaSvnHooks.pre_commit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.admin.ISVNChangeEntryHandler;
import org.tmatesoft.svn.core.wc.admin.ISVNTreeHandler;
import org.tmatesoft.svn.core.wc.admin.SVNAdminPath;
import org.tmatesoft.svn.core.wc.admin.SVNChangeEntry;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;

/**
 * Implements a handler for {@link SVNLookClient#doGetChanged(java.io.File, String, ISVNChangeEntryHandler, boolean)}
 * and {@link SVNLookClient#doGetTree(File, String, String, boolean, boolean, ISVNTreeHandler)}
 * that writes files to the checkoutDir.
 * 
 * @author Adam Krafczyk
 */
class PreCommitSvnCatHandler implements ISVNChangeEntryHandler, ISVNTreeHandler {
    
    private SVNLookClient lookClient;
    
    private PreCommitConfiguration configuration;
    
    private List<String> changedFiles;
    
    /**
     * Creates an {@link PreCommitSvnCatHandler} that will write files to the checkoutDir of the configuration.
     * 
     * @param configuration The {@link Configuration} object.
     * @param lookClient The {@link SVNLookClient} to get files from the repository.
     */
    PreCommitSvnCatHandler(PreCommitConfiguration configuration, SVNLookClient lookClient) {
        this.lookClient = lookClient;
        this.configuration = configuration;
        changedFiles = new ArrayList<String>();
    }
    
    /**
     * Checks out the given repository path using the <code>svnlook</code> utility program.
     * 
     * @param path The path inside the repository to check out.
     * @param target The target file location to write the file to.
     */
    private void checkoutWithSvnlookProcess(String path, File target) {
        
        ProcessBuilder pb = new ProcessBuilder(
            "/usr/bin/svnlook",
            "cat",
            "--transaction",
            configuration.getTransaction(),
            configuration.getRepository().getAbsolutePath(),
            path
        );
        
        pb.redirectOutput(target);
        pb.redirectError(Redirect.PIPE);
        
        try {
            Process proc = pb.start();
            
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            StringBuilder stderr = new StringBuilder();
            String line;
            while ((line = stderrReader.readLine()) != null) {
                stderr.append(line);
            }
            
            int result = proc.waitFor();
            
            if (result != 0) {
                Logger.INSTANCE.log("WARNING: svnlook exited with " + result);
                Logger.INSTANCE.log("svnlook stderr:\n" + stderr.toString());
            }
            
        } catch (IOException | InterruptedException e) {
            Logger.INSTANCE.logException(e, false);
        }
    }
    
    /**
     * Checks out the given path in the repository using the svnkit library.
     * <p>
     * <b>Note:</b> There occurred a strange bug inside of svnkit, where checking out files in the pre-commit hook
     * sometimes failed inexplicably. For this reason, {@link #checkoutWithSvnlookProcess(String, File)} was created
     * as a replacement.
     * 
     * @param path The path inside the repository to check out.
     * @param target The target file location to write the file to.
     * 
     * @throws SVNException If the checkout fails.
     */
    @SuppressWarnings("unused")
    private void checkoutWithSvnkit(String path, File target) throws SVNException {
        try (FileOutputStream fos = new FileOutputStream(target)) {
            
            lookClient.doCat(configuration.getRepository(), path, configuration.getTransaction(), fos);
            
        } catch (IOException e) {
            Logger.INSTANCE.logException(e, false);
        }
    }

    /**
     * Checks out the given file in the repository to the checkout directory.
     * 
     * @param path The path inside the repository.
     * 
     * @throws SVNException If checking out the file fails.
     */
    private void writeFileToCheckoutDir(String path) throws SVNException {
        changedFiles.add(path);
        
        File target = new File(configuration.getCheckoutDir(), path);
        File dir = target.getParentFile();
        if (!dir.isDirectory() && !dir.mkdirs()) {
            Logger.INSTANCE.log("WARNING: Could not create directory " + dir);
        }
        
//        checkoutWithSvnkit(path, target);
        
        checkoutWithSvnlookProcess(path, target);
    }
    
    @Override
    public void handleEntry(SVNChangeEntry entry) throws SVNException {
//        Logger.INSTANCE.log("handleEntry(path=" + entry.getPath() + ", type=" + entry.getType()
//                + ", kind=" + entry.getKind() + ")");
        
        if (entry.getKind() == SVNNodeKind.FILE && !isFileIgnored(entry.getPath())
                && entry.getType() != SVNChangeEntry.TYPE_DELETED) {
            
            writeFileToCheckoutDir(entry.getPath());
        }
    }

    @Override
    public void handlePath(SVNAdminPath path) throws SVNException {
//        Logger.INSTANCE.log("handlePath(path=" + path.getPath() + ", isDir=" + path.isDir() + ")");
        
        if (!path.isDir() && !isFileIgnored(path.getPath())) {
            writeFileToCheckoutDir(path.getPath());
        }
    }
    
    /**
     * Checks whether the {@link Configuration} specifies that the file is ignored.
     * @param path The path of the file.
     * @return <code>true</code> if the file is ignored.
     */
    private boolean isFileIgnored(String path) {
        boolean ignored = false;
        for (Pattern pattern : configuration.getIgnoreFilesPatterns()) {
            if (pattern.matcher(path).matches()) {
                ignored = true;
                break;
            }
        }
        return ignored;
    }
    
    /**
     * Getter for all modified files in doGetChanged() (full path in SVN repository).
     * 
     * @return An unmodifiable {@link List} of filenames.
     */
    public List<String> getChangedFiles() {
        return Collections.unmodifiableList(changedFiles);
    }

}
