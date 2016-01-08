package org.wildfly.swarm.container.runtime;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public abstract class MarshallingServerConfiguration<T extends Fraction> extends ExtensionServerConfiguration<T> {


    protected MarshallingServerConfiguration(Class<T> fractionClass) {
        super( fractionClass, null );
    }

    protected MarshallingServerConfiguration(Class<T> fractionClass, String extensionModuleName) {
        super( fractionClass, extensionModuleName );
    }

    @Override
    public List<ModelNode> getList(T fraction) throws Exception {
        List<ModelNode> list = super.getList(fraction);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }
}
