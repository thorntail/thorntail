package org.wildfly.swarm.container.runtime.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.literal.DefaultLiteral;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * Created by bob on 5/12/17.
 */
public class DeploymentScopedExtension implements Extension {

    public DeploymentScopedExtension(DeploymentContext deploymentContext) {
        this.deploymentContext = deploymentContext;

    }

    public void addScope(@Observes final BeforeBeanDiscovery event) {
        event.addScope(DeploymentScoped.class, true, false);
    }

    public void registerContext(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {

        // Register the command deploymentContext
        event.addContext(this.deploymentContext);

        // Register the command deploymentContext bean
        event.addBean(new Bean<DeploymentContext>() {

            @Override
            public DeploymentContext create(CreationalContext<DeploymentContext> creationalContext) {
                return new DeploymentContextImpl.InjectableDeploymentContext(deploymentContext, beanManager);
            }

            @Override
            public void destroy(DeploymentContext instance, CreationalContext<DeploymentContext> creationalContext) {
            }

            @Override
            public Set<Type> getTypes() {
                //return ImmutableSet.of(DeploymentContext.class);
                return Collections.unmodifiableSet(Collections.singleton(DeploymentContext.class));
            }

            @Override
            public Set<Annotation> getQualifiers() {
                //return ImmutableSet.of(DefaultLiteral.INSTANCE);
                return Collections.unmodifiableSet(Collections.singleton(DefaultLiteral.INSTANCE));
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public Class<?> getBeanClass() {
                return DeploymentScopedExtension.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }

            @Override
            public boolean isNullable() {
                return false;
            }
        });

        // Register the CommandExecution bean
        /*
        event.addBean(new Bean<CommandExecution>() {

            @Override
            public CommandExecution create(CreationalContext<CommandExecution> creationalContext) {
                return commandContext.getCurrentCommandExecution();
            }

            @Override
            public void destroy(CommandExecution instance, CreationalContext<CommandExecution> creationalContext) {
            }

            @Override
            public Set<Type> getTypes() {
                return ImmutableSet.of(CommandExecution.class);
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return ImmutableSet.of(DefaultLiteral.INSTANCE);
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return CommandScoped.class;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public Class<?> getBeanClass() {
                return CommandExtension.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }

            @Override
            public boolean isNullable() {
                return false;
            }
        });
        */
    }

    private final DeploymentContext deploymentContext;
}
