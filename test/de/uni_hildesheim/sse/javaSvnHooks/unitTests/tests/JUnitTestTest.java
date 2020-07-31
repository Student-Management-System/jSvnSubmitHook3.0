package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config.EclipseConfigurationTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.JunitTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;

/**
 * Tests the {@link JunitTest}.
 * 
 * @author Holger Eichelberger
 */
public class JUnitTestTest extends AbstractTest {

    private static final File BASE = new File("testdata/junitTest");

    @Override
    protected File getBase() {
        return BASE;
    }

    /**
     * Tests both test folders (ok, fail) with a proper configuration including regex and message.
     */
    @Test
    public void testJUnit() {
        TestConfiguration config = createConfig("hook.properties", 
            new EclipseConfigurationTest(), new JavacTest(), new JunitTest());
        config.setHookDirectory(BASE.getAbsoluteFile());
        config.setProperty("junitSuitePath", BASE.getAbsoluteFile().toString());
        String res = executeTest("/submission", true, config);
        assertEmpty(res);
    }

}
