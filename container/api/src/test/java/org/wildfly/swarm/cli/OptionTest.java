package org.wildfly.swarm.cli;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
public class OptionTest {

    @Test
    public void testLongNoArgs() {
        ParseState state = new ParseState("--help");

        AtomicBoolean helpSet = new AtomicBoolean(false);

        Option help = new Option((cmd, value) -> {
            helpSet.set(true);
        }).withLong("help");

        assertThat(help.parse(state, null)).isTrue();

        assertThat(helpSet.get()).isTrue();
    }

    @Test
    public void testShortNoArgs() {
        ParseState state = new ParseState("-h");

        AtomicBoolean helpSet = new AtomicBoolean(false);

        Option help = new Option((cmd, value) -> {
            helpSet.set(true);
        }).withShort('h');

        assertThat(help.parse(state, null)).isTrue();

        assertThat(helpSet.get()).isTrue();
    }

    @Test
    public void testShortWithValueTogether() {
        ParseState state = new ParseState("-c=standalone.xml");

        AtomicReference<String> config = new AtomicReference<>(null);

        Option configOpt = new Option((cmd, value) -> {
            config.set(value);
        }).withShort('c').hasValue("<value>");

        assertThat(configOpt.parse(state, null)).isTrue();

        assertThat(config.get()).isEqualTo("standalone.xml");
    }

    @Test
    public void testShortWithValueApart() {
        ParseState state = new ParseState("-c", "standalone.xml");

        AtomicReference<String> config = new AtomicReference<>(null);

        Option configOpt = new Option((cmd, value) -> {
            config.set(value);
        }).withShort('c').hasValue("<value>");

        assertThat(configOpt.parse(state, null)).isTrue();

        assertThat(config.get()).isEqualTo("standalone.xml");
    }

    @Test
    public void testShortWithValueMissing() {
        ParseState state = new ParseState("-c");

        AtomicReference<String> config = new AtomicReference<>(null);

        Option configOpt = new Option((cmd, value) -> {
            config.set(value);
        }).withShort('c').hasValue("<value>");

        try {
            configOpt.parse(state, null);
            fail("should have throw a missing-argument exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("-c requires an argument");
        }
    }

    @Test
    public void testPropertiesLike() {

        ParseState state = new ParseState("-Dfoo", "-Dbar=cheese");

        Properties props = new Properties();

        Option configOpt = new Option((cmd, value) -> {
            String[] keyValue = value.split("=");

            if (keyValue.length == 1) {
                props.setProperty(keyValue[0], "true");
            } else {
                props.setProperty(keyValue[0], keyValue[1]);
            }
        }).withShort('D').hasValue("<value>").valueMayBeSeparate(false);

        while (configOpt.parse(state, null)) {
            // do it again
        }

        assertThat( props ).hasSize( 2 );

        System.err.println( props );

        assertThat( props.getProperty( "foo" ) ).isEqualTo( "true" );
        assertThat( props.getProperty( "bar" ) ).isEqualTo( "cheese" );

    }
}
