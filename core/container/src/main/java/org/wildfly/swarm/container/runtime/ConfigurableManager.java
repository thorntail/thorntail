package org.wildfly.swarm.container.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.logging.Logger;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Configurable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Bob McWhirter
 */
public class ConfigurableManager {
    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.config");

    private final List<Configurable<?>> configurables = new ArrayList<>();

    private final StageConfig stageConfig;

    public ConfigurableManager(StageConfig stageConfig) {
        this.stageConfig = stageConfig;
    }

    public List<Configurable<?>> configurables() {
        return this.configurables;
    }

    protected <T> void configure(Configurable<T> configurable) {
        if ( configurable.name() == null ) {
            return;
        }
        StageConfig.Resolver<?> resolver = this.stageConfig.resolve(configurable.name());
        resolver = resolver.as(configurable.type());

        if (resolver.hasValue()) {
            Object resolvedValue = resolver.getValue();
            configurable.set(configurable.type().cast(resolvedValue));
        }

    }

    public void scan(Object instance) throws IllegalAccessException, InvocationTargetException {
        scan(instance, instance instanceof Fraction);
    }

    protected void scan(Object instance, boolean subresources) throws IllegalAccessException, InvocationTargetException {
        scan(instance, instance.getClass());
        if (subresources) {
            scanSubresources(instance);
        }
    }

    protected void scan(Object instance, Class<?> curClass) throws IllegalAccessException {
        if (curClass == null) {
            return;
        }
        Field[] fields = curClass.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                if (field.getType().equals(Configurable.class)) {
                    field.setAccessible(true);
                    Configurable<?> configurable = (Configurable<?>) field.get(instance);
                    if (configurable != null) {
                        this.configurables.add(configurable);
                        configure(configurable);
                    }
                }
            }
        }

        scan(instance, curClass.getSuperclass());
    }

    protected void scanSubresources(Object instance) throws InvocationTargetException, IllegalAccessException {
        Method method = getSubresourcesMethod(instance);

        if ( method == null ) {
            return;
        }

        Object subresources = method.invoke(instance);

        Field[] fields = subresources.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {

                field.setAccessible(true);
                Object value = field.get(subresources);
                if (value == null) {
                    continue;
                }
                if (value instanceof Collection) {
                    for (Object each : ((Collection) value)) {
                        scan(each, true);
                    }
                } else {
                    scan(value, true);
                }
            }
        }
    }

    protected Method getSubresourcesMethod(Object instance) {
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (!method.getName().equals("subresources")) {
                continue;
            }

            if (method.getParameterCount() != 0) {
                continue;
            }

            return method;

        }

        return null;

    }

    public void log() {
        this.configurables.forEach(this::logConfiguration);
    }

    protected void logConfiguration(Configurable<?> configurable) {
        Object value = configurable.orElse(null);

        String printedValue = "(unset)";

        if (value != null) {
            printedValue = value.toString();
        }

        SwarmMessages.MESSAGES.configurationItem(
                configurable.name(),
                printedValue,
                configurable.type().getSimpleName());
    }
}
