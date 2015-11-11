package org.wildfly.swarm.bootstrap.util;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class LayoutTest {

    @Test
    public void testSingletoness() throws Exception {
        assertThat( Layout.getInstance() ).isSameAs( Layout.getInstance() );
    }

    @Test
    public void testNotUberJar() throws Exception {
        Layout layout = Layout.getInstance();
        assertThat( layout.isUberJar() ).isFalse();
    }

    @Test
    public void testBootstrapClassLoader() throws Exception {
        Layout layout = Layout.getInstance();
        assertThat( layout.getBootstrapClassLoader() ).isSameAs( Layout.class.getClassLoader() );
    }

    @Test
    public void testGetManifest() throws Exception {
        Layout layout = Layout.getInstance();

        System.err.println( layout.getManifest() );

    }
}
