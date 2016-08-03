package org.wildfly.swarm.jpa;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.datasources.DefaultDatasource;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;

/**
 * @author Ken Finnigan
 */
@Singleton
@Post
public class DefaultDatasourceCustomizer implements Customizer {
    @Inject
    @DefaultDatasource
    Instance<String> defaultDatasourceInstance;

    @Inject
    @Any
    Instance<JPAFraction> jpaFractionInstance;

    @Override
    public void customize() {
        if (!jpaFractionInstance.isUnsatisfied() && !defaultDatasourceInstance.isUnsatisfied()) {
            jpaFractionInstance.get().defaultDatasource("jboss/datasources/" + defaultDatasourceInstance.get());
        }
    }
}
