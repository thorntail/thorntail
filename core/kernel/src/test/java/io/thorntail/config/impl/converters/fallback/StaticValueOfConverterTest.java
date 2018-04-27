package io.thorntail.config.impl.converters.fallback;

import org.fest.assertions.Assertions;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

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
