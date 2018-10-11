/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.runtime;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.config.runtime.Keyed;
import org.wildfly.swarm.config.runtime.SubresourceInfo;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.internal.SwarmConfigMessages;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.Configurables;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.Converter;
import org.wildfly.swarm.spi.api.config.Resolver;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * @author Bob McWhirter
 */
public class ConfigurableManager implements AutoCloseable {

    private static final String SUBRESOURCES = "subresources";

    private static final String ACCEPT = "accept";

    private static final Set<String> BLACKLISTED_FIELDS = new HashSet<String>() {{
        add("pcs");
        add("key");
        add(SUBRESOURCES);
    }};

    private static final Set<Class<?>> BLACKLISTED_CLASSES = new HashSet<Class<?>>() {{
        add(List.class);
        add(Map.class);
    }};

    private static final Set<Class<?>> CONFIGURABLE_VALUE_TYPES = new HashSet<Class<?>>() {{
        add(Boolean.class);
        add(Boolean.TYPE);
        add(Short.class);
        add(Short.TYPE);
        add(Integer.class);
        add(Integer.TYPE);
        add(Long.class);
        add(Long.TYPE);
        add(Float.class);
        add(Float.TYPE);
        add(String.class);

        add(List.class);
        add(Map.class);
        add(Properties.class);

        add(Defaultable.class);
    }};

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.config");

    private final List<ConfigurableHandle> configurables = new ArrayList<>();

    private final List<Object> deferred = new ArrayList<>();

    private final ConfigView configView;

    public ConfigurableManager(ConfigView configView, DeploymentContext deploymentContext) {
        this.configView = configView;
        this.deploymentContext = deploymentContext;
    }

    public ConfigView configView() {
        return this.configView;
    }

    public List<ConfigurableHandle> configurables() {
        return this.configurables;
    }

    @SuppressWarnings("unchecked")
    protected <T> boolean configure(ConfigurableHandle configurable) throws Exception {
        if (this.rescanning) {
            return true;
        }
        try (AutoCloseable handle = Performance.accumulate("ConfigurableManager#configure")) {
            Resolver<?> resolver = this.configView.resolve(configurable.key());

            Class<?> resolvedType = configurable.type();

            boolean isList = false;
            boolean isMap = false;
            boolean isProperties = false;

            if (resolvedType.isEnum()) {
                resolver = resolver.as((Class<Enum>) resolvedType, converter((Class<Enum>) resolvedType));
            } else if (List.class.isAssignableFrom(resolvedType)) {
                isList = true;
                resolver = listResolver((Resolver<String>) resolver, configurable.key());
            } else if (Properties.class.isAssignableFrom(resolvedType)) {
                isProperties = true;
                resolver = propertiesResolver((Resolver<String>) resolver, configurable.key());
            } else if (Map.class.isAssignableFrom(resolvedType)) {
                isMap = true;
                resolver = mapResolver((Resolver<String>) resolver, configurable.key());
            } else {
                resolver = resolver.as(resolvedType);
            }


            if (isList || isMap || isProperties || resolver.hasValue()) {
                Object resolvedValue = resolver.getValue();
                if (isList && ((List) resolvedValue).isEmpty()) {
                    // ignore
                } else if (isMap && ((Map) resolvedValue).isEmpty()) {
                    // ignore
                } else if (isProperties && ((Properties) resolvedValue).isEmpty()) {
                    // also ignore
                } else {
                    configurable.set(resolvedType.cast(resolvedValue));
                    return true;
                }
            }
        }

        return false;
    }

    private <ENUMTYPE extends Enum<ENUMTYPE>> Converter<ENUMTYPE> converter(Class<ENUMTYPE> enumType) {
        return (str) -> {
            try {
                return Enum.valueOf(enumType, str.toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value '" + str + "'; should be one of: "
                                                           + String.join(",", Arrays.stream(enumType.getEnumConstants()).map((constant) -> constant.toString()).collect(Collectors.toList())));
            }
        };
    }

    private Resolver<List> listResolver(Resolver<String> resolver, ConfigKey key) {
        return resolver.withDefault("").as(List.class, listConverter(key));
    }

    private Converter<List> listConverter(ConfigKey key) {
        return (ignored) -> {
            return this.configView.simpleSubkeys(key).stream()
                    .map((subKey) -> {
                        return this.configView.resolve(key.append(subKey)).getValue();
                    })
                    .collect(Collectors.toList());
        };
    }

    private Resolver<Map> mapResolver(Resolver<String> resolver, ConfigKey key) {
        return resolver.withDefault("").as(Map.class, mapConverter(key));
    }

    private Converter<Map> mapConverter(ConfigKey key) {
        return (ignored) -> {
            Map<String, Object> map = new HashMap<>();
            List<SimpleKey> subKeys = this.configView.simpleSubkeys(key);

            for (SimpleKey subKey : subKeys) {
                map.put(subKey.name(), this.configView.resolve(key.append(subKey)).getValue());
            }
            return map;
        };
    }

    private Resolver<Properties> propertiesResolver(Resolver<String> resolver, ConfigKey key) {
        return resolver.withDefault("").as(Properties.class, propertiesConverter(key));
    }

    private Converter<Properties> propertiesConverter(ConfigKey key) {
        return (ignored) -> {
            Properties props = new Properties();
            List<SimpleKey> subKeys = this.configView.simpleSubkeys(key);

            for (SimpleKey subKey : subKeys) {
                props.setProperty(subKey.name(), this.configView.resolve(key.append(subKey)).getValue());
            }
            return props;
        };
    }

    public void rescan() throws Exception {
        this.rescanning = true;
        try {
            for (Object each : this.deferred) {
                scanInternal(each);
            }
        } finally {
            this.rescanning = false;
        }
    }

    public void scan(Object instance) throws Exception {
        try (AutoCloseable handle = Performance.accumulate("ConfigurableManager#scan")) {
            this.deferred.add(instance);
            scanInternal(instance);
        }
    }

    public boolean hasConfiguration(Fraction fraction) throws Exception {
        ConfigKey prefix = nameFor(fraction);
        return this.configView.hasKeyOrSubkeys(prefix);
    }

    private void scanInternal(Object instance) throws Exception {
        if (instance instanceof Fraction) {
            scanFraction((Fraction) instance);
        } else {
            scan(null, instance, false);
        }
    }

    protected void scanFraction(Fraction fraction) throws Exception {
        ConfigKey prefix = nameFor(fraction);
        scan(prefix, fraction, true);
    }

    protected SimpleKey getKey(Object object) throws Exception {
        if (object instanceof Keyed) {
            return new SimpleKey(((Keyed) object).getKey());
        }

        Method getKey = findGetKeyMethod(object);
        if (getKey != null) {
            Object key = getKey.invoke(object);
            if (key != null) {
                return new SimpleKey(key.toString());
            }
        }
        return null;
    }

    protected Method findGetKeyMethod(Object object) {
        Method[] methods = object.getClass().getMethods();

        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (!method.getName().equals("getKey")) {
                continue;
            }

            if (method.getParameterCount() != 0) {
                continue;
            }

            return method;
        }

        return null;
    }

    protected ConfigKey nameFor(Fraction fraction) throws Exception {
        Configurable anno = fraction.getClass().getAnnotation(Configurable.class);
        if (anno != null) {
            return ConfigKey.parse(anno.value());
        }

        SimpleKey key = getKey(fraction);

        if (key == null) {
            key = new SimpleKey(fraction.getClass().getSimpleName().replace("Fraction", "").toLowerCase());
        }

        return ConfigKey.of("thorntail").append(key);
    }

    protected void scan(ConfigKey prefix, Object instance, boolean implicit) throws Exception {
        if (seen(prefix)) {
            return;
        }
        this.seenObjects.add(prefix);
        scan(prefix, instance, instance.getClass(), implicit);
        if (implicit) {
            scanSubresources(prefix, instance);
        }
    }

    protected void scan(ConfigKey prefix, Object instance, Class<?> curClass, boolean implicit) throws Exception {
        if (curClass == null || curClass == Object.class || isBlacklisted(curClass)) {
            return;
        }
        Field[] fields = curClass.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                if (isBlacklisted(field)) {
                    continue;
                }
                if (implicit || field.getAnnotation(Configurable.class) != null || field.getAnnotation(Configurables.class) != null) {
                    if (isConfigurableType(field.getType())) {
                        List<ConfigKey> names = namesFor(prefix, field);

                        boolean configured = false;

                        for (ConfigKey name : names) {
                            if (!seen(name)) {
                                ConfigurableHandle configurable = new ObjectBackedConfigurableHandle(name, instance, field);
                                this.configurables.add(configurable);
                                configured = configure(configurable);
                            }
                            if (configured) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (!rescanning) {
            Method[] methods = curClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Configurable.class)) {
                    ConfigKey subPrefix = prefix.append(nameFor(method));
                    if (method.getParameterCount() == 1) {
                        // If doesn't exist, only create it if there's some
                        // configuration keys that imply we want it.
                        if (this.configView.hasKeyOrSubkeys(subPrefix)) {
                            Object lambda = createLambda(subPrefix, method);
                            if (lambda != null) {
                                method.invoke(instance, lambda);
                            }
                        }
                    } else if (method.getParameterCount() == 2) {
                        List<SimpleKey> keysWithConfiguration = this.configView.simpleSubkeys(subPrefix);
                        if (!keysWithConfiguration.isEmpty()) {
                            for (SimpleKey key : keysWithConfiguration) {
                                ConfigKey itemPrefix = subPrefix.append(key);
                                Object lambda = createLambda(itemPrefix, method);
                                if (lambda != null) {
                                    method.invoke(instance, key.name(), lambda);
                                }
                            }
                        }

                    }
                }
            }
        }

        scan(prefix, instance, curClass.getSuperclass(), implicit);
    }

    private boolean seen(ConfigKey name) {
        if (name == null) {
            return false;
        }
        if (this.deploymentContext.isActive()) {
            // we wish to allow multiple configurables if
            // this is a deployment-activated context.
            return false;
        }
        return this.seenObjects.contains(name) || this.configurables.stream().anyMatch(e -> e.key().equals(name));
    }

    private boolean isConfigurableType(Class<?> type) {
        return type.isEnum() || CONFIGURABLE_VALUE_TYPES.contains(type);
    }

    private boolean isBlacklisted(Class<?> cls) {
        return BLACKLISTED_CLASSES.stream().anyMatch((e) -> {
            if (e.isInterface()) {
                for (Class<?> each : cls.getInterfaces()) {
                    if (each == e) {
                        return true;
                    }
                }
                return false;
            } else {
                return e == cls;
            }
        });
    }

    private boolean isBlacklisted(Field field) {
        if (BLACKLISTED_FIELDS.stream().anyMatch((e) -> e.equals(field.getName()))) {
            return true;
        }

        return isBlacklisted(field.getType());
    }

    protected List<ConfigKey> namesFor(ConfigKey prefix, Field field) {

        List<ConfigKey> names = new ArrayList<>();

        Configurables plural = field.getAnnotation(Configurables.class);
        if (plural != null) {
            for (Configurable each : plural.value()) {
                ConfigKey key = nameFor(prefix, each);
                if (key != null) {
                    names.add(key);
                }
            }
        } else {
            Configurable[] annos = field.getAnnotationsByType(Configurable.class);
            if (annos != null && annos.length > 0) {
                for (Configurable anno : annos) {
                    ConfigKey key = nameFor(prefix, anno);
                    if (key != null) {
                        names.add(key);
                    }
                }
            } else {
                ConfigKey key = handleDeploymentConfiguration(prefix.append(nameFor(field)));
                names.add(key);
            }
        }

        return names;
    }

    protected ConfigKey nameFor(ConfigKey prefix, Configurable anno) {
        if (!anno.value().equals("")) {
            return handleDeploymentConfiguration(ConfigKey.parse(anno.value()));
        }

        if (!anno.simpleName().equals("")) {
            handleDeploymentConfiguration(prefix.append(ConfigKey.parse(anno.simpleName())));
        }

        return null;
    }

    private static ConfigKey DEPLOYMENT_PREFIX = ConfigKey.parse("thorntail.deployment.*");

    protected ConfigKey handleDeploymentConfiguration(ConfigKey in) {
        if (!this.deploymentContext.isActive()) {
            return in;
        }

        if (in.isChildOf(DEPLOYMENT_PREFIX)) {
            in.replace(2, this.deploymentContext.getCurrentName());
        }

        return in;
    }

    protected ConfigKey nameFor(Field member) {
        StringBuilder str = new StringBuilder();

        char[] chars = member.getName().toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                str.append("-");
            }

            str.append(Character.toLowerCase(c));
        }

        return ConfigKey.of(str.toString());
    }

    protected ConfigKey nameFor(Method member) {
        StringBuilder str = new StringBuilder();

        char[] chars = member.getName().toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                str.append("-");
            }

            str.append(Character.toLowerCase(c));
        }

        if (member.getParameterCount() == 2) {
            // pluralize since it's keyed.
            str.append("s");
        }

        return ConfigKey.of(str.toString());
    }

    protected void scanSubresources(ConfigKey prefix, Object instance) throws Exception {
        Method method = getSubresourcesMethod(instance);

        if (method == null) {
            return;
        }

        Object subresources = method.invoke(instance);

        Field[] fields = subresources.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getAnnotation(SubresourceInfo.class) == null && List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(subresources);
            ConfigKey subPrefix = prefix.append(nameFor(field));
            if (seen(subPrefix)) {
                continue;
            }
            if (value != null && value instanceof List) {
                int index = 0;
                Set<SimpleKey> seenKeys = new HashSet<>();
                for (Object each : ((List) value)) {
                    SimpleKey key = getKey(each);
                    ConfigKey itemPrefix = null;
                    if (key != null) {
                        seenKeys.add(key);
                        itemPrefix = subPrefix.append(key);
                    } else {
                        itemPrefix = subPrefix.append("" + index);
                    }
                    scan(itemPrefix, each, true);
                    ++index;
                }

                List<SimpleKey> keysWithConfiguration = this.configView.simpleSubkeys(subPrefix);

                keysWithConfiguration.removeAll(seenKeys);

                if (!keysWithConfiguration.isEmpty()) {
                    Method factoryMethod = getKeyedFactoryMethod(instance, field);

                    if (factoryMethod != null) {
                        for (SimpleKey key : keysWithConfiguration) {
                            ConfigKey itemPrefix = subPrefix.append(key);
                            Object lambda = createLambda(itemPrefix, factoryMethod);
                            if (lambda != null) {
                                factoryMethod.invoke(instance, key.name(), lambda);
                            }
                        }
                    }
                }
            } else {
                // Singleton resources, without key
                if (value == null) {
                    // If doesn't exist, only create it if there's some
                    // configuration keys that imply we want it.
                    if (this.configView.hasKeyOrSubkeys(subPrefix)) {
                        Method factoryMethod = getNonKeyedFactoryMethod(instance, field);
                        if (factoryMethod != null) {
                            Object lambda = createLambda(subPrefix, factoryMethod);
                            if (lambda != null) {
                                factoryMethod.invoke(instance, lambda);
                            }
                        }
                    }
                } else {
                    scan(subPrefix, value, true);
                }
            }
        }
    }

    protected Object createLambda(ConfigKey itemPrefix, Method factoryMethod) {

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // The consumer is the last parameter
        Class<?> consumerType = factoryMethod.getParameterTypes()[factoryMethod.getParameterCount() - 1];

        try {
            Method acceptMethod = null;
            for (Method method : consumerType.getMethods()) {
                if (method.getName().equals(ACCEPT)) {
                    acceptMethod = method;
                }
            }

            if (acceptMethod == null) {
                return null;
            }

            MethodHandle target = lookup.findVirtual(ConfigurableManager.class, "subresourceAdded", MethodType.methodType(void.class, ConfigKey.class, Object.class));

            MethodType samType = MethodType.methodType(void.class, acceptMethod.getParameterTypes()[0]);

            MethodHandle mh = LambdaMetafactory.metafactory(
                    lookup,
                    ACCEPT,
                    MethodType.methodType(consumerType, ConfigurableManager.class, ConfigKey.class),
                    samType,
                    target,
                    samType)
                    .getTarget();

            return mh.invoke(this, itemPrefix);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void subresourceAdded(ConfigKey itemPrefix, Object object) throws Exception {
        scan(itemPrefix, object, true);
    }


    protected Method getKeyedFactoryMethod(Object instance, Field field) {
        SubresourceInfo anno = field.getAnnotation(SubresourceInfo.class);
        if (anno != null) {
            String name = anno.value();
            Method[] methods = instance.getClass().getMethods();
            for (Method method : methods) {
                if (!method.getName().equals(name)) {
                    continue;
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                if (method.getParameterCount() != 2) {
                    continue;
                }

                if (method.getParameterTypes()[0] != String.class) {
                    continue;
                }

                if (method.getParameterTypes()[1].getAnnotation(FunctionalInterface.class) == null) {
                    continue;
                }

                boolean acceptMethodFound = false;
                for (Method paramMethod : method.getParameterTypes()[1].getMethods()) {
                    if (paramMethod.getName().equals(ACCEPT)) {
                        acceptMethodFound = true;
                        break;
                    }
                }

                if (!acceptMethodFound) {
                    continue;
                }

                return method;
            }

        }
        return null;
    }

    protected Method getNonKeyedFactoryMethod(Object instance, Field field) {
        String name = field.getName();
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().equals(name)) {
                continue;
            }

            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                continue;
            }

            if (method.getParameterTypes()[0].getAnnotation(FunctionalInterface.class) == null) {
                continue;
            }

            boolean acceptMethodFound = false;
            for (Method paramMethod : method.getParameterTypes()[0].getMethods()) {
                if (paramMethod.getName().equals(ACCEPT)) {
                    acceptMethodFound = true;
                    break;
                }
            }

            if (!acceptMethodFound) {
                continue;
            }

            return method;
        }

        return null;
    }

    protected Method getSubresourcesMethod(Object instance) {
        Method[] methods = instance.getClass().getMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (!method.getName().equals(SUBRESOURCES)) {
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

        // just for a while
        boolean verbose = true;

        int longestKey = 0;

        for (ConfigurableHandle each : this.configurables) {
            if (each.key().name().length() > longestKey) {
                longestKey = each.key().name().length();
            }
        }

        StringBuilder str = new StringBuilder();

        List<ConfigurableHandle> sorted = this.configurables
                .stream()
                .sorted(Comparator.comparing(l -> l.key().name()))
                .collect(Collectors.toList());

        boolean first = true;
        for (ConfigurableHandle each : sorted) {
            try {
                String name = each.key().name();
                Object value = each.currentValue();

                if (value != null || verbose) {
                    String printedValue = "(unset)";
                    if (value != null) {
                        if (name.toLowerCase().contains("password")) {
                            printedValue = "<redacted>";
                        } else {
                            printedValue = value.toString();
                        }
                    }

                    if (!first) {
                        str.append("\n");
                    }
                    str.append(String.format("  %-" + longestKey + "s = %s", name, printedValue));
                    first = false;
                }

            } catch (Exception e) {
                SwarmConfigMessages.MESSAGES.errorResolvingConfigurableValue(each.key().name(), e);
            }
        }
        SwarmConfigMessages.MESSAGES.configuration(str.toString());
    }

    public void close() {
        this.seenObjects.clear();
        this.configurables.clear();
        this.deferred.clear();
    }

    private Set<ConfigKey> seenObjects = new HashSet<>();

    private final DeploymentContext deploymentContext;

    private boolean rescanning;
}
