package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

/**
 * Represents a failed test.
 * 
 * @author Adam Krafcuzyk
 */
public class TestFailure {

    private String message;
    private boolean mandatory;
    private String file;
    private int line;
    
    /**
     * Creates a {@link TestFailure} for a failed test. 
     * @param message A description of the failure; will be shown to the user.
     * @param mandatory Whether the test is mandatory or not.
     * @param file The file name of the class that caused the test to fail;
     *   may be <code>null</code>.
     * @param line The line number in the source file that caused the test to
     * fail; -1 if not known
     */
    public TestFailure(String message, boolean mandatory, String file,
            int line) {
        this.message = message;
        this.mandatory = mandatory;
        this.file = file;
        this.line = line;
    }
    
    /**
     * Getter for the failure description.
     * @return A string describing the failure.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Getter whether the failed test if mandatory or not.
     * @return <code>true</code> if the test is mandatory to pass.
     */
    public boolean isMandatory() {
        return mandatory;
    }
    
    /**
     * Getter for the name of the file that caused the test to fail.
     * @return The file name of the class. May be <code>null</code>.
     */
    public String getFileName() {
        return file;
    }
    
    /**
     * Getter for the line number in the source file that caused the test to
     * fail.
     * @return The line number; -1 if not known.
     */
    public int getLine() {
        return line;
    }
    
}
