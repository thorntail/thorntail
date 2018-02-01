package org.jboss.unimbus.test;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.UNimbus;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 1/19/18.
 */
@RunWith(UNimbusTestRunner.class)
public class TestRunnerTest {

    @Test
    public void testInjectSystem() {
        assertThat(this.prop).isEqualTo("tacos");
        assertThat(this.system).isNotNull();
        assertThat(this.testInjectable).isNotNull();
    }

    @Inject
    private UNimbus system;

    @Inject
    private InjectMe testInjectable;

    @Inject
    @ConfigProperty(name="random.test.prop")
    private String prop;
}
