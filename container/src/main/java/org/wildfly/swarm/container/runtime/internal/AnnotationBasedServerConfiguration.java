/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.container.runtime.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.inject.Vetoed;
import javax.xml.namespace.QName;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.staxmapper.XMLElementReader;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.AbstractParserFactory;
import org.wildfly.swarm.spi.runtime.ServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class AnnotationBasedServerConfiguration implements ServerConfiguration {

    private final Class<? extends Fraction> type;

    private boolean marshal;

    private boolean ignorable;

    private String extension;

    private List<Configurator> configurators = new ArrayList<>();

    private Class<? extends AbstractParserFactory> parserFactoryClass;
    private AbstractParserFactory parserFactory;

    private String defaultFractionMethodName;

    private String[] deploymentModules;

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

    public void parserFactory(AbstractParserFactory parserFactory) {
        this.parserFactory = parserFactory;
    }

    public void setDeploymentModules(String[] deploymentModules) {
        this.deploymentModules = deploymentModules;
    }

    @Override
    public void prepareArchive(Archive a) {
        if ( this.deploymentModules != null ) {
            for (String each : this.deploymentModules) {
                int colonLoc = each.indexOf(':');
                if ( colonLoc > 0 ) {
                    String moduleName = each.substring(0, colonLoc );
                    String moduleSlot = each.substring(colonLoc+1);
                    a.as(JARArchive.class).addModule( moduleName, moduleSlot );
                } else {
                    String moduleName = each;
                    a.as(JARArchive.class).addModule( moduleName );
                }
            }
        }
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
        if (this.parserFactoryClass != null) {
            return AbstractParserFactory.mapParserNamespaces(this.parserFactoryClass.newInstance());
        }
        if ( this.parserFactory != null ) {
            return AbstractParserFactory.mapParserNamespaces(this.parserFactory);
        }
        return Optional.empty();
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
