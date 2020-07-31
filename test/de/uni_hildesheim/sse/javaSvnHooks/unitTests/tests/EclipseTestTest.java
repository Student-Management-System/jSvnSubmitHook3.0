package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config.EclipseConfigurationTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javac.JavacTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;

/**
 * Tests the {@link EclipseTest}.
 * 
 * @author Holger Eichelberger
 */
public class EclipseTestTest extends AbstractTest {

    private static final File BASE = new File("testdata/eclipseTest");
    
    @Override
    protected File getBase() {
        return BASE;
    }
    
    @Override
    protected TestConfiguration createConfig() {
        TestConfiguration config = createConfig("sub.properties", 
                new EclipseConfigurationTest(), new JavacTest());
            config.setHookDirectory(BASE.getAbsoluteFile());
        return config;
    }

    /**
     * Tests sub1, shall fail.
     */
    @Test
    public void testEclipseSub1() {
        String res = executeTest("/sub1", false, createConfig());
        Assert.assertTrue("Message must refer to Eclipse configuration", 
            res.indexOf("Eclipse configuration not present") > 0);
    }

    /**
     * Tests sub1, shall pass.
     */
    @Test
    public void testEclipseSub2() {
        String res = executeTest("/sub2", false, createConfig());
        Assert.assertTrue("Message must complain about src/bin", 
            res.indexOf("Output path (usually called src") > 0);
    }

}
