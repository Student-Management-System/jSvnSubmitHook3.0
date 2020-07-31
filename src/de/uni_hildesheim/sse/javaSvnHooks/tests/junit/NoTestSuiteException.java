package de.uni_hildesheim.sse.javaSvnHooks.tests.junit;

/**
 * Thrown if the hook does not find a specified test suite. Replaces initial
 * ClassNotFoundException to separate problem cases.
 * @author Holger Eichelberger
 */
public class NoTestSuiteException extends Exception {

    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a {@link NoTestSuiteException} for the message.
     * 
     * @param message the failure message.
     */
    public NoTestSuiteException(String message) {
        super(message);
    }

    /**
     * Creates a {@link NoTestSuiteException} for the given cause.
     * 
     * @param cause the failure cause
     */
    public NoTestSuiteException(Throwable cause) {
        super("", cause);
    }

}
