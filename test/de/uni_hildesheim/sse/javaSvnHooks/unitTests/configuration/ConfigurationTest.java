package de.uni_hildesheim.sse.javaSvnHooks.unitTests.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;

/**
 * A test class for the {@link Configuration} class.
 * 
 * @author Adam Krafczyk
 */
public class ConfigurationTest {
    
    private Configuration configuration;
    
    /**
     * Reads the {@link Configuration} object from the configuration file in testdata/.
     */
    @Before
    public void setUp() {
        configuration = new TestConfiguration();
    }
    
    /**
     * Tests whether configuration items are read correctly.
     */
    @Test
    public void testConfiguration() {
        Assert.assertTrue(configuration.produceXmlOutput());
        Assert.assertEquals(new File("someDir/tmp"), configuration.getTempDir());
        Assert.assertEquals("foobar", configuration.getStringProperty("someTestValue", "notFoobar"));
        Assert.assertEquals("notFoobar", configuration.getStringProperty("someNonExistantValue", "notFoobar"));
    }
    
    /**
     * Tests whether all path configurations are read correctly.
     */
    @Test
    public void testReadPathConfigurations() {
        List<PathConfiguration> configs = new ArrayList<PathConfiguration>();
        Iterator<PathConfiguration> iter = configuration.pathConfigurations();
        while (iter.hasNext()) {
            configs.add(iter.next());
        }
        
        Assert.assertEquals(2, configs.size());
        
        PathConfiguration pc0 = configs.get(0);
        PathConfiguration pc1 = configs.get(1);
        
        if (!pc0.getPath().equals("/Path0")) {
            PathConfiguration tmp = pc0;
            pc0 = pc1;
            pc1 = tmp;
        }
        
        Assert.assertEquals("/Path0", pc0.getPath());
        Assert.assertEquals("/Path1", pc1.getPath());
        
        Assert.assertEquals(configuration, pc0.getGlobalConfiguration());
        Assert.assertEquals(configuration, pc1.getGlobalConfiguration());
        
        Assert.assertEquals("foo-bar", pc0.getStringProperty("someTestValue", "notFoobar"));
    }

}
