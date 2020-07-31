package de.uni_hildesheim.sse.javaSvnHooks.tests.junit;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.FailureMessage;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.Importance;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestResult;


/**
 * Listener for JUnit tests. Collects error messages of failed tests.
 * 
 * @author Adam Krafczyk
 */
public class JUnitRunListener extends RunListener {
    
    private Description currentTest;
    private TestResult testResult;
    
    /**
     * Creates a new JUnitRunListener for JUnit tests.
     * @param testResult The testResult to add {@link TestFailure}s to.
     */
    public JUnitRunListener(TestResult testResult) {
        this.testResult = testResult;
    }
    
    @Override
    public void testStarted(Description description) {
        currentTest = description;
    }
    
    @Override
    public void testFailure(Failure failure) {
        Importance importance = currentTest.getAnnotation(Importance.class);
        boolean mandatory = true;
        if (null != importance) {
            mandatory = importance.mandatory();
        }
        
        FailureMessage messageAnnoation = currentTest.getAnnotation(
                FailureMessage.class);
        
        String message;
        if (messageAnnoation != null) {
            message = messageAnnoation.message();
        } else {
            Logger.INSTANCE.logException(failure.getException(), false);
            message = "Unerwarteter Fehler: " +  failure.getException().toString();
        }
        
        TestFailure testFauilure = new TestFailure(message, mandatory, null,
                -1);
        testResult.addTestFailure(testFauilure);
    }
    
}
