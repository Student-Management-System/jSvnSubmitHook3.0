package de.uni_hildesheim.sse.javaSvnHooks.logging;

import java.io.PrintStream;

/**
 * Class for logging.
 * 
 * @author Adam Krafczyk
 */
public class Logger {

    public static final Logger INSTANCE = new Logger();
    
    private PrintStream out = System.out;
    
    /**
     * Private since this is a singleton.
     */
    private Logger() {
    }
    
    /**
     * Changes the {@link PrintStream} where log messages will be written to.
     * 
     * @param out The new {@link PrintStream}.
     */
    public void setOutputStream(PrintStream out) {
        this.out = out;
    }
    
    /**
     * Logs a message.
     * 
     * @param message The message to be logged.
     */
    public void log(String message) {
        out.println(message);
    }
    
    /**
     * Logs an exception.
     * 
     * @param exception The exception to be logged.
     * @param handled Intentionally log just the top parts of the stack 
     *     trace to indicate that this is a handled exception.
     */
    public void logException(Throwable exception, boolean handled) {
        if (handled) {
            out.println("Handled " + exception.getClass().getName() + ": " 
                + exception.getMessage());
            StackTraceElement[] trace = exception.getStackTrace();
            if (null != trace) {
                for (int t = trace.length - 1; t >= Math.max(0, trace.length - 2); t--) {
                    out.println(" " + trace[t]);
                }
            }
        } else {
            exception.printStackTrace(out);
        }
    }

}
