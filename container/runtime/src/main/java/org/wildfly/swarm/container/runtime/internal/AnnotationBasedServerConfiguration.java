package org.wildfly.swarm.container.runtime.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;
import org.wildfly.swarm.spi.runtime.ServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class AnnotationBasedServerConfiguration implements ServerConfiguration {

    private final Class<? extends Fraction> type;

    private boolean marshal;

    private boolean ignorable;

    private String extension;

    private List<Configurator> configurators = new ArrayList<>();

    private Class<? extends AbstractParserFactory> parserFactoryClass;

    private String defaultFractionMethodName;

    public AnnotationBasedServerConfiguration(Class<? extends Fraction> type) {
        this.type = type;
    }

    public void marshal(boolean marshal) {
        this.marshal = marshal;
        if (marshal) {
            this.configurators.add(new MarshalConfigurator());
        }
    }

    public void extension(String extension) {
        this.extension = extension;
    }

    public void ignorable(boolean ignorable) {
        this.ignorable = ignorable;
    }

    public void parserFactoryClass(Class<? extends AbstractParserFactory> parserFactoryClass) {
        this.parserFactoryClass = parserFactoryClass;
    }

    @Override
    public boolean isIgnorable() {
        return this.ignorable;
    }

    @Override
    public Optional<ModelNode> getExtension() {
        if (this.extension != null && !this.extension.equals("")) {
            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(EXTENSION, this.extension);
            node.get(OP).set(ADD);
            return Optional.of(node);
        }

        return Optional.empty();
    }

    @Override
    public Class getType() {
        return this.type;
    }

    @Override
    public Optional<Map<QName, XMLElementReader<List<ModelNode>>>> getSubsystemParsers() throws Exception {
        if (this.parserFactoryClass == null) {
            return Optional.empty();
        }
        return AbstractParserFactory.mapParserNamespaces(this.parserFactoryClass.newInstance());
    }

    @Override
    public List<ModelNode> getList(Fraction fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();

        for (Configurator configurator : this.configurators) {
            configurator.execute(fraction, list);
        }

        return list;
    }

    public void defaultFraction(String defaultFractionMethodName) {
        this.defaultFractionMethodName = defaultFractionMethodName;
    }

    @Override
    public Fraction defaultFraction() {
        Method[] methods = this.type.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(this.defaultFractionMethodName) && Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0 ) {
                try {
                    return (Fraction) method.invoke(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            return this.type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}
