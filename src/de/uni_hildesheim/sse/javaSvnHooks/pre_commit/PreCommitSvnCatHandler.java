package de.uni_hildesheim.sse.javaSvnHooks.pre_commit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    @Override
    public void handleEntry(SVNChangeEntry entry) throws SVNException {
        if (entry.getKind() == SVNNodeKind.FILE && !isFileIgnored(entry.getPath())) {
            
            changedFiles.add(entry.getPath());
            
            File file = new File(configuration.getCheckoutDir(), entry.getPath());
            file.getParentFile().mkdirs();
            
            if (entry.getType() != SVNChangeEntry.TYPE_DELETED) {
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    
                    lookClient.doCat(configuration.getRepository(), entry.getPath(),
                            configuration.getTransaction(), fos);
                    
                    fos.close();
                    
                } catch (FileNotFoundException e) {
                    Logger.INSTANCE.logException(e, false);
                } catch (IOException e) {
                    Logger.INSTANCE.logException(e, false);
                }
            }
            
        }
    }

    @Override
    public void handlePath(SVNAdminPath path) throws SVNException {
        if (!path.isDir() && !isFileIgnored(path.getPath())) {
            
            File file = new File(configuration.getCheckoutDir(), path.getPath());
            
            changedFiles.add(path.getPath());
            
            file.getParentFile().mkdirs();
            
            try {
                FileOutputStream fos = new FileOutputStream(file);
                
                lookClient.doCat(configuration.getRepository(), path.getPath(),
                        configuration.getTransaction(), fos);
                
                fos.close();
                
            } catch (FileNotFoundException e) {
                Logger.INSTANCE.logException(e, false);
            } catch (IOException e) {
                Logger.INSTANCE.logException(e, false);
            }
            
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
