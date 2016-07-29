package org.wildfly.swarm.container.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.ModelNodeBinding;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class FractionMarshaller {

    @Inject
    @WildFlyExtension
    private Instance<Fraction> extensionFractions;

    @Inject
    @MarshalDMR
    private Instance<Fraction> marshallableFractions;


    public List<ModelNode> marshal() {
        List<ModelNode> list = new ArrayList<>();

        marshalExtensions(list);
        marshalDMR(list);

        return list;
    }

    protected void marshalExtensions(List<ModelNode> list) {
        for (Fraction each : this.extensionFractions) {
            System.err.println("EACH EXTENSION: " + each);
            WildFlyExtension anno = each.getClass().getAnnotation(WildFlyExtension.class);
            System.err.println("extension module: " + anno.module());


            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(EXTENSION, anno.module());
            node.get(OP).set(ADD);

            list.add(node);
        }
    }

    protected void marshalDMR(List<ModelNode> list) {
        Marshaller marshaller = new Marshaller();
        for (Fraction each : this.marshallableFractions) {
            System.err.println("EACH MARSHALL: " + each);
            try {
                list.addAll(marshaller.marshal(each));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
