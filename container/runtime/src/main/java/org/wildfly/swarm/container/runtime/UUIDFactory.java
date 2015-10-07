package org.wildfly.swarm.container.runtime;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class UUIDFactory {

    public static UUID getUUID() {


        String swarmNodeId = System.getProperty( "swarm.node.id");
        String jbossNodeName = System.getProperty( "jboss.node.name" );

        String uuidInput = null;

        // Prefer swarm.node.id, if present and jboss.node.name is
        // not, then set jboss.node.name=swarm.node.id
        if ( swarmNodeId != null ) {
            uuidInput = swarmNodeId;
            if ( jbossNodeName == null ) {
                System.setProperty( "jboss.node.name", swarmNodeId );
            }
        } if ( jbossNodeName != null ) {
            uuidInput = jbossNodeName;
        }

        // if neither swarm.node.id nor jboss.node.name are set,
        // just generate a random UUID
        if ( uuidInput == null ) {
            return UUID.randomUUID();
        }

        return UUID.nameUUIDFromBytes( uuidInput.getBytes() );
    }
}
