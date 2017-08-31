/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @Test(expected = RuntimeException.class)
    public void testTooMuchConsume() {
        ParseState state = new ParseState("foo");

        assertThat( state.consume() ).isEqualTo( "foo" );
        state.consume();
    }
}
