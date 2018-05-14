package io.thorntail.config.impl.converters.fallback;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class EnumValueOfConverterTest {

    @Test
    public void testNonEnum() {
        EnumValueOfConverter converter = new EnumValueOfConverter();
        Assertions.assertThat(converter.convert("hi", Integer.class)).isNull();
    }

    @Test
    public void testValidEnum() {
        EnumValueOfConverter converter = new EnumValueOfConverter();
        Assertions.assertThat(converter.convert("SECONDS", TimeUnit.class)).isEqualTo(TimeUnit.SECONDS);
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
