package org.wildfly.swarm.container.runtime.internal.marshal;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class DMRMarshaller implements ConfigurationMarshaller  {

    @Inject
    private ExtensionMarshaller extensionMarshaller;

    @Inject
    private SubsystemMarshaller subsystemMarshaller;

    @Inject
    private InterfaceMarshaller interfaceMarshaller;

    @Inject
    private SocketBindingGroupMarshaller socketBindingGroupMarshaller;

    @Override
    public List<ModelNode> marshal() {
        List<ModelNode> list = new ArrayList<>();

        list.addAll( this.extensionMarshaller.marshal() );
        list.addAll( this.subsystemMarshaller.marshal() );
        list.addAll( this.interfaceMarshaller.marshal() );
        list.addAll( this.socketBindingGroupMarshaller.marshal() );

        return list;
    }
}
