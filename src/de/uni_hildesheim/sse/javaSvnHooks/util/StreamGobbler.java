package de.uni_hildesheim.sse.javaSvnHooks.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.ITestMessageFilter;

/**
 * An internal accumulator for reading input streams e.g. of 
 * processes. An instance of this class observes a specified
 * input stream, may pass through the information to another 
 * stream, to return the accumulated contents as a <i>String</i>
 * and to optionally inform a message filter instance..
 * 
 * @since 1.00
 */
public class StreamGobbler extends Thread {

    /**
     * The stream to be observed.
     * 
     * @since 1.00
     */
    private InputStream is;

    /**
     * An optional output instance to which the 
     * characters on {@link #is} should be
     * passed through.
     * 
     * @since 1.00
     */
    private PrintStream passThrough;

    /**
     * The message filter to be informed about
     * completely read lines.
     * 
     * @since 1.00
     */
    private ITestMessageFilter filter;

    /**
     * The accumulated result.
     * 
     * @since 1.00
     */
    private StringBuffer result = new StringBuffer();
    
    /**
     * Whether the gobbler ist still running.
     */
    private boolean running = true;

    /**
     * Creates a new stream gobbler.
     * 
     * @param is the input stream to be observed
     * @param passThrough an optional output instance to which
     *        the characters on {@link #is} should be
     *        passed through
     * @param filter an optional message filter to be 
     *        informed on completely read lines
     * 
     * @since 1.00
     */
    public StreamGobbler(InputStream is, PrintStream passThrough,
        ITestMessageFilter filter) {
        this.is = is;
        this.passThrough = passThrough;
        this.filter = filter;
        start();
    }

    /**
     * Observes the stream, reads in the characters from
     * the stream and aggregates the characters into 
     * {@link #result}.
     * 
     * @since 1.00
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int lineNr = 1;
            while ((line = br.readLine()) != null) {
                // strange line needed because sometimes characters 
                // above 128 occurred, which leaded to strange problems
                line = XmlUtilities.reduceToReadable(line);
                if (null != filter) {
                    line = filter.filterMessage(lineNr, line);
                }
                handleLine(line);
                lineNr++;
            }
        } catch (IOException ioe) {
            Logger.INSTANCE.logException(ioe, false);
        }
        running = false;
        handleLine(filter.done());
    }
    
    /**
     * Handles a processed line, i.e., appends it to the result or emits it to {@link #passThrough}.
     * 
     * @param line the line, may be <b>null</b>
     */
    private void handleLine(String line) {
        if (null != line) {
            result.append(line);
            result.append('\n');
            if (null != passThrough) {
                passThrough.println(line);
            }
        }
    }

    /**
     * Returns the accumulated contents of this gobbler.
     * 
     * @return the contents
     * 
     * @since 1.00
     */
    public String toString() {
        return result.toString();
    }
    
    /**
     * Returns whether the gobbler is still running (in parallel).
     * 
     * @return <code>true</code> for running, <code>false</code> else
     */
    public boolean isRunning() {
        return running;
    }
    
}