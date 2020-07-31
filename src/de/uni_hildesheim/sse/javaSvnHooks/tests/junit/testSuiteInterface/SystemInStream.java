package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simulates User input via {@link System#in}.
 * @author El-Sharkawy
 *
 */
class SystemInStream extends InputStream {
    
    /**
     * Delimiter to mark the end of one user input.
     */
    private static final String END_OF_INPUT = "\n";

    private Deque<String> stack;
    private byte[] currentValues;
    private int index;
    
    /**
     * Sole / default constructor for this class.
     */
    public SystemInStream() {
        stack = new ArrayDeque<String>();
        currentValues = null;
        index = 0;
    }
    
    /**
     * Deletes all (outstanding) user inputs from this Stream.
     */
    public void reset() {
        stack.clear();
        currentValues = null;
        index = 0;
    }
    
    /**
     * Passes one &quot;user input&quot; to {@link System#in}.
     * @param value The value (e.g. String or Integer) which shall be passed to
     * {@link System#in} as an user input.
     */
    public void input(Object value) {
        if (null != value) {
            stack.addLast(value + END_OF_INPUT);
        }
    }
    
    /**
     * Passes multiple &quot;user inputs&quot; to {@link System#in}.
     * @param values The values (e.g. String or Integer) which shall be passed
     * to {@link System#in} as user inputs.
     */
    public void input(Object... values) {
        for (int i = 0; i < values.length; i++) {
            input(values[i]);
        }
    }
    
    @Override
    public synchronized int read() throws IOException {
        if (null == currentValues) {
            String value = !stack.isEmpty() ? stack.removeFirst() : "";
            currentValues = value.getBytes();
            index = 0;
        }
        
        int value = 0;
        if (null != currentValues && index < currentValues.length) {
            value = currentValues[index];
        }
        index++;
        if (index == currentValues.length) {
            currentValues = null;
        }
        
        return value;
    }
    
    @Override
    public synchronized int read(byte[] bytes, int off, int len)
        throws IOException {
        
        if (null == currentValues) {
            String value = !stack.isEmpty() ? stack.removeFirst() : "";
            currentValues = value.getBytes();
            index = 0;
        }
        return super.read(bytes, off, currentValues.length);
    }
    
    @Override
    public int available() throws IOException {
        return null != currentValues
                ? (currentValues.length - index) : super.available();
    }

    @Override
    public String toString() {
        return stack.toString().replace(END_OF_INPUT, "");
    }
}
