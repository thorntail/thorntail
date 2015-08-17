package org.wildfly.swarm.transactions;

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.naming.NamingFraction;

/**
 * @author Bob McWhirter
 */
public class TransactionsInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new TransactionsFraction() );
        container.start().stop();
    }
}
