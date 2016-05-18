package org.wildfly.swarm.cli;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
public class ParseStateTest {

    @Test
    public void testEmptyState() {
        ParseState state = new ParseState();
        assertThat(state.la()).isNull();
    }

    @Test
    public void testOne() {
        ParseState state = new ParseState( "--help" );
        assertThat(state.la()).isEqualTo( "--help" );
        assertThat(state.la()).isEqualTo( "--help" );
        assertThat(state.la()).isEqualTo( "--help" );
        assertThat(state.la()).isEqualTo( "--help" );
        assertThat(state.consume()).isEqualTo( "--help" );
        assertThat(state.la()).isNull();
    }

    @Test
    public void testMany() {
        ParseState state = new ParseState(
                "--help",
                "--version",
                "foo",
                "-Dbar=cheese",
                "-Dfoo"
        );

        assertThat( state.la() ).isEqualTo( "--help" );
        assertThat( state.consume() ).isEqualTo( "--help" );

        assertThat( state.la() ).isEqualTo( "--version" );
        assertThat( state.consume() ).isEqualTo( "--version" );

        assertThat( state.la() ).isEqualTo( "foo" );
        assertThat( state.consume() ).isEqualTo( "foo" );

        assertThat( state.la() ).isEqualTo( "-Dbar=cheese" );
        assertThat( state.consume() ).isEqualTo( "-Dbar=cheese" );

        assertThat( state.la() ).isEqualTo( "-Dfoo" );
        assertThat( state.consume() ).isEqualTo( "-Dfoo" );

        assertThat( state.la() ).isNull();
    }

    @Test
    public void testTooMuchConsume() {
        ParseState state = new ParseState("foo");

        assertThat( state.consume() ).isEqualTo( "foo" );

        try {
            state.consume();
            fail( "should have thrown an exception for over-consume()");
        } catch (RuntimeException e) {
            // expected and correct
            e.printStackTrace();
        }

    }
}
