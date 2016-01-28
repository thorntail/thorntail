package org.wildfly.swarm.topology;

import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Bob McWhirter
 */
public interface Topology {
    String JNDI_NAME = "swarm/topology";

    static Topology lookup() throws NamingException {
        InitialContext context = new InitialContext();
        return (Topology) context.lookup("jboss/" + Topology.JNDI_NAME);
    }

    void addListener(TopologyListener listener);

    void removeListener(TopologyListener listener);

    Map<String, List<Entry>> asMap();

    interface Entry {

        String getAddress();

        int getPort();

        String[] getTags();

    }
}
