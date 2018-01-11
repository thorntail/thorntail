package org.jboss.unimbus.example;

import org.jboss.unimbus.UNimbus;
import org.junit.Test;

public class ServletTest {

    @Test
    public void test() {
        UNimbus.run(MyAppUNimbusConfig.class);
    }
}
