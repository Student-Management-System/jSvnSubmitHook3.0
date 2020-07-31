package de.uni_hildesheim.sse.javaSvnHooks.util;

/**
 * Holds constants for exit codes of hooks.
 * 
 * @author Adam Krafczyk
 */
public final class ExitCodes {

    /**
     * Exit code when the commit should be accepted.
     */
    public static final int EXIT_SUCCESS = 0;
    
    /**
     * Exit code when the commit should be rejected.
     */
    public static final int EXIT_FAIL = 1;
    
    /**
     * Exit code when an error in this tool appears (Could be equal to
     * {@link ExitCodes#EXIT_SUCCESS} or {@link ExitCodes#EXIT_FAIL}
     * or a special other value).
     */
    public static final int EXIT_ERROR = 2;
    
}
