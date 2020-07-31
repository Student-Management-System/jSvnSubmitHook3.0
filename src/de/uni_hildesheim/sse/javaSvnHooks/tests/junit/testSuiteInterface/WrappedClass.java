package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

/**
 * A wrapper arround a {@link Class} object with java file name and content.
 * 
 * @author Adam Krafczyk
 */
public class WrappedClass {

    private Class<?> wrappedClass;
    private String file;
    private String content;
    
    /**
     * Constructs a new {@link WrappedClass} for the given class.
     * 
     * @param wrappedClass The {@link Class} to be wrapped.
     * @param file The path to the java file of the class (relative to project).
     * @param content The content of the java file of the class.
     */
    WrappedClass(Class<?> wrappedClass, String file, String content) {
        this.wrappedClass = wrappedClass;
        this.file = file;
        this.content = content;
    }
    
    /**
     * Getter for the {@link Class}.
     * @return The {@link Class} object.
     */
    public Class<?> getWrappedClass() {
        return wrappedClass;
    }
    
    /**
     * Getter for the java file name of the wrapped class. 
     * @return The file name relative to the submitted directory. May be
     * <code>null</code>.
     */
    public String getFileName() {
        return file;
    }
    
    /**
     * Getter for the content of the java file.
     * @return The content of the java file. May be <code>null</code>.
     */
    public String getContent() {
        return content;
    }
    
}
