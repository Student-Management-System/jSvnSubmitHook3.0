package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.io.FilePermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.PropertyPermission;

/**
 * A security manager to sandbox executed code. The message of the thrown
 * {@link SecurityException}s will contain the name of {@link Permission} that
 * was not granted.
 * 
 * @author Adam Krafczyk
 */
public class TestSuiteSecurityManager extends SecurityManager {
    
    private boolean calledByThis = false;
    
    private Permissions allowedPermissions;
    
    /**
     * Creates a new {@link TestSuiteSecurityManager}.
     * 
     * @param tmpPath The path to the temporary execution directory. Submitted
     * code will be allowed to read, write and delete files in this directory
     * and all subdirectories.
     */
    public TestSuiteSecurityManager(String tmpPath) {
        allowedPermissions = new Permissions();
        
        // Allow reading, writing and deleting files in the temp directory
        allowedPermissions.add(new FilePermission(tmpPath + "/-",
                "read,write,delete"));
        
        // Allow reading some settings (user.dir is set to tmpPath in JunitTest)
        allowedPermissions.add(new PropertyPermission("user.dir", "read"));
        allowedPermissions.add(new PropertyPermission("file.separator",
                "read"));
        allowedPermissions.add(new PropertyPermission("line.separator",
                "read"));
        allowedPermissions.add(new PropertyPermission("path.separator",
                "read"));
    }
    
    /**
     * Sets this security manager to be the current {@link SecurityManager} of
     * the {@link System}.
     */
    public void set() {
        System.setSecurityManager(this);
    }
    
    /**
     * Resets the previous {@link SecurityManager} of the {@link System}. This
     * is the only way to stop using this {@link SecurityManager}.
     */
    public void unset() {
        synchronized (this) {
            calledByThis = true;
            System.setSecurityManager(null);
            calledByThis = false;
        }
    }
    
    @Override
    public void checkPermission(Permission perm) {
        if (perm instanceof RuntimePermission) {
            if (perm.getName().equals("setSecurityManager")) {
                if (calledByThis) {
                    return;
                }
            }
        }
        if (!allowedPermissions.implies(perm)) {
            throw new SecurityException(perm.getClass().getName());
        }
    }
    
    @Override
    public void checkPermission(Permission perm, Object context) {
        checkPermission(perm);
    }
    
}
