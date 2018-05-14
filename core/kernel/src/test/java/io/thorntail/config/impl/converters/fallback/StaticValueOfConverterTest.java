package io.thorntail.config.impl.converters.fallback;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaticValueOfConverterTest {

    @Test
    public void testValid() {
        StaticValueOfConverter converter = new StaticValueOfConverter();
        Assertions.assertThat(converter.convert("4", Integer.class)).isEqualTo(4);
    }

    /*
    @Test
    public void testInvalid() {
        StaticValueOfConverter converter = new StaticValueOfConverter();
        assertThat(converter.convert( "four", Integer.class)).isNull();
    }
    */
}
