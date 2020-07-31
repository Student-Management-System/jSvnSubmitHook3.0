package de.uni_hildesheim.sse.javaSvnHooks.tests.junit;

import java.io.File;

/**
 * Thrown if a ClassRegistry doesn't find a compiled class, although
 * the .java file was compiled. This implies that the package declaration
 * in the java file is invalid.
 * @author Adam Krafczyk
 */
public class WrongPackageException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private File classFile;
    
    /**
     * Creates a {@link WrongPackageException} for the given class name.
     * 
     * @param classFile The class file that cannot be loaded.
     */
    public WrongPackageException(File classFile) {
        this.classFile = classFile;
    }
    
    /**
     * Gets the name of the class that likely has an invalid package declaration.
     * 
     * @return The name of the class that couldn't be found.
     */
    public File getClassFile() {
        return classFile;
    }

}
