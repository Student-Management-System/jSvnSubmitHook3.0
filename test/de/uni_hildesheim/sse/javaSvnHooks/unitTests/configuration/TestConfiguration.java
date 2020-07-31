package de.uni_hildesheim.sse.javaSvnHooks.unitTests.configuration;

import java.io.File;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;

/**
 * A simple implementation of the {@link Configuration} class for testing purposes.
 * 
 * @author Adam Krafczyk
 */
class TestConfiguration extends Configuration {

    private Stage stage;
    
    /**
     * Creates a {@link Configuration} that will read the configuration file from testdata/.
     */
    TestConfiguration() {
        this(Stage.POST_COMMIT); // legacy
    }
    
    /**
     * Creates a {@link Configuration} that will read the configuration file from testdata/.
     */
    TestConfiguration(Stage stage) {
        this.stage = stage;
        readConfig(new File("testdata/configTest/hook.properties"));
    }
    
    @Override
    public String getUniqueIdentifier() {
        return "test_" + String.valueOf(Math.random());
    }

    @Override
    public Stage getStage() {
        return stage;
    }

}
