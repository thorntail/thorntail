package io.thorntail.testsuite.config;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.thorntail.test.ThorntailTestRunner;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class ConfigTest {

    @Test
    public void test() {
        /*
        List<String> value = this.optionalList.get();
        assertThat(value).contains("foo");
        assertThat(value).contains("bar");
        assertThat(value).contains("baz");
        */

        assertThat(requiredList).contains("tacos");
        assertThat(requiredList).contains("cheese");

        assertThat(this.notPresentList.isPresent()).isFalse();
    }

    /*
    @Inject
    @ConfigProperty(name = "mp.config.optional.string.list")
    Optional<List<String>> optionalList;
    */

    @Inject
    @ConfigProperty(name = "mp.config.required.string.list")
    List<String> requiredList;

    @Inject
    @ConfigProperty(name = "mp.config.optional.string.optional.not.present")
    Optional<List<String>> notPresentList;
}
