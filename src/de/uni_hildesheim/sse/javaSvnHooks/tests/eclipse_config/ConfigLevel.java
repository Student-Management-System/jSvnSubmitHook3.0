package de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config;

/**
 * Defines some basic configuration levels.
 * 
 * @author Holger Eichelberger
 */
enum ConfigLevel {
    
    /**
     * Value should be ignored.
     * 
     * @since 1.20
     */
    NONE,

    /**
     * Value should be treated as a warning.
     * 
     * @since 1.20
     */
    WARNING,

    /**
     * Value should be treated as an error.
     * 
     * @since 1.20
     */
    ERROR, 
}