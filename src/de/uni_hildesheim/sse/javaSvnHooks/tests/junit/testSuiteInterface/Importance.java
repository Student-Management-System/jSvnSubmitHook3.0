package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Whether the test is mandatory or not.
 * 
 * @author Adam Krafczyk
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Importance {
    
    /**
     * Whether this test is mandatory to pass or not.
     * 
     * @return whether passing is mandatory
     */
    boolean mandatory() default true;
    
}
