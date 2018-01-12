package org.jboss.unimbus.config.mp.converters.fallback;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class EnumValueOfConverterTest {

    @Test
    public void testNonEnum() {
        EnumValueOfConverter converter = new EnumValueOfConverter();
        assertThat(converter.convert( "hi", Integer.class)).isNull();
    }

    @Test
    public void testValidEnum() {
        EnumValueOfConverter converter = new EnumValueOfConverter();
        assertThat(converter.convert("SECONDS", TimeUnit.class)).isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void testInvalidEnum() {
        EnumValueOfConverter converter = new EnumValueOfConverter();
        try {
            converter.convert("BOB", TimeUnit.class);
            fail("should have throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
