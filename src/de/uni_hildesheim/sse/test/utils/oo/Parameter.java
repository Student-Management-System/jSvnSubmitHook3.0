package de.uni_hildesheim.sse.test.utils.oo;

/**
 * Wraper for runtime tests. Represents a parameter (data type + value).
 * @author El-Sharkawy
 *
 */
public class Parameter {
    private Class<?> type;
    private Object value;

    /**
     * Sole constructor of this class, creates a new Parameter.
     * @param type The data type of the parameter
     * @param value The value of the parameter
     */
    public Parameter(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Getter for the data type.
     * @return the data type of the parameter.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Getter for the value.
     * @return the value of the parameter.
     */
    public Object getValue() {
        return value;
    }
    
    
}
