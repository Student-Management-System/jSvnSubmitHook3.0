package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects {@link TestFailure}s.
 * 
 * @author Adam Krafczyk
 */
public class TestResult {
    
    private List<TestFailure> testFailures;
    
    /**
     * Constructs a new, empty {@link TestResult}.
     */
    public TestResult() {
        testFailures = new ArrayList<TestFailure>();
    }
    
    /**
     * Adds a new {@link TestFailure} representing a failed test.
     * @param testFailure The new {@link TestFailure}.
     */
    public void addTestFailure(TestFailure testFailure) {
        testFailures.add(testFailure);
    }
    
    /**
     * Returns a list of {@link TestFailure} for all failed tests.
     * @return An unmodifiable {@link List} of {@link TestFailure}s.
     */
    public List<TestFailure> getTestFailures() {
        return Collections.unmodifiableList(testFailures);
    }
    
}
