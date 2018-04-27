package io.thorntail.config.impl.interpolation;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import io.thorntail.config.impl.sources.MapConfigSource;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by bob on 4/2/18.
 */
public class InterpolatorTest {

    @Before
    public void setUp() {
        this.map = new HashMap<>();
        this.map.put( "var.one", "ONE");
        this.map.put( "var.two", "TWO");
        this.map.put( "var.three", "${var.one:42}");
        this.map.put( "var.four", "${missing:${var.three}}");
        this.map.put( "var.recurse.1", "${var.recurse.1}");
        this.map.put( "var.recurse.a", "${var.recurse.b}");
        this.map.put( "var.recurse.b", "${var.recurse.c}");
        this.map.put( "var.recurse.c", "${var.recurse.a}");
        this.configSource = new MapConfigSource("test", this.map );
        this.config = ConfigProviderResolver.instance().getBuilder().withSources(this.configSource).build();
        this.interpolator = new Interpolator(config);
    }

    @Test
    public void testNull() {
        assertThat(interpolate(null)).isNull();
    }

    @Test
    public void testEmptyString() {
        assertThat(interpolate( "" )).isEqualTo("");
    }

    @Test
    public void testSimpleVar() {
        assertThat(interpolate("${var.one}")).isEqualTo("ONE");
    }
    @Test
    public void testMultiVars() {
        assertThat(interpolate("${var.one}${var.two}")).isEqualTo("ONETWO");
    }

    @Test
    public void testMixed() {
        assertThat(interpolate("cheese${var.one}-${var.two}tacos")).isEqualTo("cheeseONE-TWOtacos");
    }

    @Test
    public void testVarWithDefaultNotUsed() {
        assertThat(interpolate("${var.one:42}")).isEqualTo( "ONE");
    }

    @Test
    public void testVarWithDefaultUsed() {
        assertThat(interpolate("${missing.var:42}")).isEqualTo("42");
    }

    @Test
    public void testDeeply() {
        assertThat(interpolate("${var.four}")).isEqualTo("ONE");
    }

    @Test
    public void testSimpleRecursion() {
        try {
            interpolate("${var.recurse.1}");
            fail( "Should have thrown" );
        } catch (IllegalArgumentException e) {
            // expected and correct
        }
    }

    @Test
    public void testDeepRecursion() {
        try {
            interpolate("${var.recurse.a}");
            fail( "Should have thrown" );
        } catch (IllegalArgumentException e) {
            // expected and correct
        }
    }

    @Test
    public void testVarMissingNoDefault() {
        try {
            interpolate("${missing.var}");
            fail("Should have thrown");
        } catch (NoSuchElementException e) {
            //expected and correct
        }

    }

    private String interpolate(String str) {
        return this.interpolator.interpolate(str);
    }


    private Map<String,String> map;

    private MapConfigSource configSource;

    private Config config;

    private Interpolator interpolator;
}
