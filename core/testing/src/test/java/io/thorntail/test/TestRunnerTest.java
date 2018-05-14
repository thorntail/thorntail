package io.thorntail.test;

import javax.inject.Inject;

import io.thorntail.Thorntail;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bob on 1/19/18.
 */
@RunWith(ThorntailTestRunner.class)
public class TestRunnerTest {

    @Test
    public void testInjectSystem() {
        assertThat(this.prop).isEqualTo("tacos");
        Assertions.assertThat(this.system).isNotNull();
        assertThat(this.testInjectable).isNotNull();
    }

    @Inject
    private Thorntail system;

    @Inject
    private InjectMe testInjectable;

    @Inject
    @ConfigProperty(name="random.test.prop")
    private String prop;
}
