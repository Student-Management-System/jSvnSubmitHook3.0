package de.uni_hildesheim.sse.javaSvnHooks.unitTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.javaSvnHooks.unitTests.configuration.ConfigurationTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.CheckstyleTestTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.EclipseTestTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.FileNameTestTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.FileSizeTestTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.JUnitTestTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.JavacTestTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests.ScenarioTests;

/**
 * Test suite for jUnit tests.
 * 
 * @author Adam Krafczyk
 */
@RunWith(Suite.class)
@SuiteClasses({
    ConfigurationTest.class,
    JavacTestTest.class,
    FileNameTestTest.class,
    FileSizeTestTest.class,
    CheckstyleTestTest.class,
    JUnitTestTest.class,
    EclipseTestTest.class,
    ScenarioTests.class
    })
public class AllTests {
}
