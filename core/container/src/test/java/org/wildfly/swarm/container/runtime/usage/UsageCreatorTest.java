package org.wildfly.swarm.container.runtime.usage;

import java.util.Properties;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 8/30/17.
 */
public class UsageCreatorTest {

    @Test
    public void testNoUsage() throws Exception {
        UsageCreator creator = new UsageCreator(UsageProvider.ofString(null), null);
        assertThat(creator.getUsageMessage()).isNull();
    }

    @Test
    public void testUsageWithNoVariableReplacement() throws Exception {
        UsageCreator creator = new UsageCreator(UsageProvider.ofString("I LIKE TACOS"), null);
        assertThat(creator.getUsageMessage()).isEqualTo("I LIKE TACOS");
    }

    @Test
    public void testUsageWithVariableReplacement() throws Exception {
        Properties props = new Properties();
        props.setProperty("swarm.http.port", "8080");
        props.setProperty("swarm.app.thingy", "TACOS");
        UsageVariableSupplier supplier = UsageVariableSupplier.ofProperties(props);

        UsageCreator creator = new UsageCreator(UsageProvider.ofString("I LIKE ${swarm.app.thingy}\nOn port ${swarm.http.port}.\nTry it now!"), supplier);
        String message = creator.getUsageMessage();
        assertThat(message).isEqualTo("I LIKE TACOS\nOn port 8080.\nTry it now!");
    }

    @Test
    public void testUsageWithVariableReplacementSomeUnknown() throws Exception {
        Properties props = new Properties();
        props.setProperty("swarm.http.port", "8080");
        UsageVariableSupplier supplier = UsageVariableSupplier.ofProperties(props);

        UsageCreator creator = new UsageCreator(UsageProvider.ofString("I LIKE ${swarm.app.thingy}\nOn port ${swarm.http.port}.\nTry it now!"), supplier);
        String message = creator.getUsageMessage();
        assertThat(message).isEqualTo("I LIKE ${swarm.app.thingy}\nOn port 8080.\nTry it now!");
    }
}
