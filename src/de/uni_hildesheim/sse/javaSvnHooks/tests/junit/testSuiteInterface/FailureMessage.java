package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Message to be shown when the test fails.
 * 
 * @author Adam Krafczyk
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FailureMessage {

    /**
     * The message describing the failure.
     * 
     * @return the message
     */
    String message() default "";
    
}
