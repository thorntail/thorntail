package org.wildfly.swarm.jaxrs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        JAXRSArquillianTest.class,
        ArqJAXRSExceptionMapperTest.class,
        ApplicationPathTest.class
})

public class JAXRSTestSuite {
  // the class remains empty,
  // used only as a holder for the above annotations
}
