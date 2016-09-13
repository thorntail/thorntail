package org.wildfly.swarm.container.runtime.deployments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.DefaultDeploymentFactory;

/**
 * @author Bob McWhirter
 */
@Singleton
public class DefaultDeploymentCreator {

    private Map<String, DefaultDeploymentFactory> factories = new HashMap<>();

    @Inject
    public DefaultDeploymentCreator(@Any Instance<DefaultDeploymentFactory> factories) {
        this((Iterable<DefaultDeploymentFactory>) factories);
    }

    public DefaultDeploymentCreator(DefaultDeploymentFactory... factories) {
        this(Arrays.asList(factories));
    }

    public DefaultDeploymentCreator(Iterable<DefaultDeploymentFactory> factories) {
        for (DefaultDeploymentFactory factory : factories) {
            final DefaultDeploymentFactory current = this.factories.get(factory.getType());
            if (current == null) {
                this.factories.put(factory.getType(), factory);
            } else {
                // if this one is high priority than the previously-seen factory, replace it.
                if (factory.getPriority() > current.getPriority()) {
                    this.factories.put(factory.getType(), factory);
                }
            }
        }
    }

    private static final String[] EFFECTIVE_TYPES = {
            "ear",
            "war",
            "rar",
            "jar",
    };

    protected String effectiveType(String nativeType) {
        if (!nativeType.equals("jar")) {
            return nativeType;
        }

        for (String each : EFFECTIVE_TYPES) {
            if (this.factories.containsKey(each)) {
                return each;
            }
        }

        return nativeType;
    }

    public Archive<?> createDefaultDeployment(String type) {
        try {
            DefaultDeploymentFactory factory = getFactory(effectiveType(type));

            if (type.equals("jar")) {
                return factory.createFromJar();
            }
            return factory.create();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    DefaultDeploymentFactory getFactory(String type) {
        DefaultDeploymentFactory factory = this.factories.get(type);
        if (factory != null) {
            return factory;
        }
        return new EmptyJARArchiveDeploymentFactory(type);
    }


    private static class EmptyJARArchiveDeploymentFactory extends DefaultDeploymentFactory {
        private final String type;

        public EmptyJARArchiveDeploymentFactory(String type) {
            this.type = type;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public Archive create() throws Exception {
            JARArchive archive = ShrinkWrap.create(JARArchive.class, UUID.randomUUID().toString() + "." + this.type);
            archive.add(EmptyAsset.INSTANCE, "empty.txt" );
            return archive;
        }

        @Override
        public Archive createFromJar() throws Exception {
            return create();
        }

        protected boolean setupUsingMaven(Archive<?> archive) throws Exception {
            return false;
        }
    }

}
