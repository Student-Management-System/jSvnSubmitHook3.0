package de.uni_hildesheim.sse.javaSvnHooks.post_commit;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.tmatesoft.svn.core.wc.SVNRevision;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;

/**
 * The post-commit specific implementation of the {@link Configuration}.
 * @author Adam Krafczyk
 */
class PostCommitConfiguration extends Configuration {

    private File repository;
    
    private SVNRevision revision;
    
    /**
     * Creates a {@link PostCommitConfiguration} with the given parameters.
     * 
     * @param repository The path to the SVN repository.
     * @param revision The revision identifier used by SVN.
     * @param hooksDir The path to the directory this hook.
     * @param testOutputStream The stream to write the output of tests to.
     */
    PostCommitConfiguration(String repository, String revision, String hooksDir, OutputStream testOutputStream) {
        this.repository = new File(repository);
        this.revision = SVNRevision.parse(revision);
        
        setHookDir(new File(hooksDir));
        
        File configFile = new File(hooksDir, "hook.properties");
        readConfig(configFile);
        
        setTestOutputStream(new PrintStream(testOutputStream));
    }
    
    /**
     * Getter for the revision identifier used by SVN.
     * 
     * @return The SVN revision identifier.
     */
    SVNRevision getRevision() {
        return revision;
    }
    
    /**
     * Getter for the SVN repository.
     * @return The path to the SVN repository.
     */
    File getRepository() {
        return repository;
    }
    
    @Override
    public String getUniqueIdentifier() {
        return "rev_" + revision;
    }
    
    /*
     * Make some methods visible to the PostCommitHook.
     */
    
    @Override
    protected void setCommitAuthor(String commitAuthor) {
        super.setCommitAuthor(commitAuthor);
    }
    
    @Override
    protected void setCommitComment(String commitComment) {
        super.setCommitComment(commitComment);
    }
    
    @Override
    protected void setChangedPathConfigurations(Set<PathConfiguration> changedPathConfigurations) {
        super.setChangedPathConfigurations(changedPathConfigurations);
    }
    
    @Override
    protected Set<PathConfiguration> getChangedPathConfigurations() {
        return super.getChangedPathConfigurations();
    }

    @Override
    public Stage getStage() {
        return Stage.POST_COMMIT;
    }

}
