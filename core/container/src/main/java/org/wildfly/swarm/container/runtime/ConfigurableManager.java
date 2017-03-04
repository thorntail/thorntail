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
import org.wildfly.swarm.internal.SwarmConfigMessages;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.ConfigurableAlias;
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
        add(Properties.class);
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

    public ConfigurableManager(ConfigView configView) {
        this.configView = configView;
    }

    public ConfigView configView() {
        return this.configView;
    }

    public List<ConfigurableHandle> configurables() {
        return this.configurables;
    }

    protected <T> void configure(ConfigurableHandle configurable) throws Exception {
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
            } else if (Map.class.isAssignableFrom(resolvedType)) {
                isMap = true;
                resolver = mapResolver((Resolver<String>) resolver, configurable.key());
            } else if (Properties.class.isAssignableFrom(resolvedType)) {
                isProperties = true;
                resolver = propertiesResolver((Resolver<String>) resolver, configurable.key());
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
                }
            }
        }
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
            Map map = new HashMap();
            Set<SimpleKey> subKeys = this.configView.simpleSubkeys(key);

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
            Set<SimpleKey> subKeys = this.configView.simpleSubkeys(key);

            for (SimpleKey subKey : subKeys) {
                props.setProperty(subKey.name(), this.configView.resolve(key.append(subKey)).getValue());
            }
            return props;
        };
    }

    public void rescan() throws Exception {
        for (Object each : this.deferred) {
            scanInternal(each);
        }
    }

    public void scan(Object instance) throws Exception {
        try (AutoCloseable handle = Performance.accumulate("ConfigurableManager#scan")) {
            this.deferred.add(instance);
            scanInternal(instance);
        }
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

        return ConfigKey.of("swarm").append(key);
    }

    protected void scan(ConfigKey prefix, Object instance, boolean isFraction) throws Exception {
        scan(prefix, instance, instance.getClass(), isFraction);
        if (isFraction) {
            scanSubresources(prefix, instance);
        }
    }

    protected void scan(ConfigKey prefix, Object instance, Class<?> curClass, boolean isFraction) throws Exception {
        if (curClass == null || curClass == Object.class || isBlacklisted(curClass)) {
            return;
        }
        Field[] fields = curClass.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                if (isBlacklisted(field)) {
                    continue;
                }
                if (isFraction || field.getAnnotation(Configurable.class) != null) {
                    if (isConfigurableType(field.getType())) {
                        ConfigKey name = nameFor(prefix, field);
                        if (!seen(name)) {
                            ConfigurableHandle configurable = new ObjectBackedConfigurableHandle(name, instance, field);
                            this.configurables.add(configurable);
                            configure(configurable);
                        }

                        // Process @ConfigurableAlias
                        if (field.getAnnotation(ConfigurableAlias.class) != null) {
                            name = nameForAlias(prefix, field);
                            if (!seen(name)) {
                                ConfigurableHandle configurable = new ObjectBackedConfigurableHandle(name, instance, field);
                                this.configurables.add(configurable);
                                configure(configurable);
                            }
                        }
                    }
                }
            }
        }

        scan(prefix, instance, curClass.getSuperclass(), isFraction);
    }

    private boolean seen(ConfigKey name) {
        return this.configurables.stream().anyMatch(e -> e.key().equals(name));
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

    protected ConfigKey nameFor(ConfigKey prefix, Field field) {
        Configurable anno = field.getAnnotation(Configurable.class);

        if (anno != null) {
            if (!anno.value().equals("")) {
                return ConfigKey.parse(anno.value());
            }
            if (!anno.simpleName().equals("")) {
                return prefix.append(ConfigKey.parse(anno.simpleName()));
            }
        }

        return prefix.append(nameFor(field));
    }

    protected ConfigKey nameForAlias(ConfigKey prefix, Field field) {
        ConfigurableAlias annoAlias = field.getAnnotation(ConfigurableAlias.class);

        if (annoAlias != null) {
            if (!annoAlias.value().equals("")) {
                return ConfigKey.parse(annoAlias.value());
            }
        }

        return prefix.append(nameFor(field));
    }

    protected ConfigKey nameFor(Field field) {
        StringBuilder str = new StringBuilder();

        char[] chars = field.getName().toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                str.append("-");
            }

            str.append(Character.toLowerCase(c));
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

                Set<SimpleKey> keysWithConfiguration = this.configView.simpleSubkeys(subPrefix);

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
                .sorted((l, r) -> l.key().name().compareTo(r.key().name()))
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
                e.printStackTrace();
            }
        }
        SwarmConfigMessages.MESSAGES.configuration(str.toString());
    }

    public void close() {
        this.configurables.clear();
        this.deferred.clear();
    }

}
