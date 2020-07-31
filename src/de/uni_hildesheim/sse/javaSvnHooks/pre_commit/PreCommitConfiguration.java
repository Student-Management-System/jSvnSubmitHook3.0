package de.uni_hildesheim.sse.javaSvnHooks.pre_commit;

import java.io.File;
import java.util.Set;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;

/**
 * The pre-commit specific implementation of the configuration.
 * 
 * @author Adam Krafczyk
 */
class PreCommitConfiguration extends Configuration {

    private File repository;
    
    private String transaction;
    
    /**
     * Creates a {@link PreCommitConfiguration} with the given parameters.
     * 
     * @param repository The path to the SVN repository.
     * @param transaction The transaction identifier used by SVN.
     * @param hooksDir The path to the directory of this hook.
     */
    PreCommitConfiguration(String repository, String transaction, String hooksDir) {
        this.repository = new File(repository);
        this.transaction = transaction;
        
        setHookDir(new File(hooksDir));
        
        File configFile = new File(hooksDir, "hook.properties");
        readConfig(configFile);
        
        setTestOutputStream(System.err);
    }
    
    /**
     * Getter for the transaction identifier.
     * 
     * @return The transaction identifier used by SVN.
     */
    String getTransaction() {
        return transaction;
    }
    
    /**
     * Getter for the SVN repository.
     * 
     * @return The SVN repository.
     */
    File getRepository() {
        return repository;
    }
    
    @Override
    public String getUniqueIdentifier() {
        return "trans_" + transaction;
    }
    
    /*
     * Make some methods visible to the PreCommitHook.
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
        return Stage.PRE_COMMIT;
    }
    
}
