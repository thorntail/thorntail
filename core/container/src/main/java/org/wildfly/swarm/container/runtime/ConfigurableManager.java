package org.wildfly.swarm.container.runtime;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.wildfly.swarm.config.runtime.Keyed;
import org.wildfly.swarm.config.runtime.SubresourceInfo;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.annotations.Configurable;

/**
 * @author Bob McWhirter
 */
public class ConfigurableManager implements AutoCloseable {

    private static final String DOT = ".";

    private static final String SUBRESOURCES = "subresources";

    private static final String ACCEPT = "accept";

    private static final Set<String> BLACKLISTED_FIELDS = new HashSet<String>() {{
        add("pcs");
        add("key");
        add(SUBRESOURCES);
    }};

    private static final Set<Class<?>> BLACKLISTED_CLASSES = new HashSet<Class<?>>() {{
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

        add(Map.class);
        add(Properties.class);

        add(Defaultable.class);
    }};

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.config");

    private final List<ConfigurableHandle> configurables = new ArrayList<>();

    private final StageConfig stageConfig;

    public ConfigurableManager(StageConfig stageConfig) {
        this.stageConfig = stageConfig;
    }

    public List<ConfigurableHandle> configurables() {
        return this.configurables;
    }

    protected <T> void configure(ConfigurableHandle configurable) throws IllegalAccessException {
        StageConfig.Resolver<?> resolver = this.stageConfig.resolve(configurable.name());

        Class<?> resolvedType = configurable.type();

        boolean isMap = false;
        boolean isProperties = false;

        if (resolvedType.isEnum()) {
            resolver = resolver.as((Class<Enum>) resolvedType, converter((Class<Enum>) resolvedType));
        } else if (Map.class.isAssignableFrom(resolvedType)) {
            isMap = true;
            resolver = mapResolver((StageConfig.Resolver<String>) resolver, configurable.name());
        } else if (Properties.class.isAssignableFrom(resolvedType)) {
            isProperties = true;
            resolver = propertiesResolver((StageConfig.Resolver<String>) resolver, configurable.name());
        } else {
            resolver = resolver.as(configurable.type());
        }

        if (isMap || isProperties || resolver.hasValue()) {
            Object resolvedValue = resolver.getValue();
            if (isMap && ((Map) resolvedValue).isEmpty()) {
                // ignore
            } else if (isProperties && ((Properties) resolvedValue).isEmpty()) {
                // also ignore
            } else {
                configurable.set(configurable.type().cast(resolvedValue));
            }
        }
    }

    private <ENUMTYPE extends Enum<ENUMTYPE>> StageConfig.Converter<ENUMTYPE> converter(Class<ENUMTYPE> enumType) {
        return (str) -> Enum.valueOf(enumType, str.toUpperCase().replace('-', '_'));
    }

    private StageConfig.Resolver<Map> mapResolver(StageConfig.Resolver<String> resolver, String name) {
        return resolver.withDefault("").as(Map.class, mapConverter(name));
    }

    private StageConfig.Converter<Map> mapConverter(String name) {
        return (ignored) -> {
            Map map = new HashMap();
            Set<String> subKeys = this.stageConfig.simpleSubkeys(name);

            for (String subKey : subKeys) {
                map.put(subKey, this.stageConfig.resolve(name + DOT + subKey).getValue());
            }
            return map;
        };
    }

    private StageConfig.Resolver<Properties> propertiesResolver(StageConfig.Resolver<String> resolver, String name) {
        return resolver.withDefault("").as(Properties.class, propertiesConverter(name));
    }

    private StageConfig.Converter<Properties> propertiesConverter(String name) {
        return (ignored) -> {
            Properties props = new Properties();
            Set<String> subKeys = this.stageConfig.simpleSubkeys(name);

            for (String subKey : subKeys) {
                props.setProperty(subKey, this.stageConfig.resolve(name + DOT + subKey).getValue());
            }
            return props;
        };
    }

    public void scan(Object instance) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        //scan(instance, instance instanceof Fraction);
        if (instance instanceof Fraction) {
            scanFraction((Fraction) instance);
        } else {
            scan(null, instance, false);
        }
    }

    protected void scanFraction(Fraction fraction) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String prefix = nameFor(fraction);
        scan(prefix, fraction, true);
    }

    protected String getKey(Object object) throws InvocationTargetException, IllegalAccessException {
        if (object instanceof Keyed) {
            return ((Keyed) object).getKey();
        }

        Method getKey = findGetKeyMethod(object);
        if (getKey != null) {
            Object key = getKey.invoke(object);
            if (key != null) {
                return key.toString();
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

    protected String nameFor(Fraction fraction) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Configurable anno = fraction.getClass().getAnnotation(Configurable.class);
        if (anno != null) {
            return anno.value();
        }

        String key = getKey(fraction);

        if (key == null) {
            key = fraction.getClass().getSimpleName().replace("Fraction", "").toLowerCase();
        }

        return "swarm." + key;
    }

    protected void scan(String prefix, Object instance, boolean isFraction) throws IllegalAccessException, InvocationTargetException {
        scan(prefix, instance, instance.getClass(), isFraction);
        if (isFraction) {
            scanSubresources(prefix, instance);
        }
    }

    protected void scan(String prefix, Object instance, Class<?> curClass, boolean isFraction) throws IllegalAccessException {
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
                        ConfigurableHandle configurable = new ConfigurableHandle(nameFor(prefix, field), instance, field);
                        this.configurables.add(configurable);
                        configure(configurable);
                    }
                }
            }
        }

        scan(prefix, instance, curClass.getSuperclass(), isFraction);
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

    protected String nameFor(String prefix, Field field) {
        Configurable anno = field.getAnnotation(Configurable.class);

        if (anno != null) {
            if (!anno.value().equals("")) {
                return anno.value();
            }
            if (!anno.simpleName().equals("")) {
                return prefix + DOT + anno.simpleName();
            }
        }

        return prefix + DOT + nameFor(field);
    }

    protected String nameFor(Field field) {
        StringBuilder str = new StringBuilder();

        char[] chars = field.getName().toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                str.append("-");
            }

            str.append(Character.toLowerCase(c));
        }

        return str.toString();
    }

    protected void scanSubresources(String prefix, Object instance) throws InvocationTargetException, IllegalAccessException {
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
            String subPrefix = prefix + DOT + nameFor(field);
            if (value != null && value instanceof List) {
                int index = 0;
                Set<String> seenKeys = new HashSet<>();
                for (Object each : ((List) value)) {
                    String key = getKey(each);
                    String itemPrefix = null;
                    if (key != null) {
                        seenKeys.add(key);
                        itemPrefix = subPrefix + DOT + key;
                    } else {
                        itemPrefix = subPrefix + DOT + index;
                    }
                    scan(itemPrefix, each, true);
                    ++index;
                }

                Set<String> keysWithConfiguration = this.stageConfig.simpleSubkeys(subPrefix);

                keysWithConfiguration.removeAll(seenKeys);

                if (!keysWithConfiguration.isEmpty()) {
                    Method factoryMethod = getKeyedFactoryMethod(instance, field);

                    if (factoryMethod != null) {
                        for (String key : keysWithConfiguration) {
                            String itemPrefix = subPrefix + DOT + key;
                            Object lambda = createLambda(itemPrefix, factoryMethod);
                            if (lambda != null) {
                                factoryMethod.invoke(instance, key, lambda);
                            }
                        }
                    }
                }
            } else {
                // Singleton resources, without key
                if (value == null) {
                    // If doesn't exist, only create it if there's some
                    // configuration keys that imply we want it.
                    if (this.stageConfig.hasKeyOrSubkeys(subPrefix)) {
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

    protected Object createLambda(String itemPrefix, Method factoryMethod) {

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

            MethodHandle target = lookup.findVirtual(ConfigurableManager.class, "subresourceAdded", MethodType.methodType(void.class, String.class, Object.class));

            MethodType samType = MethodType.methodType(void.class, acceptMethod.getParameterTypes()[0]);

            MethodHandle mh = LambdaMetafactory.metafactory(
                    lookup,
                    ACCEPT,
                    MethodType.methodType(consumerType, ConfigurableManager.class, String.class),
                    samType,
                    target,
                    samType)
                    .getTarget();

            return mh.invoke(this, itemPrefix);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void subresourceAdded(String itemPrefix, Object object) throws InvocationTargetException, IllegalAccessException {
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
            if (each.name().length() > longestKey) {
                longestKey = each.name().length();
            }
        }

        StringBuilder str = new StringBuilder();

        List<ConfigurableHandle> sorted = this.configurables
                .stream()
                .sorted((l, r) -> l.name().compareTo(r.name()))
                .collect(Collectors.toList());

        boolean first = true;
        for (ConfigurableHandle each : sorted) {
            try {
                String name = each.name();
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

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        SwarmMessages.MESSAGES.configuration(str.toString());
    }

    public void close() {
        this.configurables.clear();

    }

}
